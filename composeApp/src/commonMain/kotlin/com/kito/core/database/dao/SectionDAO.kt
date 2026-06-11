package com.kito.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.kito.core.database.entity.SectionEntity
import org.koin.core.annotation.Provided

@Provided
@Dao
interface SectionDAO {

    @Upsert
    suspend fun insertSection(sectionEntity: List<SectionEntity>)

    @Delete
    suspend fun deleteSection(sectionEntity: SectionEntity)

    @Query("DELETE FROM SectionEntity")
    suspend fun deleteAllSection()
}
