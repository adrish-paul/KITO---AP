package com.kito.core.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import org.koin.mp.KoinPlatform
import platform.Foundation.NSURL

/**
 * Forwards an incoming OAuth redirect URL (kiito://auth-callback) to Supabase to complete the
 * PKCE exchange. Called from iOSApp.swift's `.onOpenURL`.
 *
 * NOTE: this file is in iosMain and is NOT compiled on the Windows dev machine — verify on a Mac.
 */
object IosAuthDeepLink {
    fun handle(urlString: String) {
        val url = NSURL(string = urlString) ?: return
        runCatching {
            KoinPlatform.getKoin().get<SupabaseClient>().handleDeeplinks(url)
        }
    }
}
