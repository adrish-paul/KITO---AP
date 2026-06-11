package com.kito.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Provided

class PrefsRepository(
    @Provided private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_ACADEMIC_YEAR = stringPreferencesKey("academic_year")
        private val KEY_TERM_CODE = stringPreferencesKey("term_code")
        private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        private val KEY_USER_SETUP_DONE = booleanPreferencesKey("user_setup_done")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_ROLLNUMBER = stringPreferencesKey("User_Password")
        private val KEY_REQUIRED_ATTENDANCE = intPreferencesKey("required_attendance")
        private val KEY_RESET_FIX = booleanPreferencesKey("reset_fix")
        private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val KEY_FRIEND_ROLLS = stringPreferencesKey("friend_rolls")
        private val KEY_SELECTED_FRIEND_ROLL = stringPreferencesKey("selected_friend_roll")
    }

    val notificationStateFlow = dataStore.data
        .map { it[KEY_NOTIFICATIONS_ENABLED] ?: false }
    val resetFixFlow = dataStore.data
        .map { it[KEY_RESET_FIX] ?: false }

    val requiredAttendanceFlow = dataStore.data
        .map { it[KEY_REQUIRED_ATTENDANCE] ?: 75 }
    val userNameFlow = dataStore.data
        .map { it[KEY_USER_NAME] ?: "" }

    val userRollFlow = dataStore.data
        .map { it[KEY_USER_ROLLNUMBER] ?: "" }

    val academicYearFlow = dataStore.data
        .map { it[KEY_ACADEMIC_YEAR] ?: "" }

    val termCodeFlow = dataStore.data
        .map { it[KEY_TERM_CODE] ?: "" }

    val onBoardingFlow = dataStore.data
        .map { it[KEY_ONBOARDING_DONE] ?: false }

    val userSetupDoneFlow = dataStore.data
        .map { it[KEY_USER_SETUP_DONE] ?: false }

    val friendRollsFlow = dataStore.data
        .map { prefs ->
            prefs[KEY_FRIEND_ROLLS]
                ?.let { json ->
                    json.removeSurrounding("[", "]")
                        .split(",")
                        .map { it.trim().removeSurrounding("\"") }
                        .filter { it.isNotBlank() }
                }
                ?: emptyList()
        }

    val selectedFriendRollFlow = dataStore.data
        .map { it[KEY_SELECTED_FRIEND_ROLL] ?: "" }

    suspend fun setUserName(username: String) {
        dataStore.edit { it[KEY_USER_NAME] = username }
    }

    suspend fun setUserRollNumber(rollNumber: String) {
        dataStore.edit { it[KEY_USER_ROLLNUMBER] = rollNumber }
    }

    suspend fun setUserSetupDone() {
        dataStore.edit { it[KEY_USER_SETUP_DONE] = true }
    }

    suspend fun setOnboardingDone() {
        dataStore.edit { it[KEY_ONBOARDING_DONE] = true }
    }

    suspend fun setAcademicYear(year: String) {
        dataStore.edit { it[KEY_ACADEMIC_YEAR] = year }
    }

    suspend fun setTermCode(term: String) {
        dataStore.edit { it[KEY_TERM_CODE] = term }
    }

    suspend fun setRequiredAttendance(attendance: Int) {
        dataStore.edit {
            it[KEY_REQUIRED_ATTENDANCE] = attendance
        }
    }

    suspend fun setResetDone(){
        dataStore.edit {
            it[KEY_RESET_FIX] = true
        }
    }

    suspend fun setNotificationState(state: Boolean) {
        dataStore.edit {
            it[KEY_NOTIFICATIONS_ENABLED] = state
        }
    }

    suspend fun addFriendRoll(roll: String) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_FRIEND_ROLLS]
                ?.let {
                    it.removeSurrounding("[", "]")
                        .split(",")
                        .map { r -> r.trim().removeSurrounding("\"") }
                        .filter { r -> r.isNotBlank() }
                }
                ?: emptyList()
            if (roll !in current) {
                val updated = current + roll
                prefs[KEY_FRIEND_ROLLS] =
                    updated.joinToString(
                        prefix = "[\"",
                        separator = "\",\"",
                        postfix = "\"]"
                    )
            }
        }
    }

    suspend fun removeFriendRoll(roll: String) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_FRIEND_ROLLS]
                ?.let {
                    it.removeSurrounding("[", "]")
                        .split(",")
                        .map { r -> r.trim().removeSurrounding("\"") }
                        .filter { r -> r.isNotBlank() }
                }
                ?: emptyList()

            val updated = current - roll

            prefs[KEY_FRIEND_ROLLS] =
                updated.joinToString(
                    prefix = "[\"",
                    separator = "\",\"",
                    postfix = "\"]"
                )
        }
    }

    suspend fun setSelectedFriendRoll(roll: String) {
        dataStore.edit { it[KEY_SELECTED_FRIEND_ROLL] = roll }
    }

    suspend fun clearSelectedFriend() {
        dataStore.edit { it.remove(KEY_SELECTED_FRIEND_ROLL) }
    }
}