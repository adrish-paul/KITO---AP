package com.kito.core.platform

expect object AppConfig {
    var portalBase: String
    var wdPath: String
    var supabaseUrl: String
    var supabaseAnonKey: String
    var isDebug: Boolean
    var kgAPIKey: String
    var kgBaseURL: String
    var cdnURL: String
    // Google "Web" / server client ID — required by Credential Manager native Google sign-in
    var googleServerClientId: String
}

