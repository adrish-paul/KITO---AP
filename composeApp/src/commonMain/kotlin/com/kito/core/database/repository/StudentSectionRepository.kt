package com.kito.core.database.repository

import com.kito.core.database.dao.StudentSectionDAO
import com.kito.core.database.entity.StudentSectionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import com.kito.kaya.KayaTimetableStore
class StudentSectionRepository(
    private val studentSectionDao: StudentSectionDAO,
    private val kayaStore: KayaTimetableStore,
) {
    fun getScheduleForStudent(rollNo: String, day: String): Flow<List<StudentSectionEntity>> =
        getAllScheduleForStudent(rollNo).map { rows -> rows.filter { it.day == day } }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllScheduleForStudent(rollNo: String): Flow<List<StudentSectionEntity>> =
        kayaStore.observe(rollNo).distinctUntilChanged().flatMapLatest { kaya ->
            if (kaya != null) flowOf(kaya) else studentSectionDao.getAllScheduleForStudent(rollNo)
        }
}
