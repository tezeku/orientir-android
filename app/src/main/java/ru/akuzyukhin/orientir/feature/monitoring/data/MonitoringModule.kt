package ru.akuzyukhin.orientir.feature.monitoring.data

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import ru.akuzyukhin.orientir.feature.monitoring.domain.repository.MonitoringRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MonitoringModule {

    @Binds
    @Singleton
    abstract fun bindMonitoringRepository(impl: MonitoringRepositoryImpl): MonitoringRepository

    companion object {
        @Provides
        @Singleton
        fun provideMonitoringApi(retrofit: Retrofit): MonitoringApi =
            retrofit.create(MonitoringApi::class.java)
    }
}