package com.kito.core.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Supabase (GoTrue) implementation of [AuthRepository].
 *
 * Drives [authState] from `auth.sessionStatus`. The Google sign-in uses the OAuth redirect flow:
 * [signInWithGoogle] launches the browser; the session is delivered later via the deep-link
 * callback (handled per-platform), which updates `sessionStatus` → [authState].
 *
 * Security note: the @kiit.ac.in / allowlist check here is the CLIENT-SIDE mirror only. The
 * authoritative gate is the server-side Supabase auth hook (see setup checklist).
 */
class SupabaseAuthRepository(
    private val client: SupabaseClient,
    scope: CoroutineScope,
    private val allowlist: Set<String> = emptySet(),
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 8)
    override val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    init {
        scope.launch {
            client.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> handleAuthenticated(status.session.user)
                    is SessionStatus.NotAuthenticated -> _authState.value = AuthState.Unauthenticated
                    is SessionStatus.Initializing -> _authState.value = AuthState.Loading
                    is SessionStatus.RefreshFailure -> _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    private suspend fun handleAuthenticated(user: UserInfo?) {
        val email = user?.email?.trim()?.lowercase()
        if (user == null || email.isNullOrEmpty()) {
            _authState.value = AuthState.Unauthenticated
            _events.emit(AuthEvent.Failure("No email associated with the Google account"))
            runCatching { client.auth.signOut() }
            return
        }
        if (!AccessPolicy.isEmailAllowed(email, allowlist)) {
            runCatching { client.auth.signOut() }
            _authState.value = AuthState.Unauthenticated
            _events.emit(AuthEvent.NotAllowed(email))
            return
        }
        val name = extractName(user) ?: email.substringBefore("@")
        val roll = AccessPolicy.rollFromEmail(email).orEmpty()
        _authState.value = AuthState.Authenticated(
            AuthUser(
                providerId = user.id,
                email = email,
                rollNumber = roll,
                name = name,
            )
        )
    }

    private fun extractName(user: UserInfo): String? {
        val metadata = user.userMetadata ?: return null
        val element = metadata["display_name"] ?: metadata["full_name"] ?: metadata["name"] ?: return null
        return element.jsonPrimitive.contentOrNull?.takeIf { it.isNotBlank() }
    }

    override suspend fun restoreSession() {
        // supabase-kt auto-loads & refreshes the persisted session on init; sessionStatus emits it.
        // No-op kept for the interface contract / future providers.
    }

    override suspend fun signInWithGoogle() {
        runCatching { client.auth.signInWith(Google) }
            .onFailure { e -> _events.emit(AuthEvent.Failure(e.message ?: "Google sign-in failed")) }
    }

    override suspend fun signOut() {
        runCatching { client.auth.signOut() }
    }

    override suspend fun updateDisplayName(name: String) {
        if (_authState.value !is AuthState.Authenticated) return
        runCatching {
            client.auth.updateUser {
                data {
                    put("display_name", kotlinx.serialization.json.JsonPrimitive(name))
                }
            }
        }
        // Failure is silent — local name is already saved; Supabase update is best-effort.
    }

    override fun currentUser(): AuthUser? =
        (_authState.value as? AuthState.Authenticated)?.user
}
