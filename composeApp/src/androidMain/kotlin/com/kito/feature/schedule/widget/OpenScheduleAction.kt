package com.kito.feature.schedule.widget

import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import androidx.core.net.toUri
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

@Keep
class OpenScheduleAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Use the launcher intent from the package instead of a direct class reference
        // to avoid a circular dependency between composeApp (library) and androidApp
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val intent = launchIntent?.apply {
            action = Intent.ACTION_VIEW
            data = "kito://schedule".toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        } ?: Intent(Intent.ACTION_VIEW, "kito://schedule".toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    }
}

