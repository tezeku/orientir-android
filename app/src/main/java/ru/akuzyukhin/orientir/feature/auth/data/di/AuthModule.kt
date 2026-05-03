package ru.akuzyukhin.orientir.feature.auth.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import ru.akuzyukhin.orientir.feature.auth.data.api.AuthApi
import ru.akuzyukhin.orientir.feature.auth.domain.repository.AuthRepository
import ru.akuzyukhin.orientir.feature.auth.domain.repository.AuthRepositoryImpl
import javax.inject.Singleton

/** Hilt-модуль фичи auth */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthBindsModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}