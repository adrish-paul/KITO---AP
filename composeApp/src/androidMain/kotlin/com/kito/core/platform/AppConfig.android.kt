package com.kito.core.platform

actual object AppConfig {
    actual var portalBase: String = ""
    actual var wdPath: String = ""
    actual var supabaseUrl: String = ""
    actual var supabaseAnonKey: String = ""
    actual var isDebug: Boolean = false

    actual var kgAPIKey: String = ""
    actual var kgBaseURL: String = ""
    actual var cdnURL: String = ""
    actual var googleServerClientId: String = ""


    fun init(
        portalBase: String,
        wdPath: String,
        supabaseUrl: String,
        supabaseAnonKey: String,
        isDebug: Boolean,
        kgAPIKey: String,
        kgBaseURL: String,
        cdnURL: String,
        googleServerClientId: String
    ) {
        this.portalBase = portalBase
        this.wdPath = wdPath
        this.supabaseUrl = supabaseUrl
        this.supabaseAnonKey = supabaseAnonKey
        this.isDebug = isDebug
        this.kgAPIKey = kgAPIKey
        this.kgBaseURL = kgBaseURL
        this.cdnURL = cdnURL
        this.googleServerClientId = googleServerClientId
    }
}
