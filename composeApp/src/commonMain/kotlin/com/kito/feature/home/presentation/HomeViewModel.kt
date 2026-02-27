package com.kito.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kito.core.common.util.currentLocalDateTime
import com.kito.core.database.entity.AttendanceEntity
import com.kito.core.database.entity.StudentSectionEntity
import com.kito.core.database.repository.AttendanceRepository
import com.kito.core.database.repository.StudentSectionRepository
import com.kito.core.datastore.PrefsRepository
import com.kito.core.network.supabase.SupabaseRepository
import com.kito.core.network.supabase.model.AdModel
import com.kito.core.network.supabase.model.MidsemScheduleModel
import com.kito.core.platform.ConnectivityObserver
import com.kito.core.platform.SecureStorage
import com.kito.core.presentation.components.AppSyncUseCase
import com.kito.core.presentation.components.StartupSyncGuard
import com.kito.core.presentation.components.state.SyncUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

class HomeViewModel (
    private val prefs: PrefsRepository,
    private val secureStorage: SecureStorage,
    private val attendanceRepository: AttendanceRepository,
    private val studentSectionRepository: StudentSectionRepository,
    private val appSyncUseCase: AppSyncUseCase,
    private val syncGuard: StartupSyncGuard,
    private val connectivityObserver: ConnectivityObserver,
    private val supabaseRepository: SupabaseRepository,
): ViewModel() {
    val isOnline = connectivityObserver.isOnline
    val name = prefs.userNameFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ""
    )

    private val _ads = MutableStateFlow<List<AdModel>>(emptyList())
    val ads: StateFlow<List<AdModel>> = _ads.asStateFlow()

    init {
        fetchAds()
    }

    val sapLoggedIn = secureStorage.isLoggedInFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    private fun fetchAds() {
        viewModelScope.launch {
            runCatching { supabaseRepository.getAds() }
                .onSuccess {
                    println("Ads loaded: ${it.size}")
                    _ads.value = it
                }
                .onFailure {
                    println("Ads error: ${it.message}")
                }
        }
    }

    private val _day = MutableStateFlow<String>("")
    val day: StateFlow<String> = _day
    private val nextDay: StateFlow<String> =
        day.map { currentDay ->
            when (currentDay) {
                "MON" -> "TUE"
                "TUE" -> "WED"
                "WED" -> "THU"
                "THU" -> "FRI"
                "FRI" -> "SAT"
                "SAT" -> "SUN"
                "SUN" -> "MON"
                else -> ""
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ""
        )

    fun updateDay(day: String) {
        _day.value = day
    }

    val attendance: StateFlow<List<AttendanceEntity>> =
        attendanceRepository
            .getAllAttendance()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    private val _syncState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncState = _syncState.asStateFlow()
    private val _syncEvents = MutableSharedFlow<SyncUiState>()
    val syncEvents: SharedFlow<SyncUiState> = _syncEvents

    private val _loginState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val loginState = _loginState.asStateFlow()

    private val _examModel = MutableStateFlow<MidsemScheduleModel?>(null)
    val examModel = _examModel.asStateFlow()
    fun syncOnStartup() {
        if (syncGuard.hasSynced) return
        syncGuard.hasSynced = true
        viewModelScope.launch {
            _syncEvents.emit(SyncUiState.Loading)
            _syncState.value = SyncUiState.Loading
            val roll = prefs.userRollFlow.first()
            val sapPassword = secureStorage.getSapPassword()
            val year = prefs.academicYearFlow.first()
            val term = prefs.termCodeFlow.first()

            val result = appSyncUseCase.syncAll(
                roll = roll,
                sapPassword = sapPassword,
                year = year,
                term = term
            )

            _syncState.value = result.fold(
                onSuccess = {
                    _syncEvents.emit(SyncUiState.Success)
                    SyncUiState.Success
                },
                onFailure = {
                    SyncUiState.Error(it.message ?: "Sync failed")
                }
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val isScheduleEmpty: StateFlow<Boolean> =
        prefs.userRollFlow
            .flatMapLatest { roll ->
                studentSectionRepository
                    .getAllScheduleForStudent(rollNo = roll)
                    .map { it.isEmpty() }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                true
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val schedule: StateFlow<List<StudentSectionEntity>> =
        prefs.userRollFlow
            .flatMapLatest { roll ->
                day.flatMapLatest { day ->
                    studentSectionRepository.getScheduleForStudent(
                        rollNo = roll,
                        day = day
                    )
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val nextSchedule: StateFlow<List<StudentSectionEntity>> =
        prefs.userRollFlow
            .flatMapLatest { roll ->
                nextDay.flatMapLatest { day ->
                    studentSectionRepository.getScheduleForStudent(
                        rollNo = roll,
                        day = day
                    )
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    fun login(
        password: String
    ){
        viewModelScope.launch {
            _loginState.value = SyncUiState.Loading
            delay(1000)
            val roll = prefs.userRollFlow.first()
            val year = prefs.academicYearFlow.first()
            val term = prefs.termCodeFlow.first()

            val result = appSyncUseCase.syncAll(
                roll = roll,
                sapPassword = password,
                year = year,
                term = term
            )

            _loginState.value = result.fold(
                onSuccess = {
                    secureStorage.saveSapPassword(password)
                    SyncUiState.Success
                },
                onFailure = {
                    SyncUiState.Error(it.message ?: "Sync failed")
                }
            )
        }
    }
    fun setLoginStateIdle(){
        _loginState.value = SyncUiState.Idle
    }

    fun getExamSchedule(){
        viewModelScope.launch {
            try {
                val roll = prefs.userRollFlow.first()
                val examSchedule = supabaseRepository.getMidSemSchedule(roll)
                _examModel.value = getNextOrOngoingExam(examSchedule)
            }catch (e: Exception){
                println("exam model error: ${e.message}")
            }
        }
    }

    fun getNextOrOngoingExam(
        exams: List<MidsemScheduleModel>
    ): MidsemScheduleModel? {

        val now = currentLocalDateTime()
        val nowDate = now.date
        val nowTime = now.time

        return exams
            .mapNotNull { exam ->
                try {
                    val examDate = LocalDate.parse(exam.date)
                    val startTime = LocalTime.parse(exam.start_time)
                    val endTime = LocalTime.parse(exam.end_time)

                    when {
                        // 🟢 Exam is ONGOING
                        examDate == nowDate &&
                                nowTime >= startTime &&
                                nowTime < endTime -> {
                            exam to LocalDateTime(examDate, startTime)
                        }

                        // 🔵 Exam is in the FUTURE
                        examDate > nowDate ||
                                (examDate == nowDate && startTime > nowTime) -> {
                            exam to LocalDateTime(examDate, startTime)
                        }

                        else -> null
                    }
                } catch (e: Exception) {
                    null
                }
            }
            .minByOrNull { it.second }
            ?.first
    }

    fun postRecruitmentClick(){
        viewModelScope.launch {
            try {
                val roll = prefs.userRollFlow.first()
                supabaseRepository.postRecruitmentClick(roll)
            }catch (e: Exception){
                _syncEvents.emit(
                    SyncUiState.Error(e.message?:"")
                )
            }
        }
    }
}




