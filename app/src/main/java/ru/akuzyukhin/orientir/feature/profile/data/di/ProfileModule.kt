package ru.akuzyukhin.orientir.feature.profile.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import ru.akuzyukhin.orientir.feature.profile.data.api.ProfileApi
import ru.akuzyukhin.orientir.feature.profile.data.repository.ProfileRepositoryImpl
import ru.akuzyukhin.orientir.feature.profile.domain.repository.ProfileRepository
import javax.inject.Singleton

/** Hilt-модуль фичи Profile */
@Module
@InstallIn(SingletonComponent::class)
object ProfileModule {

    @Provides
    @Singleton
    fun provideProfileApi(retrofit: Retrofit): ProfileApi = retrofit.create()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileBindsModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository
}
