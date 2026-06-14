package com.kito.core.sync.data

import com.kito.core.database.entity.SectionEntity
import com.kito.core.database.entity.StudentEntity
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

import org.koin.core.annotation.Provided

class SyncRemoteDataSource(
    @Provided private val client: HttpClient
) {
    suspend fun getStudentByRoll(rollNo: String): StudentEntity {
        val result: List<StudentEntity> = client.get("rest/v1/students") {
            parameter("roll_no", "eq.$rollNo")
            parameter("select", "*")
        }.body()

        if (result.isEmpty()) {
            throw IllegalStateException("Student not found in Supabase")
        }

        return result.first()
    }

    suspend fun getTimetableForStudent(
        section: String,
        batch: String
    ): List<SectionEntity> {
        return client.get("rest/v1/timetable") {
            parameter("section", "eq.$section")
            parameter("batch", "eq.$batch")
            parameter("select", "*")
        }.body()
    }
}
