package com.learningis4fun.gameshub.crusade_cards.di

import android.content.Context
import androidx.room.Room
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.local.GamesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {

    @Provides
    @Named("test-db")
    fun providesInMemoryDB(@ApplicationContext context: Context) = Room.inMemoryDatabaseBuilder(
        context,
        GamesDatabase::class.java
    ).allowMainThreadQueries()
        .build()

}