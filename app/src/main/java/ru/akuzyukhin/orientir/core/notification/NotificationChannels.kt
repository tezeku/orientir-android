package ru.akuzyukhin.orientir.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/** Идентификаторы и фабрика каналов уведомлений приложения */
object NotificationChannels {

    /** Канал критичных напоминаний */
    const val TASK_REMINDERS_CRITICAL = "task_reminders_critical"

    /** Канал обычных напоминаний */
    const val TASK_REMINDERS_DEFAULT = "task_reminders_default"

    fun createAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as? NotificationManager
            ?: return

        val critical = NotificationChannel(
            TASK_REMINDERS_CRITICAL,
            "Критичные напоминания",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Напоминания о задачах высокой важности"
            enableVibration(true)
            enableLights(true)
        }

        val default = NotificationChannel(
            TASK_REMINDERS_DEFAULT,
            "Напоминания",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Напоминания о повседневных задачах"
            enableVibration(true)
        }

        manager.createNotificationChannels(listOf(critical, default))
    }
}