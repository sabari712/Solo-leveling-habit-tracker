package com.example.sololeveling90days.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.sololeveling90days.MainActivity
import com.example.sololeveling90days.R

object NotificationHelper {

    private const val CHANNEL_ID = "task_reminder_channel"
    private const val CHANNEL_NAME = "Daily Task Reminders"
    private const val CHANNEL_DESC = "Reminds you to complete your daily tasks and maintain your streak"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun fireTaskReminder(context: Context, incompleteCount: Int) {
        createNotificationChannel(context)

        val (title, body) = when {
            incompleteCount <= 0 -> return // Nothing to notify
            incompleteCount == 1 -> Pair(
                "\u26A1 Final Push \u2014 1 Task Left!",
                "You're SO close. Complete it now and keep your streak alive!"
            )
            incompleteCount == 2 -> Pair(
                "\uD83D\uDD25 Almost There \u2014 2 Tasks Remaining",
                "Don't let your streak die. Finish those last 2 tasks!"
            )
            incompleteCount <= 4 -> Pair(
                "\uD83D\uDCAA $incompleteCount Tasks Still Pending",
                "Your streak is counting on you. Get it done \u2014 you can do this!"
            )
            else -> Pair(
                "\u23F0 Daily Tasks Incomplete ($incompleteCount left)",
                "Rise up! You have $incompleteCount tasks remaining. Don't fall behind today."
            )
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission not granted yet
        }
    }

    fun cancelTaskReminder(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}
