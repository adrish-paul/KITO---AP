package com.kito.feature.attendance.data

import com.kito.core.database.dao.AttendanceDAO
import com.kito.feature.attendance.data.mapper.toDomain
import com.kito.feature.attendance.domain.model.Attendance
import com.kito.feature.attendance.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Reuses the existing Room DAO as the data source and maps entities to domain models.
 * (Writes/sync still go through the legacy core AttendanceRepository until those features migrate.)
 */
class AttendanceRepositoryImpl(
    private val attendanceDao: AttendanceDAO,
) : AttendanceRepository {
    override fun observeAttendance(): Flow<List<Attendance>> =
        attendanceDao.getAllAttendance().map { entities -> entities.map { it.toDomain() } }

    override suspend fun deleteAllAttendance() = attendanceDao.deleteAllAttendance()
}
