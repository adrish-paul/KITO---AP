package com.kito.sap

import com.fleeksoft.ksoup.Ksoup
import com.kito.core.platform.AppConfig
import com.kito.core.platform.ErrorSanitizer
import com.kito.core.platform.createHttpEngine
import com.kito.sap.sensitive.SapError
import com.kito.sap.sensitive.SapPortalHeaders
import com.kito.sap.sensitive.SapPortalHtmlParser
import com.kito.sap.sensitive.SapPortalParams
import com.kito.sap.sensitive.SapPortalTokenExtractor
import com.kito.sap.sensitive.SapPortalUrls
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import io.ktor.http.decodeURLPart
import io.ktor.http.encodeURLQueryComponent
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.time.TimeSource

class SapPortalClient {

    private val cookieStorage = ClearableCookiesStorage()

    private val client: HttpClient by lazy {
        HttpClient(createHttpEngine()) {
            install(HttpCookies) {
                storage = cookieStorage
            }
            install(HttpRedirect) {
                checkHttpMethod = false
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30000
                connectTimeoutMillis = 30000
                socketTimeoutMillis = 30000
            }
            install(DefaultRequest) {
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36")
                header("sec-ch-ua", "\"Google Chrome\";v=\"141\", \"Not?A_Brand\";v=\"8\", \"Chromium\";v=\"141\"")
                header("sec-ch-ua-mobile", "?0")
                header("sec-ch-ua-platform", "\"Windows\"")
                header("sec-fetch-dest", "document")
                header("sec-fetch-mode", "navigate")
                header("sec-fetch-site", "same-origin")
                header("sec-fetch-user", "?1")
                header("upgrade-insecure-requests", "1")
                header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                header("Accept-Language", "en-US,en;q=0.9")
                header("Cache-Control", "no-cache")
                header("Pragma", "no-cache")
                header("DNT", "1")
            }
        }
    }

