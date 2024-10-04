package com.learningis4fun.gameshub.crusade_cards.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.local.CrusadeCardsPreferencesManagerImpl
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.local.GamesDatabase
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.remote.GamesHubAPI
import com.learningis4fun.gameshub.crusade_cards.commons.data.repository.*
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.*
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.start.domain.use_cases.GetAllCards
import com.learningis4fun.gameshub.crusade_cards.start.domain.use_cases.GetAllCardsImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
object CrusadeCardsDataModule {

    @Singleton
    @Provides
    fun providesGamesHubAPI(): GamesHubAPI = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(Constants.TEST_BASE_URL)
        .build()
        .create(GamesHubAPI::class.java)

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

    @Singleton
    @Provides
    fun provideGetAllCards(
        gamesHubAPI: GamesHubAPI
    ) = GetAllCardsImpl(
        gamesHubAPI = gamesHubAPI
    ) as GetAllCards

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
    fun providesCardsDatabase(
        app: Application,
        cardsCallback: GamesDatabase.InsertCardsCallback
    ) = Room.databaseBuilder(app, GamesDatabase::class.java, "games.db")
        .addCallback(cardsCallback)
        .build()

    @DefaultGlideInstance
    @Singleton
    @Provides
    fun providesDefaultGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions().placeholder(R.color.white)
    )

    @CardGlideInstance
    @Singleton
    @Provides
    fun providesCardGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions().placeholder(R.drawable.card_base)
    )

    @ProfileGlideInstance
    @Singleton
    @Provides
    fun providesProfileGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions().placeholder(R.drawable.game_test_profile)
    )

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


    // different glide instances
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class CardGlideInstance

    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class DefaultGlideInstance

    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class ProfileGlideInstance
}