package com.kito.feature.friendview.data

import com.kito.core.database.entity.SectionEntity
import com.kito.core.database.entity.StudentEntity
import com.kito.feature.friendview.data.mapper.toDomain
import com.kito.feature.friendview.domain.model.FriendScheduleItem
import com.kito.feature.friendview.domain.repository.FriendViewRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

import org.koin.core.annotation.Provided

class FriendViewRepositoryImpl(
    @Provided private val client: HttpClient,
) : FriendViewRepository {
    override suspend fun getFriendSchedule(roll: String): List<FriendScheduleItem> {
        val result: List<StudentEntity> = client.get("rest/v1/students") {
            parameter("roll_no", "eq.$roll")
            parameter("select", "*")
        }.body()

        if (result.isEmpty()) return emptyList()
        val student = result.first()
        if (student.section.isBlank()) return emptyList()

        val timetable = client.get("rest/v1/timetable") {
            parameter("section", "eq.${student.section}")
            parameter("batch", "eq.${student.batch}")
            parameter("select", "*")
        }.body<List<SectionEntity>>()

        return timetable.map { it.toDomain() }
    }
}
