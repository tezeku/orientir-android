package ru.akuzyukhin.orientir.core.data.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Расширение на [Context], создающее единственный экземпляр DataStore с именем "auth" */
private val Context.authDataStore by preferencesDataStore(name = "auth")

/** Хранилище токенов аутентификации и базовой информации о пользователе */
@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = longPreferencesKey("user_id")
        val ROLE = stringPreferencesKey("role")
    }

    /** Поток с текущим access-токенов. null, если пользователь не залогинен */
    val accessTokenFlow: Flow<String?> = context.authDataStore.data
        .map { prefs -> prefs[Keys.ACCESS_TOKEN] }

    /** Поток с текущей ролью. null, если пользователь не залогинен */
    val roleFlow: Flow<String?> = context.authDataStore.data
        .map { prefs -> prefs[Keys.ROLE] }

    /** Синхронное чтение access-токена */
    suspend fun getAccessToken(): String? =
        context.authDataStore.data.first()[Keys.ACCESS_TOKEN]

    /** Синхронное чтение refresh-токена. Используется аутентификатором */
    suspend fun getRefreshToken(): String? =
        context.authDataStore.data.first()[Keys.REFRESH_TOKEN]

    /** Сохранение всей информации о сессии после успешного логина/регистрации */
    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        userId: Long,
        role: String
    ) {
        context.authDataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = accessToken
            prefs[Keys.REFRESH_TOKEN] = refreshToken
            prefs[Keys.USER_ID] = userId
            prefs[Keys.ROLE] = role
        }
    }

    /** Обновление только пары токенов */
    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        context.authDataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = accessToken
            prefs[Keys.REFRESH_TOKEN] = refreshToken
        }
    }

    /** Полная очистка */
    suspend fun clear() {
        context.authDataStore.edit { it.clear() }
    }
}