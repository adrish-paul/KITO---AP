package com.kito.core.platform

actual object AppConfig {
    actual var portalBase: String = System.getenv("PORTAL_BASE") ?: ""
    actual var wdPath: String = System.getenv("WD_PATH") ?: ""
    actual var supabaseUrl: String = System.getenv("SUPABASE_URL") ?: ""
    actual var supabaseAnonKey: String = System.getenv("SUPABASE_ANON_KEY") ?: ""
    actual var isDebug: Boolean = true
    actual var kgAPIKey: String = System.getenv("KHAOOGULLY_API_KEY") ?: ""
    actual var kgBaseURL: String = System.getenv("KHAOOGULLY_BASE_URL") ?: ""
    actual var cdnURL: String = System.getenv("CDN_URL") ?: ""
    actual var googleServerClientId: String = System.getenv("GOOGLE_SERVER_CLIENT_ID") ?: ""
}
