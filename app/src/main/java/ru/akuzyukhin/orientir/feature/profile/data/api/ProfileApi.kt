package ru.akuzyukhin.orientir.feature.profile.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import ru.akuzyukhin.orientir.feature.profile.data.dto.ChangePasswordRequestDto
import ru.akuzyukhin.orientir.feature.profile.data.dto.ProfileResponseDto
import ru.akuzyukhin.orientir.feature.profile.data.dto.UpdateProfileRequestDto

/** Retrofit-интерфейс эндпоинтов /users/me */
interface ProfileApi {
    /** Получение профиля текущего пользователя */
    @GET("users/me")
    suspend fun getProfile(): ProfileResponseDto

    /** Частичное обновление профиля */
    @PATCH("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequestDto): ProfileResponseDto

    /** Смена пароля */
    @POST("users/me/password")
    suspend fun changePassword(@Body request: ChangePasswordRequestDto)
}