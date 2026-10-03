package com.kito.core.sync.data

import com.kito.core.database.entity.SectionEntity
import com.kito.core.database.entity.StudentEntity
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.request.parameter

import org.koin.core.annotation.Provided

class SyncRemoteDataSource(
    @Provided private val client: HttpClient
) {
    suspend fun getStudentByRoll(rollNo: String): StudentEntity? {
        return client.get("rest/v1/students") {
            expectSuccess = true
            parameter("roll_no", "eq.$rollNo")
            parameter("select", "*")
        }.body<List<StudentEntity>>().firstOrNull()
    }

    suspend fun getActiveSessionConfig(): ActiveSessionConfig {
        val result: List<ActiveSessionConfig> = client.get("rest/v1/active_session") {
            expectSuccess = true
            parameter("select", "*")
        }.body()

        if (result.isEmpty()) {
            throw IllegalStateException("Active session config not found in Supabase")
        }

        return result.first()
    }

    suspend fun getTimetableForStudent(
        section: String,
        batch: String,
    ): List<SectionEntity> {
        return client.get("rest/v1/timetable") {
            expectSuccess = true
            parameter("section", "eq.$section")
            parameter("batch", "eq.$batch")
            parameter("select", "*")
        }.body()
    }

    suspend fun getStudentElective(rollNo: String): StudentElectiveConfig? {
        val result: List<StudentElectiveConfig> = client.get("rest/v1/student_elective") {
            expectSuccess = true
            parameter("roll_no", "eq.$rollNo")
            parameter("select", "*")
        }.body()
        return result.firstOrNull()
    }
}
