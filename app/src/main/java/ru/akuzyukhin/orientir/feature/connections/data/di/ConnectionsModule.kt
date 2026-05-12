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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConnectionsModule {

    @Provides
    @Singleton
    fun provideConnectionsApi(retrofit: Retrofit): ConnectionsApi = retrofit.create()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ConnectionsBindsModule {

    @Binds
    @Singleton
    abstract fun bindConnectionsRepository(
        impl: ConnectionsRepositoryImpl
    ): ConnectionsRepository
}
