package com.kito.core.platform

actual object AppConfig {
    actual var portalBase: String = ""
    actual var wdPath: String = ""
    actual var supabaseUrl: String = ""
    actual var supabaseAnonKey: String = ""
    actual var isDebug: Boolean = false


    fun init(
        portalBase: String,
        wdPath: String,
        supabaseUrl: String,
        supabaseAnonKey: String,
        isDebug: Boolean
    ) {
        this.portalBase = portalBase
        this.wdPath = wdPath
        this.supabaseUrl = supabaseUrl
        this.supabaseAnonKey = supabaseAnonKey
        this.isDebug = isDebug
    }
}
