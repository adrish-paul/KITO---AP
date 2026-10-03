package com.kito.core.datastore.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kito.core.datastore.domain.repository.PrefsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Provided

class PrefsRepositoryImpl(
    @Provided private val dataStore: DataStore<Preferences>
) : PrefsRepository {
    companion object {
        private val KEY_ACADEMIC_YEAR = stringPreferencesKey("academic_year")
        private val KEY_TERM_CODE = stringPreferencesKey("term_code")
        private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        private val KEY_USER_SETUP_DONE = booleanPreferencesKey("user_setup_done")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_ROLLNUMBER = stringPreferencesKey("User_Password")
        private val KEY_REQUIRED_ATTENDANCE = intPreferencesKey("required_attendance")
        private val KEY_RESET_FIX_V3 = booleanPreferencesKey("reset_fix_V3")
        private val KEY_KAYA_INTRO_SEEN = booleanPreferencesKey("kaya_intro_seen")
        private val KEY_KAYA_CONNECTED = booleanPreferencesKey("kaya_connected")
        private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val KEY_FRIEND_ROLLS = stringPreferencesKey("friend_rolls")
        private val KEY_SELECTED_FRIEND_ROLL = stringPreferencesKey("selected_friend_roll")
    }

    override val notificationStateFlow: Flow<Boolean> = dataStore.data
        .map { it[KEY_NOTIFICATIONS_ENABLED] ?: false }

    override val resetFixFlow: Flow<Boolean> = dataStore.data
        .map { it[KEY_RESET_FIX_V3] ?: false }

    override val kayaIntroSeenFlow: Flow<Boolean> = dataStore.data
        .map { it[KEY_KAYA_INTRO_SEEN] ?: false }

    override val kayaConnectedFlow: Flow<Boolean> = dataStore.data
        .map { it[KEY_KAYA_CONNECTED] ?: false }

    override val requiredAttendanceFlow: Flow<Int> = dataStore.data
        .map { it[KEY_REQUIRED_ATTENDANCE] ?: 75 }

    override val userNameFlow: Flow<String> = dataStore.data
        .map { it[KEY_USER_NAME] ?: "" }

    override val userRollFlow: Flow<String> = dataStore.data
        .map { it[KEY_USER_ROLLNUMBER] ?: "" }

    override val academicYearFlow: Flow<String> = dataStore.data
        .map { it[KEY_ACADEMIC_YEAR] ?: "" }

    override val termCodeFlow: Flow<String> = dataStore.data
        .map { it[KEY_TERM_CODE] ?: "" }

    override val onBoardingFlow: Flow<Boolean> = dataStore.data
        .map { it[KEY_ONBOARDING_DONE] ?: false }

    override val userSetupDoneFlow: Flow<Boolean> = dataStore.data
        .map { it[KEY_USER_SETUP_DONE] ?: false }

    override val friendRollsFlow: Flow<List<String>> = dataStore.data
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

    override val selectedFriendRollFlow: Flow<String> = dataStore.data
        .map { it[KEY_SELECTED_FRIEND_ROLL] ?: "" }

    override suspend fun setUserName(username: String) {
        dataStore.edit { it[KEY_USER_NAME] = username }
    }

    override suspend fun setUserRollNumber(rollNumber: String) {
        dataStore.edit {
            if (it[KEY_USER_ROLLNUMBER] != rollNumber) {
                it[KEY_KAYA_CONNECTED] = false
                it.remove(stringPreferencesKey("kaya_roll"))
                it.remove(stringPreferencesKey("kaya_timetable"))
            }
            it[KEY_USER_ROLLNUMBER] = rollNumber
        }
    }

    override suspend fun setUserSetupDone() {
        dataStore.edit { it[KEY_USER_SETUP_DONE] = true }
    }

    override suspend fun setOnboardingDone() {
        dataStore.edit { it[KEY_ONBOARDING_DONE] = true }
    }

    override suspend fun setKayaIntroSeen() {
        dataStore.edit { it[KEY_KAYA_INTRO_SEEN] = true }
    }

    override suspend fun setKayaConnected(connected: Boolean) {
        dataStore.edit { it[KEY_KAYA_CONNECTED] = connected }
    }

    override suspend fun setAcademicYear(year: String) {
        dataStore.edit { it[KEY_ACADEMIC_YEAR] = year }
    }

    override suspend fun setTermCode(term: String) {
        dataStore.edit { it[KEY_TERM_CODE] = term }
    }

    override suspend fun setRequiredAttendance(attendance: Int) {
        dataStore.edit {
            it[KEY_REQUIRED_ATTENDANCE] = attendance
        }
    }

    override suspend fun setResetDone() {
        dataStore.edit {
            it[KEY_RESET_FIX_V3] = true
        }
    }

    override suspend fun setNotificationState(state: Boolean) {
        dataStore.edit {
            it[KEY_NOTIFICATIONS_ENABLED] = state
        }
    }

    override suspend fun addFriendRoll(roll: String) {
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

    override suspend fun removeFriendRoll(roll: String) {
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

    override suspend fun setSelectedFriendRoll(roll: String) {
        dataStore.edit { it[KEY_SELECTED_FRIEND_ROLL] = roll }
    }

    override suspend fun clearSelectedFriend() {
        dataStore.edit { it.remove(KEY_SELECTED_FRIEND_ROLL) }
    }
}
