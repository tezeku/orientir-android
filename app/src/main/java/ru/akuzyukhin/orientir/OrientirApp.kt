package ru.akuzyukhin.orientir

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ru.akuzyukhin.orientir.core.notification.NotificationChannels

/**
 * Корневой класс приложения.
 */
@HiltAndroidApp
class OrientirApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createAll(this)
    }
}