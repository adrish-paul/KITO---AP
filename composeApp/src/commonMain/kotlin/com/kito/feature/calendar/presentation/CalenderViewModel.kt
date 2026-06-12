package com.kito.feature.calendar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kito.feature.calendar.domain.model.CalendarEvent
import com.kito.feature.calendar.domain.repository.CalendarRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

class CalendarViewModel(
    private val calendarRepository: CalendarRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val today = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault())

    private val _displayMonth = MutableStateFlow(today.monthNumber)
    val displayMonth: StateFlow<Int> = _displayMonth.asStateFlow()

    private val _displayYear = MutableStateFlow(today.year)
    val displayYear: StateFlow<Int> = _displayYear.asStateFlow()

    private val _selectedDate = MutableStateFlow(today.toString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _currentView = MutableStateFlow("month")
    val currentView: StateFlow<String> = _currentView.asStateFlow()

    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = _events.asStateFlow()

    private val _heatMode = MutableStateFlow(false)
    val heatMode: StateFlow<Boolean> = _heatMode.asStateFlow()

    private val _showStats = MutableStateFlow(false)
    val showStats: StateFlow<Boolean> = _showStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showAddModal = MutableStateFlow(false)
    val showAddModal: StateFlow<Boolean> = _showAddModal.asStateFlow()

    init {
        fetchEvents()
    }

    fun fetchEvents() {
        viewModelScope.launch(dispatcher) {
            runCatching {
                calendarRepository.getEventsByMonth(
                    year = _displayYear.value,
                    month = _displayMonth.value
                )
            }.onSuccess { list ->
                _events.value = list
            }.onFailure { e ->
                println("Calendar error: ${e.message}")
            }
            _isLoading.value = false
        }
    }

    fun getEventsForDate(date: String): List<CalendarEvent> =
        _events.value.filter { it.date == date }

    fun getEventsForDay(day: Int): List<CalendarEvent> =
        getEventsForDate(formatDateKey(day, _displayMonth.value, _displayYear.value))

    fun heatLevelForDay(day: Int): Int = when (getEventsForDay(day).size) {
        0    -> 0
        1    -> 1
        2    -> 2
        else -> 3
    }

    fun nextMonth() {
        if (_displayMonth.value == 12) {
            _displayMonth.value = 1
            _displayYear.value += 1
        } else {
            _displayMonth.value += 1
        }
        fetchEvents()
    }

    fun prevMonth() {
        if (_displayMonth.value == 1) {
            _displayMonth.value = 12
            _displayYear.value -= 1
        } else {
            _displayMonth.value -= 1
        }
        fetchEvents()
    }

    fun nextDay() {
        val parts = _selectedDate.value.split("-")
        val date = LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            .plus(1, DateTimeUnit.DAY)
        _selectedDate.value = date.toString()
    }

    fun prevDay() {
        val parts = _selectedDate.value.split("-")
        val date = LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            .plus(-1, DateTimeUnit.DAY)
        _selectedDate.value = date.toString()
    }

    fun selectDate(date: String) { _selectedDate.value = date }
    fun selectDay(day: Int) { _selectedDate.value = formatDateKey(day, _displayMonth.value, _displayYear.value) }
    fun setView(v: String) { _currentView.value = v }
    fun toggleHeat() { _heatMode.value = !_heatMode.value }
    fun toggleStats() { _showStats.value = !_showStats.value }
    fun setShowAddModal(v: Boolean) { _showAddModal.value = v }

    fun isToday(day: Int): Boolean =
        day == today.dayOfMonth &&
                _displayMonth.value == today.monthNumber &&
                _displayYear.value == today.year

    fun getTotalEvents(): Int = _events.value.size
    fun getCategoryCount(cat: String): Int = _events.value.count { it.category == cat }

    fun getUpcomingEvents(): List<CalendarEvent> {
        val todayStr = today.toString()
        return _events.value
            .filter { it.date >= todayStr }
            .sortedWith(compareBy { "${it.date}${it.startTime}" })
            .take(4)
    }

    fun getAgendaEvents(): Map<String, List<CalendarEvent>> {
        val grouped = mutableMapOf<String, MutableList<CalendarEvent>>()
        _events.value.forEach { ev ->
            val d = ev.date.ifEmpty { return@forEach }
            grouped.getOrPut(d) { mutableListOf() }.add(ev)
        }
        return grouped.entries
            .sortedBy { it.key }
            .associate { it.key to it.value.toList() }
    }

    fun formatDateKey(day: Int, month: Int, year: Int): String =
        "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

    fun daysInMonth(month: Int, year: Int): Int = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11            -> 30
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        else                   -> 30
    }

    fun firstDayOfMonth(month: Int, year: Int): Int {
        val dow = LocalDate(year, month, 1).dayOfWeek.isoDayNumber
        return if (dow == 7) 0 else dow
    }

    fun getDayOfWeek(dateStr: String): Int {
        return try {
            val parts = dateStr.split("-")
            val dow = LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt()).dayOfWeek.isoDayNumber
            if (dow == 7) 0 else dow
        } catch (e: Exception) { 0 }
    }
}