package com.mojealterego.newgpt.di

import android.content.Context
import androidx.room.Room
import com.mojealterego.newgpt.data.local.ChatDatabase
import com.mojealterego.newgpt.data.local.MessageDao
import com.mojealterego.newgpt.data.local.SecureSettings
import com.mojealterego.newgpt.data.repository.ChatRepositoryImpl
import com.mojealterego.newgpt.domain.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideHttpClient(): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; isLenient = true }) }
    }

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ChatDatabase =
        Room.databaseBuilder(context, ChatDatabase::class.java, "newgpt.db").build()

    @Provides fun provideDao(db: ChatDatabase): MessageDao = db.messageDao()

    @Provides @Singleton
    fun provideRepository(impl: ChatRepositoryImpl): ChatRepository = impl

    @Provides fun provideSettings(settings: SecureSettings): SecureSettings = settings
}
