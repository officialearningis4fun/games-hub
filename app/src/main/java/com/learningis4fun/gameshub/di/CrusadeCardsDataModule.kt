package com.learningis4fun.gameshub.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.learningis4fun.gameshub.api.CrusadeGameService
import com.learningis4fun.gameshub.api.CrusadeGameServiceImpl
import com.learningis4fun.gameshub.api.use_cases.AssignCardsUseCase
import com.learningis4fun.gameshub.api.use_cases.GetLatestStageNumber
import com.learningis4fun.gameshub.commons.domain.controller.CrusadeComputerCardsController
import com.learningis4fun.gameshub.commons.domain.controller.CrusadeComputerCardsControllerImpl
import com.learningis4fun.gameshub.commons.domain.repository.CardRepository
import com.learningis4fun.gameshub.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameStageRepository
import com.learningis4fun.gameshub.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.data.data_source.OpenAICrusadeCardsControllerImpl
import com.learningis4fun.gameshub.data.data_source.local.CrusadeCardsPreferencesManagerImpl
import com.learningis4fun.gameshub.data.data_source.local.GamesDatabase
import com.learningis4fun.gameshub.data.data_source.remote.GoogleAuthUiClient
import com.learningis4fun.gameshub.data.repository.CardRepositoryImpl
import com.learningis4fun.gameshub.data.repository.DefaultCardRepositoryImpl
import com.learningis4fun.gameshub.data.repository.GameCardRepositoryImpl
import com.learningis4fun.gameshub.data.repository.GameRepositoryImpl
import com.learningis4fun.gameshub.data.repository.GameStageRepositoryImpl
import com.learningis4fun.gameshub.data.repository.UserRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
object CrusadeCardsDataModule {

    @Provides
    @Singleton
    fun providesGoogleAuthClient(
        @ApplicationContext context: Context,
    ): GoogleAuthUiClient {
        return GoogleAuthUiClient(context)
    }

    @Provides
    @Singleton
    fun providesCrusadeGameService(
        googleAuthUiClient: GoogleAuthUiClient,
        preferencesManager: CrusadeCardsPreferencesManager
    ) = CrusadeGameServiceImpl(
        googleAuthUiClient = googleAuthUiClient,
        preferencesManager = preferencesManager
    ) as CrusadeGameService

    @Provides
    @Singleton
    fun provideHttpClient(
    ): HttpClient {
        return HttpClient(CIO) {
            install(HttpTimeout)
        }
    }

    @Singleton
    @Provides
    fun providePreferencesManager(@ApplicationContext context: Context) =
        CrusadeCardsPreferencesManagerImpl(context) as CrusadeCardsPreferencesManager

    @Singleton
    @Provides
    fun providesRepository(
        db: GamesDatabase,
        crusadeCardsPreferencesManager: CrusadeCardsPreferencesManager
    ) = DefaultCardRepositoryImpl(
        db = db,
        crusadeCardsPreferencesManager = crusadeCardsPreferencesManager
    ) as DefaultCardRepository

    @Singleton
    @Provides
    fun provideCardRepository(
        db: GamesDatabase,
        crusadeCardsPreferencesManager: CrusadeCardsPreferencesManager
    ) = CardRepositoryImpl(
        db = db,
        crusadeCardsPreferencesManager = crusadeCardsPreferencesManager
    ) as CardRepository

    @LocalCrusadeCardController
    @Singleton
    @Provides
    fun provideLocalComputerCardController(
        gameRepository: GameRepository,
        cardRepository: CardRepository,
        gameCardRepository: GameCardRepository,
        gameStageRepository: GameStageRepository,
    ) = CrusadeComputerCardsControllerImpl(
        gameRepository = gameRepository,
        cardRepository = cardRepository,
        gameCardRepository = gameCardRepository,
        gameStageRepository = gameStageRepository,
    ) as CrusadeComputerCardsController

    @OpenAICrusadeCardController
    @Singleton
    @Provides
    fun provideOpenAICardController(
        gameRepository: GameRepository,
        cardRepository: CardRepository,
        gameCardRepository: GameCardRepository,
        gameStageRepository: GameStageRepository,
        getLatestStageNumber: GetLatestStageNumber,
        crusadeCardsPreferencesManager: CrusadeCardsPreferencesManager
    ) = OpenAICrusadeCardsControllerImpl(
        gameRepository = gameRepository,
        cardRepository = cardRepository,
        gameCardRepository = gameCardRepository,
        gameStageRepository = gameStageRepository,
        getLatestStageNumber = getLatestStageNumber,
        preferencesManager = crusadeCardsPreferencesManager
    ) as CrusadeComputerCardsController


    @ExperimentalStdlibApi
    @Singleton
    @Provides
    fun provideGameCardRepository(
        db: GamesDatabase
    ) = GameCardRepositoryImpl(
        db = db
    ) as GameCardRepository

    @Singleton
    @Provides
    fun provideUserRepository(
        db: GamesDatabase,
        crusadeCardsPreferencesManager: CrusadeCardsPreferencesManager
    ) = UserRepositoryImpl(
        db = db,
        crusadeCardsPreferencesManager = crusadeCardsPreferencesManager
    ) as UserRepository

    @Singleton
    @Provides
    fun provideGameRepository(
        db: GamesDatabase,
        crusadeCardsPreferencesManager: CrusadeCardsPreferencesManager
    ) = GameRepositoryImpl(
        db = db,
        crusadeCardsPreferencesManager = crusadeCardsPreferencesManager
    ) as GameRepository

    @Singleton
    @Provides
    fun provideGameStageRepository(
        db: GamesDatabase
    ) = GameStageRepositoryImpl(db = db) as GameStageRepository

    @Singleton
    @Provides
    fun providesCardsDatabase(
        app: Application,
        cardsCallback: GamesDatabase.InsertCardsCallback
    ) = Room.databaseBuilder(app, GamesDatabase::class.java, "games.db")
        .addCallback(cardsCallback)
        .build()

    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @IODispatcher
    @Provides
    fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @DefaultDispatcher
    @Provides
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @ApplicationScope
    @Provides
    fun providesApplicationScope() = CoroutineScope(SupervisorJob())

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class OpenAICrusadeCardController

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class LocalCrusadeCardController

    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class ApplicationScope

    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class MainDispatcher

    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class IODispatcher

    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class DefaultDispatcher
}