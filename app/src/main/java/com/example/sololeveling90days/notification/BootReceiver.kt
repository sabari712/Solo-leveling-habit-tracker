package com.example.sololeveling90days.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * BroadcastReceiver that restarts the WorkManager reminder chain after a device reboot.
 * Registered in AndroidManifest.xml with BOOT_COMPLETED and MY_PACKAGE_REPLACED intents.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            NotificationScheduler.scheduleReminders(context)
        }
    }
}

object NotificationScheduler {
    private const val WORK_NAME = "solo_leveling_task_reminder"

    fun scheduleReminders(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<TaskReminderWorker>(
            30, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun cancelReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
