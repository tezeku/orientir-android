package ru.akuzyukhin.orientir.feature.schedule.data

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import ru.akuzyukhin.orientir.feature.schedule.domain.repository.SchedulesRepository
import javax.inject.Singleton

/** Hilt-модуль фичи расписаний */
@Module
@InstallIn(SingletonComponent::class)
abstract class SchedulesModule {

    @Binds
    @Singleton
    abstract fun bindSchedulesRepository(impl: SchedulesRepositoryImpl): SchedulesRepository

    companion object {
        @Provides
        @Singleton
        fun provideSchedulesApi(retrofit: Retrofit): SchedulesApi =
            retrofit.create(SchedulesApi::class.java)
    }
}