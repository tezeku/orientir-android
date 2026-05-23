package ru.akuzyukhin.orientir.feature.reminder.data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.akuzyukhin.orientir.feature.reminder.data.receiver.TaskReminderReceiver
import ru.akuzyukhin.orientir.feature.reminder.data.storage.ReminderStorage
import ru.akuzyukhin.orientir.feature.reminder.domain.model.ReminderInfo
import ru.akuzyukhin.orientir.feature.reminder.domain.repository.ReminderRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val storage: ReminderStorage
) : ReminderRepository {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun schedule(reminder: ReminderInfo) {
        if (reminder.scheduledAt.isBefore(Instant.now())) return

        scheduleAlarm(reminder)
        storage.put(reminder)
    }

    override suspend fun cancel(taskExecutionId: Long) {
        cancelAlarm(taskExecutionId)
        storage.remove(taskExecutionId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun sync(reminders: List<ReminderInfo>) {
        val active = storage.snapshot()
        val incoming = reminders.associateBy { it.taskExecutionId }

        (active.keys - incoming.keys).forEach { cancelAlarm(it) }

        incoming.values.forEach { scheduleAlarm(it) }

        val effective = incoming.values.filter { !it.scheduledAt.isBefore(Instant.now()) }
        storage.replaceAll(effective)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleAlarm(reminder: ReminderInfo) {
        if (reminder.scheduledAt.isBefore(Instant.now())) {
            android.util.Log.w(TAG, "scheduleAlarm: skip past time ${reminder.scheduledAt}")
            return
        }

        val pendingIntent = buildPendingIntent(reminder)
        val triggerMs = reminder.scheduledAt.toEpochMilli()

        if (canScheduleExact()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
        }
    }

    private fun cancelAlarm(taskExecutionId: Long) {
        val pendingIntent = findPendingIntent(taskExecutionId) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun canScheduleExact(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return alarmManager.canScheduleExactAlarms()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildPendingIntent(reminder: ReminderInfo): PendingIntent {
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(TaskReminderReceiver.EXTRA_TASK_EXECUTION_ID, reminder.taskExecutionId)
            putExtra(TaskReminderReceiver.EXTRA_TASK_NAME, reminder.taskName)
            putExtra(TaskReminderReceiver.EXTRA_IMPORTANCE, reminder.importance.name)
            putExtra(TaskReminderReceiver.EXTRA_WINDOW_MINUTES, reminder.windowMinutes)
            putExtra(
                TaskReminderReceiver.EXTRA_SCHEDULED_AT_MILLIS,
                reminder.scheduledAt.toEpochMilli()
            )
        }
        return checkNotNull(
            PendingIntent.getBroadcast(
                context,
                reminder.taskExecutionId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        ) {
            "Failed to create PendingIntent for task execution ${reminder.taskExecutionId}"
        }
    }

    private fun findPendingIntent(taskExecutionId: Long): PendingIntent? {
        val intent = Intent(context, TaskReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            taskExecutionId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun restoreFromStorage() {
        val snapshot = storage.snapshot()
        if (snapshot.isEmpty()) return
        if (!canScheduleExact()) return

        val now = Instant.now()
        snapshot.values.forEach { reminder ->
            if (reminder.scheduledAt.isBefore(now)) return@forEach
            scheduleAlarm(reminder)
        }
    }

    companion object {
        private const val TAG = "ReminderRepository"
    }
}