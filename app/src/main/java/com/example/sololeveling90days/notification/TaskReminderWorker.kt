package com.example.sololeveling90days.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sololeveling90days.data.AppRepository
import kotlinx.coroutines.flow.first
import com.example.sololeveling90days.data.PrefsKeys
import com.example.sololeveling90days.data.dataStore
import java.time.LocalDate
import java.time.LocalTime

/**
 * Background worker that checks if daily basic tasks are complete.
 * - If NOT complete: fires a push notification, then reschedules itself.
 * - If complete OR it's past midnight: cancels and does nothing.
 *
 * Triggered every 30 minutes by WorkManager chaining (one-time work re-enqueued on each run).
 */
class TaskReminderWorker(
    private val appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // Only remind between 8:00 AM and 11:30 PM
        val hour = LocalTime.now().hour
        if (hour < 8 || hour >= 24) return Result.success()

        val repository = AppRepository(appContext)

        // Check if tasks are done by reading DataStore directly (one-shot)
        val prefs = appContext.dataStore.data.first()
        val today = LocalDate.now().toString()
        val lastCompleted = prefs[PrefsKeys.LAST_COMPLETED_DATE] ?: ""

        // If user already completed all tasks today, don't notify
        if (lastCompleted == today) {
            NotificationHelper.cancelTaskReminder(appContext)
            return Result.success()
        }

        // Count incomplete tasks
        val questsJson = prefs[PrefsKeys.QUESTS_JSON] ?: "[]"
        val incompleteCount = try {
            val list = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                .decodeFromString<List<com.example.sololeveling90days.data.Quest>>(questsJson)
            list.count { it.isActive && !it.isCompleted }
        } catch (e: Exception) { 0 }

        if (incompleteCount > 0) {
            NotificationHelper.fireTaskReminder(appContext, incompleteCount)
        } else {
            NotificationHelper.cancelTaskReminder(appContext)
        }

        return Result.success()
    }
}
