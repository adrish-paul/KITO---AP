package com.kito.core.auth

import android.content.Intent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import org.koin.mp.KoinPlatform

/**
 * Forwards an incoming OAuth redirect Intent (kiito://auth-callback) to Supabase so it can
 * complete the PKCE exchange and emit the authenticated session. Called from MainActivity.
 * Kept here (composeApp/androidMain) so androidApp never references supabase-kt types directly.
 */
object AndroidAuthDeepLink {
    fun handle(intent: Intent) {
        runCatching {
            KoinPlatform.getKoin().get<SupabaseClient>().handleDeeplinks(intent)
        }
    }
}
