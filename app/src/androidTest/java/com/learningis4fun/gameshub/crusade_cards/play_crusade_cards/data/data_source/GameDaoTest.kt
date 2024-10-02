package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.local.GamesDatabase
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
@MediumTest
@HiltAndroidTest
class GameDaoTest {

    @get:Rule
    var hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named("test-db")
    lateinit var db: GamesDatabase
    private lateinit var gameDao: GameDao

    @Before
    fun setup() {
        hiltAndroidRule.inject()
        gameDao = db.gameDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertGame_success() = runBlockingTest{
        val game = Game(
            id = "id",
            name = "name"
        )
        gameDao.insertGame(game)

        assertThat(gameDao.getGame("id")).isEqualTo(game)

    }

    @Test
    fun updateGame_success() = runBlockingTest{
        val game = Game(
            id = "id",
            name = "name"
        )
        gameDao.insertGame(game)
        val updateGame = Game(
            id = "id",
            name = "name",
            gameDuration = 10L
        )
        gameDao.updateGame(updateGame)

        assertThat(gameDao.getGame("id")).isEqualTo(updateGame)

    }
}