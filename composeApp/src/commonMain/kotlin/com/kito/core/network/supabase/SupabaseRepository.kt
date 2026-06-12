package com.kito.core.network.supabase


import com.kito.core.database.entity.SectionEntity
import com.kito.core.database.entity.StudentEntity
import com.kito.core.network.supabase.model.CalendarEventModel
import com.kito.core.network.supabase.model.EventAndAdModel
import com.kito.core.network.supabase.model.FeatureFlagModel
import com.kito.core.network.supabase.model.LatestAppVersionModel
import com.kito.core.network.supabase.model.MidsemScheduleModel
import com.kito.core.network.supabase.model.PlatformClass
import com.kito.core.network.supabase.model.TeacherFuzzySearchModel
import com.kito.core.network.supabase.model.TeacherModel
import com.kito.core.network.supabase.model.TeacherScheduleByIDModel
import com.kito.core.network.supabase.request.MidsemScheduleRequest
import com.kito.core.network.supabase.request.RecruitmentClickRequest
import com.kito.core.network.supabase.request.TeacherScheduleByIDRequest
import com.kito.core.network.supabase.request.TeacherSearchRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.koin.core.annotation.Provided

class SupabaseRepository(
    @Provided private val client: HttpClient
) {

    suspend fun getStudents(): List<StudentEntity> {
        return client.get("rest/v1/students").body()
    }

    suspend fun getSection(): List<SectionEntity> {
        return client.get("rest/v1/timetable").body()
    }

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

    suspend fun getAllTeacherDetail(): List<TeacherModel> {
        return client.get("rest/v1/v_teachers_with_details").body()
    }

    suspend fun getTeacherSearchResponse(query: String): List<TeacherFuzzySearchModel> {
        return client.post("rest/v1/rpc/search_teachers_fuzzy") {
            contentType(ContentType.Application.Json)
            setBody(TeacherSearchRequest(p_query = query))
        }.body()
    }

    suspend fun getTeacherScheduleById(teacherId: Long): List<TeacherScheduleByIDModel> {
        return client.post("rest/v1/rpc/get_teacher_schedule_by_id") {
            contentType(ContentType.Application.Json)
            setBody(TeacherScheduleByIDRequest(p_teacher_id = teacherId))
        }.body()
    }

    suspend fun getTeacherDetailByID(teacherId: Long): List<TeacherModel>{
        return client.get("rest/v1/v_teachers_with_details") {
            parameter("teacher_id", "eq.$teacherId")
        }.body()
    }

    suspend fun getMidSemSchedule(rollNo: String): List<MidsemScheduleModel> {
        return client.post("rest/v1/rpc/get_midsem_schedule_by_roll") {
            contentType(ContentType.Application.Json)
            setBody(MidsemScheduleRequest(p_roll_no = rollNo))
        }.body()
    }

    suspend fun getEventsAndAds(): List<EventAndAdModel> {
        return client.get("rest/v1/events_and_ads") {
            parameter("is_active", "eq.true")
            parameter("order", "display_order.asc")
        }.body()
    }

    suspend fun getLatestAppVersion(platform: PlatformClass): List<LatestAppVersionModel> {
        return client.get("rest/v1/app_version") {
            parameter("platform","eq.${platform.value}")
            parameter("select","*")
        }.body()
    }

    suspend fun postRecruitmentClick(rollNo: String){
        client.post("rest/v1/recruitment_clicks") {
            setBody(
                RecruitmentClickRequest(
                    roll_number = rollNo
                )
            )
        }
    }

    suspend fun getCalendarEventsByMonth(year: Int, month: Int): List<CalendarEventModel> {
        val monthStr = "${year}-${month.toString().padStart(2, '0')}"
        return client.get("rest/v1/calendar_events") {
            parameter("date", "gte.${monthStr}-01")
            parameter("date", "lte.${monthStr}-31")
            parameter("is_active", "eq.true")
            parameter("order", "date.asc,start_time.asc")
            parameter("select", "*")
        }.body()
    }

    suspend fun getAllCalendarEvents(): List<CalendarEventModel> {
        return client.get("rest/v1/calendar_events") {
            parameter("is_active", "eq.true")
            parameter("order", "date.asc,start_time.asc")
            parameter("select", "*")
        }.body()
    }

    suspend fun getFeatureFlag(): List<FeatureFlagModel>{
        return client.get("rest/v1/feature_flag").body()
    }
}
