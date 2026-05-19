package ru.akuzyukhin.orientir.feature.connections.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import ru.akuzyukhin.orientir.feature.connections.data.api.ConnectionsApi
import ru.akuzyukhin.orientir.feature.connections.data.repository.ConnectionsRepositoryImpl
import ru.akuzyukhin.orientir.feature.connections.domain.repository.ConnectionsRepository
import ru.akuzyukhin.orientir.feature.statistics.data.api.StatisticsApi
import ru.akuzyukhin.orientir.feature.statistics.domain.repository.StatisticsRepository
import ru.akuzyukhin.orientir.feature.statistics.domain.repository.StatisticsRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConnectionsModule {

    @Provides
    @Singleton
    fun provideConnectionsApi(retrofit: Retrofit): ConnectionsApi = retrofit.create()

    @Provides
    @Singleton
    fun provideStatisticsApi(retrofit: Retrofit): StatisticsApi =
        retrofit.create(StatisticsApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ConnectionsBindsModule {

    @Binds
    @Singleton
    abstract fun bindConnectionsRepository(
        impl: ConnectionsRepositoryImpl
    ): ConnectionsRepository

    @Binds
    @Singleton
    abstract fun bindStatisticsRepository(
        impl: StatisticsRepositoryImpl
    ): StatisticsRepository
}
