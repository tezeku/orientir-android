package ru.akuzyukhin.orientir.feature.profile.domain.repository

import ru.akuzyukhin.orientir.feature.profile.domain.model.Profile
import ru.akuzyukhin.orientir.feature.profile.domain.model.ProfileUpdate

/** Контракт работы с профилем текущего пользователя */
interface ProfileRepository {

    /** Получение профиля */
    suspend fun getProfile(): Result<Profile>

    /** Обновление профиля
     */
    suspend fun updateProfile(update: ProfileUpdate): Result<Profile>

    /** Смена пароля */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
}
