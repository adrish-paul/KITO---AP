package com.kito.feature.friendview.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kito.core.database.entity.SectionEntity
import com.kito.core.datastore.PrefsRepository
import com.kito.core.network.supabase.SupabaseRepository
import com.kito.feature.schedule.presentation.WeekDay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FriendViewViewmodel(
    private val supabaseRepo: SupabaseRepository,
    private val prefs: PrefsRepository
) : ViewModel() {

    val friendRolls = prefs.friendRollsFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val selectedFriendRoll = prefs.selectedFriendRollFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ""
        )

    fun selectFriend(roll: String) {
        viewModelScope.launch {
            prefs.setSelectedFriendRoll(roll)
        }
    }

    fun addFriend(roll: String) {
        viewModelScope.launch {
            prefs.addFriendRoll(roll)
            selectFriend(roll)
        }
    }

    fun removeFromList(roll: String) {
        viewModelScope.launch {
            prefs.removeFriendRoll(roll)
            if(roll == selectedFriendRoll.value){
                selectFriend(friendRolls.value.firstOrNull() ?: "")
            }
        }
    }
    val weeklySchedule: StateFlow<Map<WeekDay, List<SectionEntity>>> =
        selectedFriendRoll
            .flatMapLatest { roll ->
                if (roll.isBlank()) {
                    flow { emit(emptyMap()) }
                } else {
                    flow {
                        try {
                            val student = supabaseRepo.getStudentByRoll(roll)
                            if (student.section.isBlank()) {
                                emit(emptyMap())
                                return@flow
                            }
                            val timetable = supabaseRepo.getTimetableForStudent(
                                section = student.section,
                                batch = student.batch
                            )
                            val grouped = WeekDay.entries.associateWith { day ->
                                timetable.filter {
                                    it.day == day.apiValue
                                }
                            }
                            emit(grouped)
                        } catch (e: Exception) {
                            println("Error fetching friend schedule: ${e.message}")
                            emit(emptyMap<WeekDay, List<SectionEntity>>())
                        }
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyMap()
            )
}