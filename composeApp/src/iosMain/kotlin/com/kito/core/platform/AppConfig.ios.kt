package com.kito.core.platform

import platform.Foundation.NSBundle
import kotlin.experimental.ExperimentalNativeApi

actual object AppConfig {
    actual var portalBase: String
        get() = NSBundle.mainBundle.infoDictionary?.get("PORTAL_BASE") as? String ?: ""
        set(_) {} // iOS values come from Info.plist, setter is no-op

    actual var wdPath: String
        get() = NSBundle.mainBundle.infoDictionary?.get("WD_PATH") as? String ?: ""
        set(_) {} // iOS values come from Info.plist, setter is no-op

    actual var supabaseUrl: String
        get() = NSBundle.mainBundle.infoDictionary?.get("SUPABASE_URL") as? String ?: ""
        set(_) {} // iOS values come from Info.plist, setter is no-op

    actual var supabaseAnonKey: String
        get() = NSBundle.mainBundle.infoDictionary?.get("SUPABASE_ANON_KEY") as? String ?: ""
        set(_) {} // iOS values come from Info.plist, setter is no-op
    @OptIn(ExperimentalNativeApi::class)
    actual var isDebug: Boolean
        get() = Platform.isDebugBinary
        set(_) {}

    actual var kgAPIKey: String
        get() = NSBundle.mainBundle.infoDictionary?.get("KHAOOGULLY_API_KEY") as? String ?: ""
        set(_) {}
    actual var kgBaseURL: String
        get() = NSBundle.mainBundle.infoDictionary?.get("KHAOOGULLY_BASE_URL") as? String ?: ""
        set(_) {}
    actual var cdnURL: String
        get() = NSBundle.mainBundle.infoDictionary?.get("CDN_URL") as? String ?: ""
        set(_) {}
}

