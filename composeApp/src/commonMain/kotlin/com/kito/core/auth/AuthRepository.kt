package com.kito.core.auth

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Provider-agnostic authentication boundary.
 *
 * The Supabase implementation lives in the data layer; the UI/ViewModels depend only on this
 * interface. Swapping Supabase for a custom backend or custom auth later = a new implementation
 * + one DI binding change, with no UI changes.
 */
interface AuthRepository {

    /** Current auth state, hot. Starts as [AuthState.Loading] until the session is restored. */
    val authState: StateFlow<AuthState>

    /** One-off UX events (cancelled / not-allowed / failure). */
    val events: SharedFlow<AuthEvent>

    /** Restore a persisted session (call on app start). */
    suspend fun restoreSession()

    /**
     * Launch the Google OAuth (redirect/deep-link) flow. The outcome is delivered asynchronously
     * via [authState] (on success) and [events] (on rejection/failure/cancel).
     */
    suspend fun signInWithGoogle()

    suspend fun signOut()

    /** Update the display name in Supabase user metadata (no-op if not authenticated via OAuth). */
    suspend fun updateDisplayName(name: String)

    fun currentUser(): AuthUser?
}
