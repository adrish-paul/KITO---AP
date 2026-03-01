package com.kito.feature.gpa.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kito.core.database.entity.StudentEntity
import com.kito.core.database.repository.StudentRepository
import com.kito.core.datastore.PrefsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GPAViewmodel(
    prefs: PrefsRepository,
    studentRepository: StudentRepository
): ViewModel() {
    val roll = prefs.userRollFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ""
    )

    private val _student = MutableStateFlow<StudentEntity?>(null)
    val student = _student.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                _student.value = studentRepository.getStudentByRoll(roll.value)
            }catch (e: Exception){
                println(e.message)
            }
        }
    }
}