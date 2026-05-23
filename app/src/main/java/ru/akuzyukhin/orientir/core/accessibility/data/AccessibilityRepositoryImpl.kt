package ru.akuzyukhin.orientir.core.accessibility.data

import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.akuzyukhin.orientir.core.accessibility.domain.model.AccessibilityProfile
import ru.akuzyukhin.orientir.core.accessibility.domain.repository.AccessibilityRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccessibilityRepositoryImpl @Inject constructor(
    private val preferences: AccessibilityPreferences
) : AccessibilityRepository {

    override fun profileFlow(): Flow<AccessibilityProfile> =
        preferences.dataStore.data.map { prefs ->
            AccessibilityProfile.fromName(prefs[AccessibilityPreferences.KEY_PROFILE])
        }

    override suspend fun setProfile(profile: AccessibilityProfile) {
        preferences.dataStore.edit { prefs ->
            prefs[AccessibilityPreferences.KEY_PROFILE] = profile.name
        }
    }
}
