package com.kito.sap.sensitive

import com.kito.sap.SubjectAttendance

object SapPortalHtmlParser {
    fun extractSaltFromLoginPage(arg: String?): String? = null
    fun extractWebDynproFormAction(arg: String?): String? = null
    fun extractFormFields(arg: String): Map<String, String> = emptyMap()
    fun detectAcademicYearAndTerm(arg1: String?, arg2: String, arg3: String): Pair<String, String> = Pair("", "")
    fun parseAttendanceData(arg: String): List<SubjectAttendance> = emptyList()
}