    suspend fun fetchAttendance(
        username: String,
        password: String,
        academicYear: String = "",
        termCode: String = ""
    ): AttendanceResult = withContext(Dispatchers.Default) {
        val totalStart = TimeSource.Monotonic.markNow()
        println("🚀 Starting fetchAttendance...")
        cookieStorage.clear()

        try {
            // ─── Step 1: Load login page ──────────────────────────────────────────────
            val step1Start = TimeSource.Monotonic.markNow()
            val loginPageResponse = client.get(SapPortalUrls.getLoginPageUrl())

            if (!loginPageResponse.status.isSuccess()) {
                val error = SapError.LoginPageFailed(loginPageResponse.status.value)
                ErrorSanitizer.log(error)
                return@withContext AttendanceResult.Error(ErrorSanitizer.sanitize(error))
            }

            val loginPageHtml = loginPageResponse.bodyAsText()
            val salt = SapPortalHtmlParser.extractSaltFromLoginPage(loginPageHtml)
            println("⏱️ Step 1 (Load Login) took: ${step1Start.elapsedNow()}")

            if (salt.isNullOrEmpty()) {
                val error = SapError.SaltExtractionFailed()
                ErrorSanitizer.log(error)
                return@withContext AttendanceResult.Error(ErrorSanitizer.sanitize(error))
            }

            // ─── Step 2: Submit login ─────────────────────────────────────────────────
            val step2Start = TimeSource.Monotonic.markNow()
            val loginParams = SapPortalParams.getLoginParams(salt, username, password)
            val loginResponse = client.submitForm(
                url = SapPortalUrls.getLoginPageUrl(),
                formParameters = Parameters.build {
                    loginParams.forEach { (key, value) -> append(key, value) }
                }
            ) {
                header("content-type", "application/x-www-form-urlencoded")
            }

            if (!loginResponse.status.isSuccess()) {
                val error = SapError.LoginFailed(
                    status = loginResponse.status.value,
                    preview = loginResponse.bodyAsText().take(200)
                )
                ErrorSanitizer.log(error)
                return@withContext AttendanceResult.Error(ErrorSanitizer.sanitize(error))
            }

            secureWipe(username.toCharArray())
            secureWipe(password.toCharArray())

            val loginResultHtml = loginResponse.bodyAsText()
            println("⏱️ Step 2 (Submit Login) took: ${step2Start.elapsedNow()}")

            if (loginResultHtml.contains("authentication failed", true)) {
                val error = SapError.InvalidCredentials()
                ErrorSanitizer.log(error)
                return@withContext AttendanceResult.Error(ErrorSanitizer.sanitize(error))
            }

            // ─── Step 3: Navigation events ────────────────────────────────────────────
            val step3Start = TimeSource.Monotonic.markNow()

            val navEvent1Response = client.submitForm(
                url = SapPortalUrls.getNavEvent1Url(),
                formParameters = Parameters.build {
                    SapPortalParams.getNavEvent1Params().forEach { (key, value) -> append(key, value) }
                }
            ) {
                header("content-type", "application/x-www-form-urlencoded")
                header("referer", SapPortalUrls.getLoginPageUrl())
                header("sec-fetch-dest", "iframe")
                header("sec-fetch-mode", "navigate")
                header("sec-fetch-site", "same-origin")
            }

            if (!navEvent1Response.status.isSuccess()) {
                val error = SapError.NavigationFailed(step = 1, status = navEvent1Response.status.value)
                ErrorSanitizer.log(error)
                return@withContext AttendanceResult.Error(ErrorSanitizer.sanitize(error))
            }

            val navEvent2Response = client.submitForm(
                url = SapPortalUrls.getNavEvent2Url(),
                formParameters = Parameters.build {
                    SapPortalParams.getNavEvent2Params().forEach { (key, value) -> append(key, value) }
                }
            ) {
                header("content-type", "application/x-www-form-urlencoded")
                header("referer", SapPortalUrls.getLoginPageUrl())
                header("sec-fetch-dest", "iframe")
                header("sec-fetch-mode", "navigate")
                header("sec-fetch-site", "same-origin")
            }

            if (!navEvent2Response.status.isSuccess()) {
                val error = SapError.NavigationFailed(step = 2, status = navEvent2Response.status.value)
                ErrorSanitizer.log(error)
                return@withContext AttendanceResult.Error(ErrorSanitizer.sanitize(error))
            }

            val navEvent3Response = client.submitForm(
                url = SapPortalUrls.getNavEvent3Url(),
                formParameters = Parameters.build {
                    SapPortalParams.getNavEvent3Params().forEach { (key, value) -> append(key, value) }
                }
            ) {
                header("content-type", "application/x-www-form-urlencoded")
                header("referer", SapPortalUrls.getLoginPageUrl())
                header("sec-fetch-dest", "iframe")
                header("sec-fetch-mode", "navigate")
                header("sec-fetch-site", "same-origin")
            }

            if (!navEvent3Response.status.isSuccess()) {
                val error = SapError.NavigationFailed(step = 3, status = navEvent3Response.status.value)
                ErrorSanitizer.log(error)
                return@withContext AttendanceResult.Error(ErrorSanitizer.sanitize(error))
            }

            val nav3Html = navEvent3Response.bodyAsText()
            println("⏱️ Step 3 (Nav Events) took: ${step3Start.elapsedNow()}")

            // ─── Step 4: Extract Web Dynpro form action ───────────────────────────────
            val wdFormAction = SapPortalHtmlParser.extractWebDynproFormAction(nav3Html)

            if (wdFormAction.isNullOrEmpty()) {
                val error = SapError.WebDynproFormActionFailed()
                ErrorSanitizer.log(error)
                return@withContext AttendanceResult.Error(ErrorSanitizer.sanitize(error))
            }

            val sapExtSid = SapPortalTokenExtractor.extractSapExtSid(wdFormAction)

            if (sapExtSid.isNullOrEmpty()) {
                val error = SapError.ExtSidExtractionFailed(wdFormAction)
                ErrorSanitizer.log(error)
                return@withContext AttendanceResult.Error(ErrorSanitizer.sanitize(error))
            }

            // ─── Step 5: Submit Web Dynpro form ───────────────────────────────────────
            val step5Start = TimeSource.Monotonic.markNow()
            val formData = SapPortalHtmlParser.extractFormFields(nav3Html)

            val wdInitialResponse = client.submitForm(
                url = wdFormAction,
                formParameters = Parameters.build {
                    formData.forEach { (key, value) -> append(key, value) }
                }
            ) {
                headers {
                    SapPortalHeaders.webDynproHeaders.forEach { (key, value) ->
                        set(key, value)
                    }
                }
            }

            if (!wdInitialResponse.status.isSuccess()) {
                val error = SapError.WebDynproSubmitFailed(wdInitialResponse.status.value)
                ErrorSanitizer.log(error)
                return@withContext AttendanceResult.Error(ErrorSanitizer.sanitize(error))
            }

            val wdResponseHtml = wdInitialResponse.bodyAsText()
            val responseUrl = wdInitialResponse.call.request.url.toString()
            println("⏱️ Step 5 (WebDynpro Init) took: ${step5Start.elapsedNow()}")

            // ─── Step 6: Extract session tokens ───────────────────────────────────────
            val wdContextId = SapPortalTokenExtractor.extractContextId(wdResponseHtml, responseUrl)
            val secureId = SapPortalTokenExtractor.extractSecureId(wdResponseHtml)

            val sapClientForm = Ksoup.parse(wdResponseHtml).selectFirst(SapPortalHeaders.sapClientFormSelector)
            val formAction = sapClientForm?.attr("action")

            val (extSidFromForm, contextIdFromForm) = SapPortalTokenExtractor.extractTokensFromFormAction(formAction)

            val finalExtSid = extSidFromForm ?: sapExtSid
            var finalContextId = contextIdFromForm ?: wdContextId

            if (finalContextId == wdContextId) {
                val urlContextIdMatch = Regex("""[?&]sap-contextid=([^&]+)""", RegexOption.IGNORE_CASE).find(responseUrl)
                if (urlContextIdMatch != null && urlContextIdMatch.groupValues.size > 1) {
                    val urlContextId = urlContextIdMatch.groupValues[1].decodeURLPart()
                    if (urlContextId.isNotEmpty()) {
                        finalContextId = urlContextId
                    }
                }
            }

            if (finalExtSid.isEmpty() || finalContextId.isEmpty() || secureId.isEmpty()) {
                val missing = buildList {
                    if (finalExtSid.isEmpty()) add("ext-sid")
                    if (finalContextId.isEmpty()) add("context-id")
                    if (secureId.isEmpty()) add("secure-id")
                }
                val error = SapError.TokenExtractionFailed(missing)
                ErrorSanitizer.log(error)
                return@withContext AttendanceResult.Error(ErrorSanitizer.sanitize(error))
            }

            // ─── Step 7: Initial attendance load ─────────────────────────────────────
            val step7Start = TimeSource.Monotonic.markNow()
            val initialUrl = SapPortalUrls.getInitialAttendanceUrl(finalExtSid, finalContextId)
            val initialBody = SapPortalParams.getInitialAttendanceBody(secureId, finalContextId)

            val initialResponse = client.submitForm(
                url = initialUrl,
                formParameters = Parameters.build {
                    initialBody.forEach { (key, value) -> append(key, value) }
                }
            ) {
                headers {
                    SapPortalHeaders.getInitialHeaders().forEach { (key, value) ->
                        set(key, value)
                    }
                }
            }

            if (!initialResponse.status.isSuccess()) {
                val error = SapError.AttendanceInitFailed(
                    status = initialResponse.status.value,
                    preview = initialResponse.bodyAsText().take(200)
                )
                ErrorSanitizer.log(error)
                return@withContext AttendanceResult.Error(ErrorSanitizer.sanitize(error))
            }

            val initialResponseBody = initialResponse.bodyAsText()
            println("⏱️ Step 7 (Init Attendance) took: ${step7Start.elapsedNow()}")

            // ─── Step 8: Detect academic year and term ────────────────────────────────
            var htmlToParse = wdResponseHtml
            val cdataMatch = SapPortalHeaders.contentUpdateRegex.find(initialResponseBody)
            if (cdataMatch != null) {
                htmlToParse = cdataMatch.groupValues[1]
            } else if (initialResponseBody.length > wdResponseHtml.length) {
                htmlToParse = initialResponseBody
            }

            val (academicYearValue, termCodeValue) = SapPortalHtmlParser.detectAcademicYearAndTerm(
                htmlToParse, academicYear, termCode
            )

            // ─── Step 9: Fetch attendance with selection ──────────────────────────────
            val step9Start = TimeSource.Monotonic.markNow()
            val attendanceBody = SapPortalParams
                .getAttendanceBodyWithSelection(secureId, academicYearValue, termCodeValue)
                .toMutableMap()

            val encodedExtSid = finalExtSid.replace("*", "*").replace("-", "--").encodeURLQueryComponent()
            val sapeventQueue = attendanceBody["SAPEVENTQUEUE"]
                ?.replace("PLACEHOLDER_EXT_SID", encodedExtSid) ?: ""
            attendanceBody["SAPEVENTQUEUE"] = sapeventQueue

            val attendanceResponse = client.submitForm(
                url = initialUrl,
                formParameters = Parameters.build {
                    attendanceBody.forEach { (key, value) -> append(key, value) }
                }
            ) {
                headers {
                    SapPortalHeaders.getInitialHeaders().forEach { (key, value) ->
                        append(key, value)
                    }
                }
            }

            if (!attendanceResponse.status.isSuccess()) {
                val error = SapError.AttendanceFetchFailed(
                    status = attendanceResponse.status.value,
                    preview = attendanceResponse.bodyAsText().take(200)
                )
                ErrorSanitizer.log(error)
                return@withContext AttendanceResult.Error(ErrorSanitizer.sanitize(error))
            }

            val attendanceHtml = attendanceResponse.bodyAsText()
            println("⏱️ Step 9 (Fetch Attendance) took: ${step9Start.elapsedNow()}")

            // ─── Step 10: Parse attendance data ───────────────────────────────────────
            val step10Start = TimeSource.Monotonic.markNow()
            val parsedAttendance = AttendanceData(SapPortalHtmlParser.parseAttendanceData(attendanceHtml))
            println("⏱️ Step 10 (Parse Data) took: ${step10Start.elapsedNow()}")

            // Fire-and-forget logout — intentionally not awaited so it doesn't block the result.
            // GlobalScope is acceptable here since this is a best-effort cleanup task.
            GlobalScope.launch(Dispatchers.Default) {
                performLogout(client)
            }

            println("✅ Total fetch time: ${totalStart.elapsedNow()}")
            return@withContext AttendanceResult.Success(parsedAttendance)

        } catch (e: Exception) {
            GlobalScope.launch(Dispatchers.Default) {
                try { performLogout(client) } catch (_: Exception) {}
            }

            val error = when {
                e.message?.contains("Unable to resolve host") == true ->
                    SapError.NetworkError(e.message ?: "Unable to resolve host")
                else ->
                    SapError.UnknownError(e.message ?: e::class.simpleName ?: "unknown")
            }
            ErrorSanitizer.log(error)
            // Always print full stack trace in debug for crash diagnostics
            if (AppConfig.isDebug) e.printStackTrace()
            return@withContext AttendanceResult.Error(ErrorSanitizer.sanitize(error))
        }
    }

