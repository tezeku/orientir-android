package ru.akuzyukhin.orientir.core.accessibility.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.akuzyukhin.orientir.core.accessibility.domain.model.AccessibilityProfile

interface AccessibilityRepository {
    fun profileFlow(): Flow<AccessibilityProfile>
    suspend fun setProfile(profile: AccessibilityProfile)
}
