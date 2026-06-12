package com.kito.core.sync.domain

interface SyncUseCase {
    suspend fun scheduleSync(roll: String): Result<Unit>
    suspend fun syncAll(roll: String, sapPassword: String, year: String, term: String): Result<Unit>
}
