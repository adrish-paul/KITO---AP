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
import kotlinx.coroutines.flow.asStateFlow
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

    val friends = prefs.friendRollsFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val selectedRoll = MutableStateFlow<String?>(null)

    fun selectFriend(roll: String) {
        selectedRoll.value = roll
    }

    private val rollFlow = MutableStateFlow<String?>("23053382")

    fun setRoll(roll: String) {
        rollFlow.value = roll
    }

    val weeklySchedule: StateFlow<Map<WeekDay, List<SectionEntity>>> =
        rollFlow
            .filterNotNull()
            .flatMapLatest { roll ->
                flow {
                    val student = supabaseRepo.getStudentByRoll(roll)
                    val timetable = supabaseRepo.getTimetableForStudent(
                        section = student.section,
                        batch = student.batch
                    )
                    timetable.forEach {
                        println("RAW DAY FROM SUPABASE = |${it.day}|")
                    }
                    val grouped = WeekDay.entries.associateWith { day ->
                        val filtered = timetable.filter {
                            println("Comparing DB |${it.day}| with ENUM |${day.apiValue}|")
                            it.day == day.apiValue
                        }
                        println("DAY ${day.apiValue} COUNT = ${filtered.size}")
                        filtered
                    }
                    emit(grouped)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyMap()
            )
}