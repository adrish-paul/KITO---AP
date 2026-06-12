package com.kito.feature.friendview.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kito.core.datastore.PrefsRepository
import com.kito.feature.friendview.domain.model.FriendScheduleItem
import com.kito.feature.friendview.domain.repository.FriendViewRepository
import com.kito.feature.schedule.presentation.WeekDay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FriendViewViewmodel(
    private val friendViewRepository: FriendViewRepository,
    private val prefs: PrefsRepository,
    private val dispatcher: kotlinx.coroutines.CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Default,
) : ViewModel() {

    val friendRolls = prefs.friendRollsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedFriendRoll = prefs.selectedFriendRollFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun selectFriend(roll: String) {
        viewModelScope.launch(dispatcher) { prefs.setSelectedFriendRoll(roll) }
    }

    fun addFriend(roll: String) {
        viewModelScope.launch(dispatcher) {
            prefs.addFriendRoll(roll)
            selectFriend(roll)
        }
    }

    fun removeFromList(roll: String) {
        viewModelScope.launch(dispatcher) {
            prefs.removeFriendRoll(roll)
            if (roll == selectedFriendRoll.value) {
                selectFriend(friendRolls.value.firstOrNull() ?: "")
            }
        }
    }

    val weeklySchedule: StateFlow<Map<WeekDay, List<FriendScheduleItem>>> =
        selectedFriendRoll
            .flatMapLatest { roll ->
                if (roll.isBlank()) {
                    flow { emit(emptyMap()) }
                } else {
                    flow {
                        try {
                            val items = friendViewRepository.getFriendSchedule(roll)
                            val grouped = WeekDay.entries.associateWith { day ->
                                items.filter { it.day == day.apiValue }
                            }
                            emit(grouped)
                        } catch (e: Exception) {
                            println("Error fetching friend schedule: ${e.message}")
                            emit(emptyMap<WeekDay, List<FriendScheduleItem>>())
                        }
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyMap(),
            )
}
