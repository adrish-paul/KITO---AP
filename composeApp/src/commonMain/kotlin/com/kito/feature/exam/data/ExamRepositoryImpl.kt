package com.kito.feature.exam.data

import com.kito.core.network.supabase.model.MidsemScheduleModel
import com.kito.core.network.supabase.request.MidsemScheduleRequest
import com.kito.feature.exam.data.mapper.toDomain
import com.kito.feature.exam.domain.model.ExamSchedule
import com.kito.feature.exam.domain.repository.ExamRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

import org.koin.core.annotation.Provided

class ExamRepositoryImpl(
    @Provided private val client: HttpClient,
) : ExamRepository {
    override suspend fun getExamSchedule(roll: String): List<ExamSchedule> {
        val response = client.post("rest/v1/rpc/get_midsem_schedule_by_roll") {
            contentType(ContentType.Application.Json)
            setBody(MidsemScheduleRequest(p_roll_no = roll))
        }.body<List<MidsemScheduleModel>>()
        return response.map { it.toDomain() }
    }
}
