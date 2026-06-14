package com.kito.feature.gpa.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kito.core.datastore.PrefsRepository
import com.kito.feature.gpa.domain.model.StudentProfile
import com.kito.feature.gpa.domain.repository.GpaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class GPAViewmodel(
    prefs: PrefsRepository,
    private val gpaRepository: GpaRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    val roll = prefs.userRollFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ""
    )

    private val _student = MutableStateFlow<StudentProfile?>(null)
    val student = _student.asStateFlow()

    private val _branch = MutableStateFlow("CSE")
    val branch = _branch.asStateFlow()

    private val _semester = MutableStateFlow(1)
    val semester = _semester.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            roll.collect { rollNumber ->
                if (rollNumber.isEmpty()) return@collect
                val profile = gpaRepository.getStudentProfile(rollNumber)
                _student.value = profile
                configureGpaDefaults(rollNumber, profile)
            }
        }
    }

    private fun configureGpaDefaults(roll: String, profile: StudentProfile?) {
        _branch.value = profile?.section?.substringBefore("-") ?: "CSE"
        _semester.value = deriveSemesterFromRoll(roll)
    }

    private fun deriveSemesterFromRoll(roll: String): Int {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val currentYear = now.year
        val month = now.month.number
        val joinYear = ("20" + roll.take(2)).toInt()
        val yearDiff = currentYear - joinYear
        val term = if (month in 7..11) "010" else "020"
        return when (yearDiff) {
            1 -> if (term == "010") 1 else 2
            2 -> if (term == "010") 3 else 4
            3 -> if (term == "010") 5 else 6
            4 -> if (term == "010") 7 else 8
            else -> 1
        }
    }

    fun onEvent(event: GPAEvent) {
        when (event) {
            is GPAEvent.UpdateSemester -> updateSemester(event.semester)
            is GPAEvent.UpdateBranch -> updateBranch(event.branch)
        }
    }

    private fun updateSemester(semester: Int) { _semester.value = semester }
    private fun updateBranch(branch: String) { _branch.value = branch }
}
