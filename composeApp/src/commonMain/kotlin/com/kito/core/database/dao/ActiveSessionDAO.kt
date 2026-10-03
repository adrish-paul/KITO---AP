package com.kito.core.database.dao

import androidx.room.Dao
import androidx.room.Upsert
import androidx.room.Query
import com.kito.core.database.entity.ActiveSessionEntity

@Dao
interface ActiveSessionDAO {
    @Query("SELECT * FROM ActiveSessionEntity WHERE id = 1")
    suspend fun getActiveSession(): ActiveSessionEntity?
    @Upsert
    suspend fun insertActiveSession(activeSession: ActiveSessionEntity)
}
