package ru.akuzyukhin.orientir.feature.task.data

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import ru.akuzyukhin.orientir.feature.task.domain.repository.TasksRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TasksModule {

    @Binds
    @Singleton
    abstract fun bindTasksRepository(impl: TasksRepositoryImpl): TasksRepository

    companion object {
        @Provides
        @Singleton
        fun provideTasksApi(retrofit: Retrofit): TasksApi =
            retrofit.create(TasksApi::class.java)
    }
}