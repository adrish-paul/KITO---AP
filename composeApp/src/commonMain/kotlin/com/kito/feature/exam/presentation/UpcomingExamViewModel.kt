package com.kito.feature.exam.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kito.core.common.util.currentLocalDateTime
import com.kito.core.datastore.PrefsRepository
import com.kito.core.presentation.components.state.SyncUiState
import com.kito.feature.exam.domain.model.ExamSchedule
import com.kito.feature.exam.domain.repository.ExamRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

class UpcomingExamViewModel(
    private val prefs: PrefsRepository,
    private val examRepository: ExamRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val _exams = MutableStateFlow<List<ExamSchedule>>(emptyList())
    val exams = _exams.asStateFlow()

    private val _uiState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val uiState = _uiState.asStateFlow()

    init {
        getExamSchedule()
    }

    fun getExamSchedule() {
        viewModelScope.launch(dispatcher) {
            try {
                val roll = prefs.userRollFlow.first()
                _exams.value = filterUpcomingOrOngoing(examRepository.getExamSchedule(roll))
                _uiState.value = SyncUiState.Success
            } catch (e: Exception) {
                println("exam error: ${e.message}")
                _uiState.value = SyncUiState.Error(e.message ?: "")
            }
        }
    }

    private fun filterUpcomingOrOngoing(exams: List<ExamSchedule>): List<ExamSchedule> {
        val now = currentLocalDateTime()
        val nowDate = now.date
        val nowTime = now.time

        return exams
            .mapNotNull { exam ->
                try {
                    val examDate = LocalDate.parse(exam.date)
                    val startTime = LocalTime.parse(exam.startTime)
                    val endTime = LocalTime.parse(exam.endTime)

                    when {
                        examDate == nowDate && nowTime >= startTime && nowTime < endTime ->
                            exam to LocalDateTime(examDate, startTime)
                        examDate > nowDate || (examDate == nowDate && startTime > nowTime) ->
                            exam to LocalDateTime(examDate, startTime)
                        else -> null
                    }
                } catch (_: Exception) {
                    null
                }
            }
            .sortedBy { it.second }
            .map { it.first }
    }
}
