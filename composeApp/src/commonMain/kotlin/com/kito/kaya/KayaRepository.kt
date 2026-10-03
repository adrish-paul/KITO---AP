package com.kito.kaya

import com.kito.core.database.entity.SectionEntity
import com.kito.kaya.sensitive.KayaPortalClient
import com.kito.core.platform.SecureStorage
import com.kito.core.platform.AppSyncTrigger
import com.kito.core.database.dao.StudentSectionDAO
import com.kito.core.sync.domain.SupabaseTimetableSync
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Provided

sealed class KayaResult {
    data class Success(val sections: List<SectionEntity>) : KayaResult()
    data class Error(val message: String) : KayaResult()
}

/** Owns the personal KAYA connection, secure credential and offline timetable. */
class KayaRepository(
    private val kayaClient: KayaPortalClient,
    private val store: KayaTimetableStore,
    @Provided private val secureStorage: SecureStorage,
    @Provided private val syncTrigger: AppSyncTrigger,
    @Provided private val studentSectionDao: StudentSectionDAO,
    private val timetableSync: SupabaseTimetableSync,
) {
    private val mutex = Mutex()
    val isConnected = store.isConnected

    suspend fun connect(roll: String, password: String): KayaResult = mutex.withLock {
        if (roll.isBlank() || password.isBlank()) return@withLock KayaResult.Error("Enter your KAYA password after setting your roll number.")
        val result = fetchTimetable(roll, password)
        if (result is KayaResult.Success) {
            try {
                check(secureStorage.saveKayaPassword(password)) { "Could not securely save your KAYA password." }
                store.save(roll, result.sections)
                notifySchedule(roll, result.sections)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                return@withLock KayaResult.Error("Could not save your KAYA connection. Please try again.")
            }
        }
        result
    }

    suspend fun refresh(roll: String): KayaResult = mutex.withLock {
        if (!store.isConnected.first() || store.observe(roll).first() == null) {
            return@withLock KayaResult.Error("Please reconnect to KAYA in Settings.")
        }
        val password = secureStorage.getKayaPassword()
        if (password.isBlank()) return@withLock KayaResult.Error("Please reconnect to KAYA in Settings.")
        val result = fetchTimetable(roll, password)
        if (result is KayaResult.Success) {
            store.save(roll, result.sections)
            notifySchedule(roll, result.sections)
        }
        result
    }

    suspend fun disconnect(roll: String): Result<Unit> = mutex.withLock {
        check(secureStorage.clearKayaPassword()) { "Could not remove the KAYA password." }
        store.clear()
        val refreshResult = try {
            timetableSync.sync(roll)
            Result.success(Unit)
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { Result.failure<Unit>(e) }
        try {
            syncTrigger.onSyncComplete(roll, studentSectionDao.getAllScheduleForStudent(roll).first())
        } catch (e: CancellationException) { throw e } catch (_: Exception) { /* UI already switched. */ }
        refreshResult
    }

    private suspend fun notifySchedule(roll: String, rows: List<SectionEntity>) {
        try {
            syncTrigger.onSyncComplete(roll, rows.map { it.toStudentSection(roll) })
        } catch (e: CancellationException) { throw e } catch (_: Exception) { /* Keep the saved timetable. */ }
    }

    suspend fun fetchTimetable(username: String, password: String): KayaResult =
        try {
            kayaClient.fetchTimetable(username, password)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            KayaResult.Error("Could not reach KAYA. Please try again.")
        }
}
