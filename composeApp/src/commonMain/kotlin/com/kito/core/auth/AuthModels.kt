package com.kito.core.auth

/**
 * App-level identity. Keyed on [email] / [rollNumber] — NEVER on the Supabase user id ([providerId]).
 * Keeping our own data keyed on email/roll is what makes a future auth-provider swap a simple
 * re-map by email instead of a UUID migration.
 *
 * - [rollNumber] is derived from the email local part (e.g. 23053382@kiit.ac.in -> 23053382)
 * - [name] comes from the Google account profile (display name)
 * - [providerId] is the Supabase `sub` (kept for reference/debugging only)
 */
data class AuthUser(
    val providerId: String,
    val email: String,
    val rollNumber: String,
    val name: String,
)

/** Observable authentication state for the app. */
sealed interface AuthState {
    /** Session is being restored at startup. */
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(val user: AuthUser) : AuthState
}

/** One-off UX events emitted during/after a sign-in attempt (for snackbars/toasts). */
sealed interface AuthEvent {
    /** User dismissed the OAuth flow. */
    data object Cancelled : AuthEvent

    /**
     * Signed in with Google, but the email is not permitted (not @kiit.ac.in and not allowlisted).
     * The session is signed out before this is emitted. NOTE: the authoritative check is the
     * server-side Supabase auth hook; this is the client-side mirror for UX.
     */
    data class NotAllowed(val email: String) : AuthEvent

    data class Failure(val message: String) : AuthEvent
}
