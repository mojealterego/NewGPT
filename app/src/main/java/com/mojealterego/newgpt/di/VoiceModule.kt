package com.mojealterego.newgpt.di

import com.mojealterego.newgpt.data.voice.AndroidVoiceChatEngine
import com.mojealterego.newgpt.domain.voice.VoiceChatEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VoiceModule {
    @Binds
    @Singleton
    abstract fun bindVoiceChatEngine(engine: AndroidVoiceChatEngine): VoiceChatEngine
}
