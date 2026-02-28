package com.kito.core.platform

expect object AppConfig {
    var portalBase: String
    var wdPath: String
    var supabaseUrl: String
    var supabaseAnonKey: String
    var isDebug: Boolean
}