    private fun secureWipe(charArray: CharArray) {
        for (i in charArray.indices) {
            charArray[i] = '0'
        }
    }

    private suspend fun performLogout(client: HttpClient) {
        try {
            withTimeout(2000) {
                try {
                    client.submitForm(
                        url = SapPortalUrls.getLogoutUrl(),
                        formParameters = Parameters.build {
                            append("logout_submit", "true")
                        }
                    ) {
                        header("content-type", "application/x-www-form-urlencoded")
                        header("origin", AppConfig.portalBase)
                        header("referer", SapPortalUrls.getLoginPageUrl())
                    }
                } catch (e: Exception) {
                    if (AppConfig.isDebug && !e.message.toString().contains("Unable to resolve host")) {
                        println("⚠️ Logout error: ${e.message}")
                    }
                }
            }
        } catch (_: Exception) {
            // Timeout during logout — safe to swallow
        }
    }
}

// ─── Result types ─────────────────────────────────────────────────────────────

sealed class AttendanceResult {
    data class Success(val data: AttendanceData) : AttendanceResult()
    data class Error(val message: String) : AttendanceResult()
}

data class AttendanceData(
    val subjects: List<SubjectAttendance> = emptyList()
)

data class SubjectAttendance(
    val subjectCode: String,
    val subjectName: String,
    val attendedClasses: Int,
    val totalClasses: Int,
    val percentage: Double,
    val facultyName: String = ""
)

