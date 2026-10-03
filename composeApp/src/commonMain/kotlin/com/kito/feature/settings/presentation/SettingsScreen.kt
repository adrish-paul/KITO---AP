package com.kito.feature.settings.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.kito.core.datastore.domain.repository.PrefsRepository
import com.kito.kaya.KayaRepository
import com.kito.kaya.KayaResult
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinInject(),
    prefs: PrefsRepository = koinInject(),
    kaya: KayaRepository = koinInject(),
    tabNavBackStack: NavBackStack<NavKey>,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val name by viewModel.name.collectAsState()
    val roll by viewModel.rollNumber.collectAsState()
    val kayaConnected by kaya.isConnected.collectAsState(initial = false)
    val year by viewModel.year.collectAsState()
    val term by viewModel.term.collectAsState()
    val notificationState by viewModel.notificationState.collectAsState()
    val requiredAttendance by viewModel.requiredAttendance.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val pendingEnable by viewModel.pendingNotificationEnable.collectAsState()

    SettingsContent(
        name = name,
        roll = roll,
        year = year,
        term = term,
        notificationState = notificationState,
        requiredAttendance = requiredAttendance,
        isLoggedIn = isLoggedIn,
        syncState = syncState,
        pendingEnable = pendingEnable,
        onEvent = { viewModel.onEvent(it) },
        tabNavBackStack = tabNavBackStack,
        snackbarHostState = snackbarHostState,
        kayaConnected = kayaConnected,
        // Username is the user's roll number; returns an error message, or null on success.
        onKayaConnect = { pass ->
            when (val result = kaya.connect(roll, pass)) {
                is KayaResult.Success -> null
                is KayaResult.Error -> result.message
            }
        },
        onKayaDisconnect = { scope.launch {
            try {
                if (kaya.disconnect(roll).isFailure) {
                    snackbarHostState.showSnackbar("Logged out of KAYA. Could not refresh the saved timetable; try again when online.")
                }
            }
            catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (_: Exception) { snackbarHostState.showSnackbar("Could not log out of KAYA. Please try again.") }
        } },
    )
}
