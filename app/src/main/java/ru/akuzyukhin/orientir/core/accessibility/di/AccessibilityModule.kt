package ru.akuzyukhin.orientir.core.accessibility.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.akuzyukhin.orientir.core.accessibility.data.AccessibilityRepositoryImpl
import ru.akuzyukhin.orientir.core.accessibility.domain.repository.AccessibilityRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AccessibilityModule {

    @Binds
    @Singleton
    abstract fun bindAccessibilityRepository(
        impl: AccessibilityRepositoryImpl
    ): AccessibilityRepository
}
