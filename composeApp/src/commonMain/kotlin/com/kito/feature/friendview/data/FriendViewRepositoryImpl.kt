package com.kito.feature.friendview.data

import com.kito.core.database.entity.SectionEntity
import com.kito.core.database.entity.StudentEntity
import com.kito.core.datastore.domain.repository.PrefsRepository
import com.kito.core.sync.data.ActiveSessionConfig
import com.kito.core.sync.data.StudentElectiveConfig
import com.kito.feature.friendview.data.mapper.toDomain
import com.kito.feature.friendview.domain.model.FriendScheduleItem
import com.kito.feature.friendview.domain.model.FriendSummary
import com.kito.feature.friendview.domain.repository.FriendViewRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Provided

class FriendViewRepositoryImpl(
    @Provided private val client: HttpClient,
    @Provided private val prefs: PrefsRepository,
) : FriendViewRepository {

    override suspend fun getFriendSummary(roll: String): FriendSummary {
        val cached = prefs.cachedFriendSummariesFlow.first()[roll]
        if (cached != null && !cached.isLoading && !cached.notFound) {
            return cached
        }
        if (roll.startsWith("SEC:")) {
            return cached ?: FriendSummary(roll = roll, notFound = true)
        }
        val summary = fetchRemoteFriendSummary(roll)
        val finalSummary = if (cached?.name?.isNotBlank() == true) {
            summary.copy(name = cached.name)
        } else {
            summary
        }
        prefs.saveCachedFriendSummary(finalSummary)
        return finalSummary
    }

    override suspend fun getFriendSchedule(roll: String): List<FriendScheduleItem> {
        val cached = prefs.cachedFriendSchedulesFlow.first()[roll]
        if (cached != null && cached.isNotEmpty()) {
            return cached
        }
        val remote = fetchRemoteFriendSchedule(roll)
        if (remote.isNotEmpty()) {
            prefs.saveCachedFriendSchedule(roll, remote)
        }
        return remote
    }

    override suspend fun syncFriend(roll: String): Result<Unit> = runCatching {
        val summary = getFriendSummary(roll)
        if (summary.notFound) return@runCatching
        val schedule = fetchRemoteFriendSchedule(roll)
        if (schedule.isNotEmpty()) {
            prefs.saveCachedFriendSchedule(roll, schedule)
        }
    }

    override suspend fun syncAllFriends(): Result<Unit> = runCatching {
        val rolls = prefs.friendRollsFlow.first()
        for (roll in rolls) {
            syncFriend(roll)
        }
    }

    override suspend fun fetchRemoteStudentSummary(roll: String): FriendSummary {
        val student = client.get("rest/v1/students") {
            parameter("roll_no", "eq.$roll")
            parameter("select", "*")
        }.body<List<StudentEntity>>().firstOrNull() ?: return FriendSummary(roll = roll, notFound = true)

        val elective = if (student.batch == "batch_3") {
            runCatching {
                client.get("rest/v1/student_elective") {
                    parameter("roll_no", "eq.$roll")
                    parameter("select", "*")
                }.body<List<StudentElectiveConfig>>().firstOrNull()
            }.getOrNull()
        } else null

        return FriendSummary(
            roll = roll,
            section = student.section,
            batch = student.batch,
            elective1 = elective?.elective_1.orEmpty(),
            elective2 = elective?.elective_2.orEmpty()
        )
    }

    private suspend fun fetchRemoteFriendSummary(roll: String): FriendSummary {
        return runCatching {
            fetchRemoteStudentSummary(roll)
        }.getOrElse {
            FriendSummary(roll = roll, notFound = true)
        }
    }

    private suspend fun fetchRemoteFriendSchedule(roll: String): List<FriendScheduleItem> {
        val cachedSummary = prefs.cachedFriendSummariesFlow.first()[roll]

        val student = if (!roll.startsWith("SEC:")) {
            runCatching {
                client.get("rest/v1/students") {
                    parameter("roll_no", "eq.$roll")
                    parameter("select", "*")
                }.body<List<StudentEntity>>().firstOrNull()
            }.getOrNull()
        } else null

        val section = student?.section ?: cachedSummary?.section.orEmpty()
        val batch = student?.batch ?: cachedSummary?.batch.orEmpty()

        if (section.isBlank()) return emptyList()

        val version = runCatching {
            client.get("rest/v1/active_session") {
                parameter("select", "*")
            }.body<List<ActiveSessionConfig>>().firstOrNull()?.version
        }.getOrNull() ?: 1

        val timetable = runCatching {
            client.get("rest/v1/timetable") {
                parameter("section", "eq.$section")
                parameter("batch", "eq.$batch")
                parameter("version", "eq.$version")
                parameter("select", "*")
            }.body<List<SectionEntity>>()
        }.getOrDefault(emptyList())

        val elective1 = if (student != null && student.batch == "batch_3") {
            runCatching {
                client.get("rest/v1/student_elective") {
                    parameter("roll_no", "eq.$roll")
                    parameter("select", "*")
                }.body<List<StudentElectiveConfig>>().firstOrNull()?.elective_1
            }.getOrNull().orEmpty()
        } else cachedSummary?.elective1.orEmpty()

        val elective2 = if (student != null && student.batch == "batch_3") {
            runCatching {
                client.get("rest/v1/student_elective") {
                    parameter("roll_no", "eq.$roll")
                    parameter("select", "*")
                }.body<List<StudentElectiveConfig>>().firstOrNull()?.elective_2
            }.getOrNull().orEmpty()
        } else cachedSummary?.elective2.orEmpty()

        val elective1Rows = if (elective1.isNotBlank()) {
            runCatching {
                client.get("rest/v1/timetable") {
                    parameter("section", "eq.$elective1")
                    parameter("batch", "eq.$batch")
                    parameter("version", "eq.$version")
                    parameter("select", "*")
                }.body<List<SectionEntity>>()
            }.getOrDefault(emptyList())
        } else emptyList()

        val elective2Rows = if (elective2.isNotBlank()) {
            runCatching {
                client.get("rest/v1/timetable") {
                    parameter("section", "eq.$elective2")
                    parameter("batch", "eq.$batch")
                    parameter("version", "eq.$version")
                    parameter("select", "*")
                }.body<List<SectionEntity>>()
            }.getOrDefault(emptyList())
        } else emptyList()

        return (timetable + elective1Rows + elective2Rows).map { it.toDomain() }
    }
}


