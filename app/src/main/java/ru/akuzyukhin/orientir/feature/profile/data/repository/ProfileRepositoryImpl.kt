package ru.akuzyukhin.orientir.feature.profile.data.repository

import ru.akuzyukhin.orientir.feature.profile.data.api.ProfileApi
import ru.akuzyukhin.orientir.feature.profile.data.dto.ChangePasswordRequestDto
import ru.akuzyukhin.orientir.feature.profile.data.mapper.toDomain
import ru.akuzyukhin.orientir.feature.profile.data.mapper.toRequestDto
import ru.akuzyukhin.orientir.feature.profile.domain.model.Profile
import ru.akuzyukhin.orientir.feature.profile.domain.model.ProfileUpdate
import ru.akuzyukhin.orientir.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileApi: ProfileApi
) : ProfileRepository {

    override suspend fun getProfile(): Result<Profile> = runCatching {
        profileApi.getProfile().toDomain()
    }

    override suspend fun updateProfile(update: ProfileUpdate): Result<Profile> = runCatching {
        profileApi.updateProfile(update.toRequestDto()).toDomain()
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> = runCatching {
        profileApi.changePassword(
            ChangePasswordRequestDto(
                currentPassword = currentPassword,
                newPassword = newPassword
            )
        )
    }
}
