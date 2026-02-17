package com.kito

import android.R
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.util.Consumer
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavKey
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.kito.core.datastore.PrefsRepository
import com.kito.core.network.supabase.SupabaseRepository
import com.kito.core.network.supabase.model.PlatformClass
import com.kito.core.platform.AppConfig
import com.kito.core.platform.ESP
import com.kito.core.platform.SecureStorage
import com.kito.core.presentation.navigation3.Routes
import com.kito.core.presentation.theme.KitoTheme
import com.kito.feature.app.presentation.MainUI
import com.kito.feature.schedule.notification.NotificationPipelineController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val prefs: PrefsRepository by inject()

    private val supabaseRepo: SupabaseRepository by inject()

    private val eSP: ESP by inject()

    private val secureStorage: SecureStorage by inject()

    private var currentUpdateType: Int = AppUpdateType.FLEXIBLE

    private val notificationPipelineController by lazy {
        NotificationPipelineController.get(applicationContext)
    }
    private lateinit var appUpdateManager: AppUpdateManager

    private val updateLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result: ActivityResult ->
            Log.d("UPDATE_FLOW", "Launcher result code: ${result.resultCode}")
        }

    private val installStateListener = InstallStateUpdatedListener { state ->
        Log.d("UPDATE_FLOW", "Install state changed: ${state.installStatus()}")

        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showCompleteUpdateSnackbar()
        }
    }


    override fun onStart() {
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        appUpdateManager = if (BuildConfig.DEBUG) {
            FakeAppUpdateManager(this)
        }else{
            AppUpdateManagerFactory.create(this)
        }
        if (appUpdateManager is FakeAppUpdateManager) {
            val fake = appUpdateManager as FakeAppUpdateManager
            fake.setUpdateAvailable(30)
            fake.setUpdatePriority(5)
        }
        AppConfig.init(
            portalBase = BuildConfig.PORTAL_BASE,
            wdPath = BuildConfig.WD_PATH,
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY
        )

        setContent {
            var startDestination by remember { mutableStateOf<NavKey?>(null) }
            var deepLinkTarget by remember { mutableStateOf<String?>(null) }
            var isReady by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                val intent = intent
                if (intent?.data?.scheme == "kito" && intent.data?.host == "schedule") {
                    deepLinkTarget = "schedule"
                    // Clear intent to avoid re-triggering on rotation/re-entry
                    this@MainActivity.intent =
                        Intent(this@MainActivity, MainActivity::class.java)
                }
                val espPass = eSP.getSapPassword()
                if (espPass.isNotEmpty()){
                    secureStorage.saveSapPassword(espPass)
                    eSP.clearSapPassword()
                }
                val encryptedPassword = secureStorage.getSapPassword()
                val isLoggedIn = secureStorage.isLoggedInFlow.first()
                notificationPipelineController.sync()
                val onboardingDone = prefs.onBoardingFlow.first()
                val isUserSetupDone = prefs.userSetupDoneFlow.first()
                startDestination = when {
                    !onboardingDone -> Routes.Onboarding
                    !isUserSetupDone -> Routes.UserSetup
                    else -> Routes.Tabs
                }
                isReady = true
            }

            // Handle new intents (e.g., if activity is singleTop)
            DisposableEffect(Unit) {
                val listener = Consumer<Intent> { newIntent ->
                    if (newIntent.data?.scheme == "kito" && newIntent.data?.host == "schedule") {
                        deepLinkTarget = "schedule"
                        this@MainActivity.intent =
                            Intent(this@MainActivity, MainActivity::class.java)
                    }
                }
                addOnNewIntentListener(listener)
                onDispose { removeOnNewIntentListener(listener) }
            }
            splashScreen.setKeepOnScreenCondition { !isReady }

            if (isReady) {
                KitoTheme {
                    MainUI(
                        deepLinkTarget = deepLinkTarget,
                        onDeepLinkConsumed = { deepLinkTarget = null },
                        initialDestination = startDestination
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Trigger the intent listener when new intent arrives
        if (intent.data?.scheme == "kito" && intent.data?.host == "schedule") {
            // The DisposableEffect listener will handle this
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
        appUpdateManager.registerListener(installStateListener)
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (
                info.updateAvailability() ==
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(currentUpdateType).build()
                )
                return@addOnSuccessListener
            }
            checkForSupabaseVersion()
        }
        lifecycleScope.launch {
            notificationPipelineController.sync()
        }
    }


    override fun onPause() {
        super.onPause()
        appUpdateManager.unregisterListener(installStateListener)
    }

    private fun showCompleteUpdateSnackbar() {
        Snackbar.make(
            findViewById(R.id.content),
            "Update ready",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("Restart") {
            appUpdateManager.completeUpdate()
        }.show()
    }

    private fun checkForSupabaseVersion() {
        lifecycleScope.launch {
            try {
                val result = supabaseRepo
                    .getLatestAppVersion(PlatformClass.ANDROID)
                    .firstOrNull()

                Log.d("UPDATE_FLOW", "Supabase result: $result")

                if (result == null) {
                    Log.d("UPDATE_FLOW", "Result is null")
                    return@launch
                }

                val currentVersion = BuildConfig.VERSION_NAME
                Log.d("UPDATE_FLOW", "Current: $currentVersion")
                Log.d("UPDATE_FLOW", "Latest: ${result.latest_version}")

                if (!isUpdateRequired(currentVersion, result.latest_version)) {
                    Log.d("UPDATE_FLOW", "Update NOT required")
                    return@launch
                }

                Log.d("UPDATE_FLOW", "Update required")

                currentUpdateType =
                    if (result.force_update)
                        AppUpdateType.IMMEDIATE
                    else
                        AppUpdateType.FLEXIBLE

                triggerPlayCoreUpdate(currentUpdateType)

            } catch (e: Exception) {
                Log.d("UPDATE_FLOW", "Exception: ${e.message}")
            }
        }
    }


    private fun isUpdateRequired(current: String, latest: String): Boolean {

        // Remove anything after '-' (like -debug, -testing)
        val cleanCurrent = current.substringBefore("-")

        val currentParts = cleanCurrent.split(".").map { it.toInt() }
        val latestParts = latest.split(".").map { it.toInt() }

        for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
            val c = currentParts.getOrElse(i) { 0 }
            val l = latestParts.getOrElse(i) { 0 }

            if (l > c) return true
            if (l < c) return false
        }

        return false
    }


    private fun triggerPlayCoreUpdate(updateType: Int) {

        Log.d("UPDATE_FLOW", "Trigger called with type: $updateType")

        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->

            Log.d("UPDATE_FLOW", "Availability: ${info.updateAvailability()}")
            Log.d("UPDATE_FLOW", "InstallStatus: ${info.installStatus()}")
            Log.d("UPDATE_FLOW", "Immediate allowed: ${info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)}")
            Log.d("UPDATE_FLOW", "Flexible allowed: ${info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)}")

            val started = appUpdateManager.startUpdateFlowForResult(
                info,
                updateLauncher,
                AppUpdateOptions.newBuilder(updateType).build()
            )

            Log.d("UPDATE_FLOW", "startUpdateFlowForResult called")
        }
    }
}