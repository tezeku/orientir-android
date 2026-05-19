package ru.akuzyukhin.orientir.feature.reminder.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.akuzyukhin.orientir.feature.reminder.data.scheduler.ReminderRepositoryImpl
import ru.akuzyukhin.orientir.feature.reminder.domain.repository.ReminderRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderModule {

    @Binds
    @Singleton
    abstract fun bindReminderRepository(
        impl: ReminderRepositoryImpl
    ): ReminderRepository
}