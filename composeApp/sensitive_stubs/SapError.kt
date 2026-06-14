package com.kito.sap.sensitive

sealed class SapError(
    val internalMessage: String,
    val userMessage: String,
    val code: String
) {
    // ── Auth ────────────────────────────────────────────────────────────────

    class LoginPageFailed(val status: Int) : SapError(
        internalMessage = "Failed to fetch page ($status)",
        userMessage = "Could not reach the portal. Please check your connection.",
        code = "SAP_001"
    )

    class SaltExtractionFailed : SapError(
        internalMessage = "Failed to parse authentication response",
        userMessage = "Portal response was unexpected. Try again later.",
        code = "SAP_002"
    )

    class LoginFailed(val status: Int, val preview: String) : SapError(
        internalMessage = "Authentication request failed ($status)",
        userMessage = "Login failed. Please try again.",
        code = "SAP_003"
    )

    class InvalidCredentials : SapError(
        internalMessage = "Authentication rejected",
        userMessage = "Invalid username or password.",
        code = "SAP_004"
    )

    // ── Navigation ──────────────────────────────────────────────────────────

    class NavigationFailed(val step: Int, val status: Int) : SapError(
        internalMessage = "Transition $step failed ($status)",
        userMessage = "Portal navigation failed. Please try again.",
        code = NAV_CODES.getOrElse(step) { "SAP_005" }
    ) {
        companion object {
            private val NAV_CODES = mapOf(
                1 to "SAP_005",
                2 to "SAP_006",
                3 to "SAP_007"
            )
        }
    }

    // ── WebDynpro / Session ─────────────────────────────────────────────────

    class WebDynproFormActionFailed : SapError(
        internalMessage = "Failed to resolve session parameters",
        userMessage = "Portal session could not be established. Please try again.",
        code = "SAP_008"
    )

    class ExtSidExtractionFailed(arg: String) : SapError(
        internalMessage = "Failed to process session context",
        userMessage = "Portal session could not be established. Please try again.",
        code = "SAP_009"
    )

    class WebDynproSubmitFailed(status: Int) : SapError(
        internalMessage = "Session initiation failed ($status)",
        userMessage = "Portal session setup failed. Please try again.",
        code = "SAP_010"
    )

    class TokenExtractionFailed(arg: List<String>) : SapError(
        internalMessage = "Required parameters are missing: $arg",
        userMessage = "Session setup failed. Please try again.",
        code = "SAP_011"
    )

    // ── Attendance ──────────────────────────────────────────────────────────

    class AttendanceInitFailed(val status: Int, val preview: String) : SapError(
        internalMessage = "Failed to initialize query ($status)",
        userMessage = "Could not load attendance data. Please try again.",
        code = "SAP_012"
    )

    class AttendanceFetchFailed(val status: Int, val preview: String) : SapError(
        internalMessage = "Failed to execute query ($status)",
        userMessage = "Could not load attendance data. Try again later.",
        code = "SAP_013"
    )

    class AttendanceParseEmpty : SapError(
        internalMessage = "Query returned no results",
        userMessage = "No attendance data found for the selected term.",
        code = "SAP_014"
    )

    // ── Network / Unknown ───────────────────────────────────────────────────

    class NetworkError(arg: String) : SapError(
        internalMessage = "Network failure: $arg",
        userMessage = "Network error. Please check your connection and try again.",
        code = "SAP_015"
    )

    class UnknownError(arg: String) : SapError(
        internalMessage = "Unknown error: $arg",
        userMessage = "Something went wrong. Please try again.",
        code = "SAP_016"
    )
}
