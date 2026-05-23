package ru.akuzyukhin.orientir.feature.reminder.data.receiver

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ru.akuzyukhin.orientir.MainActivity
import ru.akuzyukhin.orientir.R
import ru.akuzyukhin.orientir.core.notification.NotificationChannels
import ru.akuzyukhin.orientir.feature.task.domain.model.Importance
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** BroadcastReceiver, вызываемый системой AlarmManager в запланированное время напоминания */
class TaskReminderReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val executionId = intent.getLongExtra(EXTRA_TASK_EXECUTION_ID, INVALID_ID)
        if (executionId == INVALID_ID) {
            Log.w(TAG, "onReceive: missing task_execution_id, skip")
            return
        }

        val taskName = intent.getStringExtra(EXTRA_TASK_NAME) ?: return
        val importanceName = intent.getStringExtra(EXTRA_IMPORTANCE) ?: return
        val importance = runCatching { Importance.valueOf(importanceName) }
            .getOrNull() ?: return
        val windowMinutes = intent.getIntExtra(EXTRA_WINDOW_MINUTES, 0)
        val scheduledAtMillis = intent.getLongExtra(EXTRA_SCHEDULED_AT_MILLIS, 0L)

        if (!hasPostNotificationsPermission(context)) {
            Log.w(TAG, "onReceive: POST_NOTIFICATIONS not granted, skip")
            return
        }

        val channelId = if (importance == Importance.CRITICAL) {
            NotificationChannels.TASK_REMINDERS_CRITICAL
        } else {
            NotificationChannels.TASK_REMINDERS_DEFAULT
        }

        val priority = NotificationCompat.PRIORITY_HIGH

        val body = buildBody(scheduledAtMillis, windowMinutes)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Пора выполнить: $taskName")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(buildContentIntent(context, executionId))
            .addAction(
                R.drawable.ic_check,
                "Выполнено",
                buildCompleteActionIntent(context, executionId)
            )
            .build()

        NotificationManagerCompat.from(context).notify(executionId.toInt(), notification)
    }

    private fun buildContentIntent(context: Context, executionId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            executionId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildCompleteActionIntent(context: Context, executionId: Long): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_COMPLETE
            putExtra(NotificationActionReceiver.EXTRA_TASK_EXECUTION_ID, executionId)
        }
        return checkNotNull(
            PendingIntent.getBroadcast(
                context,
                (executionId + ACTION_REQUEST_CODE_OFFSET).toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        ) {
            "Failed to create PendingIntent for complete action $executionId"
        }
    }

    private fun hasPostNotificationsPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildBody(scheduledAtMillis: Long, windowMinutes: Int): String {
        val timeText = if (scheduledAtMillis > 0L) {
            TIME_FORMATTER.format(Instant.ofEpochMilli(scheduledAtMillis))
        } else {
            ""
        }
        return when {
            timeText.isNotEmpty() && windowMinutes > 0 ->
                "Запланировано на $timeText, окно $windowMinutes мин."
            timeText.isNotEmpty() -> "Запланировано на $timeText"
            else -> "Запланированная задача"
        }
    }

    companion object {
        const val EXTRA_TASK_EXECUTION_ID = "task_execution_id"
        const val EXTRA_TASK_NAME = "task_name"
        const val EXTRA_IMPORTANCE = "importance"
        const val EXTRA_WINDOW_MINUTES = "window_minutes"
        const val EXTRA_SCHEDULED_AT_MILLIS = "scheduled_at_millis"

        private const val INVALID_ID = Long.MIN_VALUE
        private const val TAG = "TaskReminderReceiver"

        private const val ACTION_REQUEST_CODE_OFFSET = 1_000_000_000L

        @RequiresApi(Build.VERSION_CODES.O)
        private val TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("HH:mm")
                .withZone(ZoneId.systemDefault())
    }
}