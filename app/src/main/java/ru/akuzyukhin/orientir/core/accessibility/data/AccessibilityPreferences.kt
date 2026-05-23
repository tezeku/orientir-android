package ru.akuzyukhin.orientir.core.accessibility.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.accessibilityDataStore by preferencesDataStore("accessibility_prefs")

@Singleton
class AccessibilityPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val dataStore: DataStore<Preferences>
        get() = context.accessibilityDataStore

    companion object {
        val KEY_PROFILE = stringPreferencesKey("profile")
    }
}
