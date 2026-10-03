package com.kito.feature.auth

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.kito.core.auth.AuthEvent
import com.kito.core.auth.AuthRepository
import com.kito.core.auth.AuthState
import com.kito.core.auth.AuthUser
import com.kito.core.datastore.domain.repository.PrefsRepository
import com.kito.core.datastore.data.PrefsRepositoryImpl
import com.kito.feature.auth.presentation.usersetup.LoadingSource
import com.kito.feature.auth.presentation.usersetup.SetupState
import com.kito.feature.auth.presentation.usersetup.UserSetupViewModel
import com.kito.testing.FakeCredentialsRepository
import com.kito.core.sync.domain.SyncUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UserSetupViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val tempPath = "auth_prefs_test.preferences_pb".toPath()
    private lateinit var prefsRepository: PrefsRepository
    private lateinit var datastoreScope: CoroutineScope

    private lateinit var fakeCredentials: FakeCredentialsRepository
    private lateinit var spySyncUseCase: SpySyncUseCase
    private lateinit var fakeAuthRepository: MutableFakeAuthRepository
    private lateinit var vm: UserSetupViewModel

    class SpySyncUseCase : SyncUseCase {
        override suspend fun syncAttendance(roll: String, sapPassword: String, year: String, term: String): Result<Unit> =
            error("Onboarding should request a full sync")
        var syncAllRoll: String? = null
        var syncAllPassword: String? = null
        var syncAllYear: String? = null
        var syncAllTerm: String? = null

        override suspend fun syncAll(roll: String, sapPassword: String, year: String, term: String): Result<Unit> {
            syncAllRoll = roll
            syncAllPassword = sapPassword
            syncAllYear = year
            syncAllTerm = term
            return Result.success(Unit)
        }
    }

    class MutableFakeAuthRepository : AuthRepository {
        private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
        override val authState = _authState

        private val _events = MutableSharedFlow<AuthEvent>()
        override val events = _events

        var signInWithGoogleCalled = false

        override suspend fun restoreSession() {}
        override suspend fun signInWithGoogle() {
            signInWithGoogleCalled = true
        }
        override suspend fun signOut() {}
        override suspend fun updateDisplayName(name: String) {}
        override fun currentUser(): AuthUser? = null

        fun emitAuthState(state: AuthState) {
            _authState.value = state
        }

        suspend fun emitEvent(event: AuthEvent) {
            _events.emit(event)
        }
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        datastoreScope = CoroutineScope(testDispatcher + SupervisorJob())
        prefsRepository = PrefsRepositoryImpl(
            PreferenceDataStoreFactory.createWithPath(
                scope = datastoreScope,
                produceFile = { tempPath }
            )
        )
        fakeCredentials = FakeCredentialsRepository()
        spySyncUseCase = SpySyncUseCase()
        fakeAuthRepository = MutableFakeAuthRepository()
        vm = UserSetupViewModel(
            prefs = prefsRepository,
            credentialsRepository = fakeCredentials,
            appSyncUseCase = spySyncUseCase,
            authRepository = fakeAuthRepository,
            dispatcher = testDispatcher,
        )
    }

    @AfterTest
    fun teardown() {
        datastoreScope.cancel()
        Dispatchers.resetMain()
        try {
            FileSystem.SYSTEM.delete(tempPath)
        } catch (_: Exception) {
            // ignore
        }
    }

    @Test
    fun setupState_initiallyIdle() = runTest(testDispatcher) {
        assertIs<SetupState.Idle>(vm.setupState.value)
        assertEquals(LoadingSource.None, vm.loadingSource.value)
    }

    @Test
    fun signInWithGoogle_triggersAuthRepository() = runTest(testDispatcher) {
        vm.signInWithGoogle()
        advanceUntilIdle()
        assertTrue(fakeAuthRepository.signInWithGoogleCalled)
        assertIs<SetupState.Loading>(vm.setupState.value)
    }

    @Test
    fun onSignInStarted_updatesLoadingSourceAndState() = runTest(testDispatcher) {
        vm.onSignInStarted()
        assertEquals(LoadingSource.Google, vm.loadingSource.value)
        assertIs<SetupState.Loading>(vm.setupState.value)
    }

    @Test
    fun onSignInError_updatesLoadingSourceAndSetsError() = runTest(testDispatcher) {
        vm.onSignInError("something failed")
        assertEquals(LoadingSource.None, vm.loadingSource.value)
        assertIs<SetupState.Error>(vm.setupState.value)
        assertEquals("something failed", (vm.setupState.value as SetupState.Error).message)
    }

    @Test
    fun onSignInCancelled_updatesLoadingSourceAndResetsState() = runTest(testDispatcher) {
        vm.onSignInStarted()
        vm.onSignInCancelled()
        assertEquals(LoadingSource.None, vm.loadingSource.value)
        assertIs<SetupState.Idle>(vm.setupState.value)
    }

    @Test
    fun completeSetup_success_savesInfoSchedulesSyncAndSetsSuccess() = runTest(testDispatcher) {
        vm.completeSetup("John Doe", "123456", "2026", "010")
        advanceUntilIdle()

        assertEquals("John Doe", prefsRepository.userNameFlow.first())
        assertEquals("123456", prefsRepository.userRollFlow.first())
        assertEquals("2026", prefsRepository.academicYearFlow.first())
        assertEquals("010", prefsRepository.termCodeFlow.first())
        assertEquals("123456", spySyncUseCase.syncAllRoll)
        assertEquals("", spySyncUseCase.syncAllPassword)
        assertEquals("2026", spySyncUseCase.syncAllYear)
        assertEquals("010", spySyncUseCase.syncAllTerm)
        assertTrue(prefsRepository.userSetupDoneFlow.first())

        assertEquals(LoadingSource.None, vm.loadingSource.value)
        assertIs<SetupState.Success>(vm.setupState.value)
    }

    @Test
    fun authStateAuthenticated_autoCompletesSetup() = runTest(testDispatcher) {
        fakeAuthRepository.emitAuthState(AuthState.Authenticated(AuthUser("google-id", "jane@kiit.ac.in", "78910", "Jane Doe")))
        advanceUntilIdle()

        assertEquals("Jane Doe", prefsRepository.userNameFlow.first())
        assertEquals("78910", prefsRepository.userRollFlow.first())
        assertEquals("78910", spySyncUseCase.syncAllRoll)
        assertEquals("", spySyncUseCase.syncAllPassword)
        assertTrue(prefsRepository.userSetupDoneFlow.first())

        assertEquals(LoadingSource.None, vm.loadingSource.value)
        assertIs<SetupState.Success>(vm.setupState.value)
    }

    @Test
    fun authEventNotAllowed_setsSpecificError() = runTest(testDispatcher) {
        // Must start collection since events is a SharedFlow
        val job = launch { vm.setupState.collect {} }
        fakeAuthRepository.emitEvent(AuthEvent.NotAllowed("user@gmail.com"))
        advanceUntilIdle()

        assertEquals(LoadingSource.None, vm.loadingSource.value)
        assertIs<SetupState.Error>(vm.setupState.value)
        assertEquals("Only @kiit.ac.in accounts can sign in", (vm.setupState.value as SetupState.Error).message)

        job.cancel()
    }

    @Test
    fun authEventFailure_setsErrorMessage() = runTest(testDispatcher) {
        val job = launch { vm.setupState.collect {} }
        fakeAuthRepository.emitEvent(AuthEvent.Failure("google signin failed"))
        advanceUntilIdle()

        assertEquals(LoadingSource.None, vm.loadingSource.value)
        assertIs<SetupState.Error>(vm.setupState.value)
        assertEquals("google signin failed", (vm.setupState.value as SetupState.Error).message)

        job.cancel()
    }

    @Test
    fun authEventCancelled_resetsToIdle() = runTest(testDispatcher) {
        val job = launch { vm.setupState.collect {} }
        vm.onSignInStarted()
        fakeAuthRepository.emitEvent(AuthEvent.Cancelled)
        advanceUntilIdle()

        assertEquals(LoadingSource.None, vm.loadingSource.value)
        assertIs<SetupState.Idle>(vm.setupState.value)

        job.cancel()
    }

    @Test
    fun completeSetup_syncAllReceivesEmptyPassword_studentNotInSupabaseStillSucceeds() = runTest(testDispatcher) {
        // syncAll is called with empty password — simulates student not yet in Supabase DB.
        // Setup should still reach Success because the VM does not check Result from syncAll.
        vm.completeSetup("New Student", "999001", "2025", "010")
        advanceUntilIdle()

        assertEquals("999001", spySyncUseCase.syncAllRoll)
        assertEquals("", spySyncUseCase.syncAllPassword)
        assertIs<SetupState.Success>(vm.setupState.value)
    }

    @Test
    fun completeSetup_differentRollNumbers_eachCallPassesCorrectRoll() = runTest(testDispatcher) {
        vm.completeSetup("Alice", "100001", "2025", "010")
        advanceUntilIdle()
        assertEquals("100001", spySyncUseCase.syncAllRoll)

        spySyncUseCase = SpySyncUseCase()
        vm = UserSetupViewModel(
            prefs = prefsRepository,
            credentialsRepository = fakeCredentials,
            appSyncUseCase = spySyncUseCase,
            authRepository = fakeAuthRepository,
            dispatcher = testDispatcher,
        )

        vm.completeSetup("Bob", "200002", "2025", "020")
        advanceUntilIdle()
        assertEquals("200002", spySyncUseCase.syncAllRoll)
        assertEquals("020", spySyncUseCase.syncAllTerm)
    }
}
