package com.kito.core.sync

import com.kito.core.sync.domain.SyncError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SyncErrorTest {

    // Each SyncError must: have the correct code, have a user-safe message,
    // and the internal message must NOT equal the user message.

    @Test
    fun studentFetchFailed_code001() {
        val e = SyncError.StudentFetchFailed("db timeout")
        assertEquals("SYNC_001", e.code)
        assertFalse(e.userMessage.contains("db timeout"), "Internal cause leaked to userMessage")
        assertFalse(e.userMessage.isBlank())
    }

    @Test
    fun timetableFetchFailed_code002() {
        val e = SyncError.TimetableFetchFailed("CS-A", "B1", "503")
        assertEquals("SYNC_002", e.code)
        assertFalse(e.userMessage.contains("503"), "Internal cause leaked to userMessage")
        assertFalse(e.userMessage.isBlank())
    }

    @Test
    fun databaseWriteFailed_code003() {
        val e = SyncError.DatabaseWriteFailed("constraint violation")
        assertEquals("SYNC_003", e.code)
        assertFalse(e.userMessage.contains("constraint"), "Internal cause leaked to userMessage")
        assertFalse(e.userMessage.isBlank())
    }

    @Test
    fun attendanceSyncFailed_code004_userMessageIsTheSanitizedInput() {
        // SYNC_004 is special: userMessage IS the sanitized message (came from ErrorSanitizer already)
        val sanitized = "Could not load attendance. Please try again."
        val e = SyncError.AttendanceSyncFailed(sanitized)
        assertEquals("SYNC_004", e.code)
        assertEquals(sanitized, e.userMessage)
    }

    @Test
    fun syncTriggerFailed_code005() {
        val e = SyncError.SyncTriggerFailed("null pointer")
        assertEquals("SYNC_005", e.code)
        assertFalse(e.userMessage.contains("null pointer"), "Internal cause leaked to userMessage")
        assertFalse(e.userMessage.isBlank())
    }

    @Test
    fun unknownError_code006() {
        val e = SyncError.UnknownError("weird internal state")
        assertEquals("SYNC_006", e.code)
        assertFalse(e.userMessage.contains("weird"), "Internal cause leaked to userMessage")
        assertFalse(e.userMessage.isBlank())
    }

    @Test
    fun internalMessage_containsCause() {
        // Internal message IS verbose — that's the point
        val e = SyncError.StudentFetchFailed("connection refused")
        assert(e.internalMessage.contains("connection refused"))
    }
}
