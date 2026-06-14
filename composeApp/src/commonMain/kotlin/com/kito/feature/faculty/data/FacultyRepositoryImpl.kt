package com.kito.feature.faculty.data

import com.kito.feature.faculty.data.mapper.toDomain
import com.kito.feature.faculty.domain.model.Faculty
import com.kito.feature.faculty.domain.model.FacultyScheduleSlot
import com.kito.feature.faculty.domain.repository.FacultyRepository
import com.kito.core.network.supabase.model.TeacherModel
import com.kito.core.network.supabase.model.TeacherFuzzySearchModel
import com.kito.core.network.supabase.model.TeacherScheduleByIDModel
import com.kito.core.network.supabase.request.TeacherSearchRequest
import com.kito.core.network.supabase.request.TeacherScheduleByIDRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.contentType

import org.koin.core.annotation.Provided

class FacultyRepositoryImpl(
    @Provided private val client: HttpClient,
) : FacultyRepository {
    override suspend fun getAllFaculty(): List<Faculty> =
        client.get("rest/v1/v_teachers_with_details").body<List<TeacherModel>>().map { it.toDomain() }

    override suspend fun searchFaculty(query: String): List<Faculty> =
        client.post("rest/v1/rpc/search_teachers_fuzzy") {
            contentType(ContentType.Application.Json)
            setBody(TeacherSearchRequest(p_query = query))
        }.body<List<TeacherFuzzySearchModel>>().map { it.toDomain() }

    override suspend fun getFacultyById(id: Long): Faculty? =
        client.get("rest/v1/v_teachers_with_details") {
            parameter("teacher_id", "eq.$id")
        }.body<List<TeacherModel>>().firstOrNull()?.toDomain()

    override suspend fun getFacultySchedule(id: Long): List<FacultyScheduleSlot> =
        client.post("rest/v1/rpc/get_teacher_schedule_by_id") {
            contentType(ContentType.Application.Json)
            setBody(TeacherScheduleByIDRequest(p_teacher_id = id))
        }.body<List<TeacherScheduleByIDModel>>().map { it.toDomain() }
}
