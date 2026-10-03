package com.kito.core.sync.domain.usecase

import com.kito.core.auth.domain.repository.CredentialsRepository
import com.kito.core.datastore.domain.repository.PrefsRepository
import com.kito.core.sync.domain.SyncUseCase
import com.kito.feature.attendance.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.first

class ChangeYearTermUseCase(
    private val prefs: PrefsRepository,
    private val credentialsRepository: CredentialsRepository,
    private val attendanceRepository: AttendanceRepository,
    private val syncUseCase: SyncUseCase
) {
    suspend operator fun invoke(year: String, term: String): Result<Unit> {
        prefs.setAcademicYear(year)
        prefs.setTermCode(term)
        attendanceRepository.deleteAllAttendance()
        
        val roll = prefs.userRollFlow.first()
        val sapPassword = credentialsRepository.getSapPassword()
        
        return syncUseCase.syncAttendance(
            roll = roll,
            sapPassword = sapPassword,
            year = year,
            term = term
        )
    }
}
