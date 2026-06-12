package com.kito.core.platform

import com.kito.core.sync.domain.SyncError
import com.kito.sap.sensitive.SapError

object ErrorSanitizer {

    fun sanitize(error: SapError): String = if (AppConfig.isDebug) {
        "[${error.code}] ${error.internalMessage}"
    } else {
        "${error.userMessage} (${error.code})"
    }

    fun sanitize(error: SyncError): String = if (AppConfig.isDebug) {
        "[${error.code}] ${error.internalMessage}"
    } else {
        "${error.userMessage} (${error.code})"
    }

    fun log(error: SapError) {
        if (AppConfig.isDebug) println("🔴 SapError [${error.code}]: ${error.internalMessage}")
        // else: CrashReporter.record(error.code, error.internalMessage)
    }

    fun log(error: SyncError) {
        if (AppConfig.isDebug) println("🔴 SyncError [${error.code}]: ${error.internalMessage}")
        // else: CrashReporter.record(error.code, error.internalMessage)
    }
}