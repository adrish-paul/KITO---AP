package com.kito.core.platform

import androidx.compose.runtime.Composable
import io.ktor.client.engine.HttpClientEngine

expect fun openUrl(url: String)

expect fun createHttpEngine(): HttpClientEngine

expect fun toast(message: String)

expect fun sendEmail(to: String, subject: String, body: String)

expect fun openAppSettings()

expect fun openNotificationSettings()

expect fun openAlarmSettings()

expect fun canScheduleExactAlarms(): Boolean

expect suspend fun areNotificationsEnabled(): Boolean

@Composable
expect fun NotificationPermissionEffect(onResult: (Boolean) -> Unit)