// ─── Cookie storage ───────────────────────────────────────────────────────────

/**
 * Simple in-memory cookie storage with clear support.
 */
class ClearableCookiesStorage : io.ktor.client.plugins.cookies.CookiesStorage {
    private val cookies = mutableListOf<io.ktor.http.Cookie>()
    private val mutex = kotlinx.coroutines.sync.Mutex()

    override suspend fun get(requestUrl: io.ktor.http.Url): List<io.ktor.http.Cookie> =
        mutex.withLock { cookies.filter { it.matches(requestUrl) } }

    override suspend fun addCookie(requestUrl: io.ktor.http.Url, cookie: io.ktor.http.Cookie): Unit =
        mutex.withLock {
            cookies.removeAll { it.name == cookie.name && it.matches(requestUrl) }
            cookies.add(cookie)
        }

    override fun close() {}

    suspend fun clear() = mutex.withLock { cookies.clear() }

    private fun io.ktor.http.Cookie.matches(requestUrl: io.ktor.http.Url): Boolean {
        val domain = this.domain ?: return true
        if (!requestUrl.host.endsWith(domain) && requestUrl.host != domain) return false
        val path = this.path ?: "/"
        if (!requestUrl.encodedPath.startsWith(path)) return false
        if (this.secure && requestUrl.protocol.name != "https") return false
        return true
    }
}