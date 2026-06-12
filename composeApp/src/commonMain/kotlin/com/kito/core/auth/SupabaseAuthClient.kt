package com.kito.core.auth

import com.kito.core.platform.AppConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient

/**
 * OAuth redirect deep-link target. MUST match, in all three places:
 *  - Supabase dashboard → Auth → URL Configuration → Redirect URLs  (kiito://auth-callback)
 *  - Android: composeApp AndroidManifest intent-filter
 *  - iOS: Info.plist CFBundleURLSchemes
 */
const val AUTH_REDIRECT_SCHEME: String = "kiito"
const val AUTH_REDIRECT_HOST: String = "auth-callback"

/**
 * SDK client used ONLY for auth (GoTrue). The raw-Ktor REST client (createSupabaseClient in
 * SupabaseAuthInterceptor.kt) is unchanged and keeps using the anon key — we migrate REST later.
 */
fun createSupabaseAuthClient(): SupabaseClient =
    createSupabaseClient(
        supabaseUrl = AppConfig.supabaseUrl,
        supabaseKey = AppConfig.supabaseAnonKey,
    ) {
        install(Auth) {
            scheme = AUTH_REDIRECT_SCHEME
            host = AUTH_REDIRECT_HOST
        }
        // Native account-picker sign-in (Credential Manager on Android / native on iOS).
        // serverClientId must be the Google "Web" (server) OAuth client ID.
        install(ComposeAuth) {
            googleNativeLogin(serverClientId = AppConfig.googleServerClientId)
        }
    }
