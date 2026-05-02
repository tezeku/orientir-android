package ru.akuzyukhin.orientir

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Корневой класс приложения.
 */
@HiltAndroidApp
class OrientirApp : Application() {
    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("OrientirApp", "Application created, Hilt initialized")
    }
}