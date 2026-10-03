package com.kito.kaya

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kito.core.database.entity.SectionEntity
import com.kito.core.database.entity.StudentSectionEntity
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Provided

/** Personal KAYA data is separate from shared Supabase sections and their IDs. */
class KayaTimetableStore(@Provided private val dataStore: DataStore<Preferences>) {
    private val connectedKey = booleanPreferencesKey("kaya_connected")
    private val ownerKey = stringPreferencesKey("kaya_roll")
    private val rowsKey = stringPreferencesKey("kaya_timetable")
    private val userKey = stringPreferencesKey("User_Password")
    private val json = Json { ignoreUnknownKeys = true }

    // null means Supabase; an empty list means KAYA selected with no cached rows.
    fun observe(roll: String) = dataStore.data.map { prefs ->
        if (prefs[connectedKey] != true || prefs[userKey] != roll ||
            prefs[ownerKey] != roll || prefs[rowsKey] == null) null
        else runCatching {
            json.decodeFromString<List<SectionEntity>>(prefs[rowsKey] ?: "[]")
                .map { it.toStudentSection(roll) }.sortedBy { it.startTime }
        }.getOrDefault(emptyList())
    }

    val isConnected = dataStore.data.map {
        it[connectedKey] == true && it[ownerKey] == it[userKey] && it[rowsKey] != null
    }

    suspend fun save(roll: String, rows: List<SectionEntity>) {
        dataStore.edit {
            check(it[userKey] == roll) { "Your roll number changed. Please connect to KAYA again." }
            it[ownerKey] = roll
            it[rowsKey] = json.encodeToString(rows)
            it[connectedKey] = true
        }
    }

    suspend fun clear() {
        dataStore.edit {
            it[connectedKey] = false
            it.remove(ownerKey)
            it.remove(rowsKey)
        }
    }
}

internal fun SectionEntity.toStudentSection(roll: String) = StudentSectionEntity(
    sectionId = id, rollNo = roll, section = section, batch = batch,
    day = day.trim().take(3).uppercase(), startTime = start_time, endTime = end_time,
    subject = subject, room = room,
)
