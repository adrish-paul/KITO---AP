package com.kito.core.database.dao

import androidx.room.Dao
import androidx.room.Upsert
import androidx.room.Query
import com.kito.core.database.entity.StudentElectiveEntity
import org.koin.core.annotation.Provided

@Provided
@Dao
interface StudentElectiveDao {
    @Query("DELETE FROM StudentElectiveEntity WHERE roll_no = :roll")
    suspend fun deleteForStudent(roll: String)
    @Upsert
    suspend fun upsertStudentElective(entity: StudentElectiveEntity)
}
