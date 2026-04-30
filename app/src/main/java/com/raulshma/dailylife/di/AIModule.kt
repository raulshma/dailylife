package com.raulshma.dailylife.di

import com.raulshma.dailylife.data.ai.AIFeatureExecutor
import com.raulshma.dailylife.data.ai.LiteRTEngineService
import com.raulshma.dailylife.data.ai.ModelManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Provides
    @Singleton
    fun provideAIFeatureExecutor(
        engineService: LiteRTEngineService,
        modelManager: ModelManager,
    ): AIFeatureExecutor = AIFeatureExecutor(engineService, modelManager)
}
