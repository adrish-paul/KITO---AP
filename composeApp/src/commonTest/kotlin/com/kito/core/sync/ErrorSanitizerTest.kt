package com.kito.core.sync

import com.kito.core.platform.AppConfig
import com.kito.core.platform.ErrorSanitizer
import com.kito.core.sync.domain.SyncError
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class ErrorSanitizerTest {

    @BeforeTest fun forceProductionMode() { AppConfig.isDebug = false }
    @AfterTest  fun restoreDebugMode()    { AppConfig.isDebug = true  }

    @Test
    fun sanitize_productionMode_returnsUserMessage_notInternalMessage() {
        val error = SyncError.StudentFetchFailed("stack trace here")
        val result = ErrorSanitizer.sanitize(error)

        // In production: "[userMessage] (code)" — no internal details
        assertTrue(result.contains(error.code), "Code must appear in sanitized output")
        assertTrue(!result.contains("stack trace"), "Internal cause must NOT appear")
        assertTrue(result.contains(error.userMessage), "User-safe message must appear")
    }

    @Test
    fun sanitize_debugMode_returnsInternalMessage() {
        AppConfig.isDebug = true
        val error = SyncError.DatabaseWriteFailed("foreign key constraint")
        val result = ErrorSanitizer.sanitize(error)

        assertTrue(result.contains("foreign key constraint"), "Debug mode should expose internal message")
        assertTrue(result.contains(error.code))
    }

    @Test
    fun sanitize_allSyncErrorTypes_neverBlank() {
        AppConfig.isDebug = false
        val errors = listOf(
            SyncError.StudentFetchFailed("x"),
            SyncError.TimetableFetchFailed("A", "B", "x"),
            SyncError.DatabaseWriteFailed("x"),
            SyncError.AttendanceSyncFailed("Safe message"),
            SyncError.SyncTriggerFailed("x"),
            SyncError.UnknownError("x"),
        )
        errors.forEach { e ->
            val s = ErrorSanitizer.sanitize(e)
            assertTrue(s.isNotBlank(), "sanitize() returned blank for ${e.code}")
        }
    }
}
