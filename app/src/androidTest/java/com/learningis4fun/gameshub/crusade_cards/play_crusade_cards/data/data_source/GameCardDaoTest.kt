package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.learningis4fun.gameshub.data.data_source.GameCardDao
import com.learningis4fun.gameshub.data.data_source.local.GamesDatabase
import com.learningis4fun.gameshub.domain.model.GameCard
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
class GameCardDaoTest {

    @get:Rule
    var hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named("test-db")
    lateinit var db: GamesDatabase
    private lateinit var gameCardDao: GameCardDao

    @Before
    fun setup() {
        hiltAndroidRule.inject()
        gameCardDao = db.gameCardDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertGameCard_success() = runBlockingTest {
        val gameCard = GameCard(
            id = "id",
            gameId = "gameId",
            cardId = "cardId",
        )
        gameCardDao.insertGameCard(gameCard)

        assertThat(gameCardDao.getGameCards(gameId = "gameId", stageNumber = 1)).contains(gameCard)
    }

    @Test
    fun updateGameCard_success() = runBlockingTest {
        val gameCard = GameCard(
            id = "id",
            gameId = "gameId",
            cardId = "cardId",
            isPlayed = false
        )
        gameCardDao.insertGameCard(gameCard)

        val newGameCard = gameCard.copy(isPlayed = true)
        gameCardDao.updateGameCard(newGameCard)

        assertThat(gameCardDao.getGameCards(gameId = "gameId", stageNumber = 1)).contains(
            newGameCard
        )
        assertThat(gameCardDao.getGameCards(gameId = "gameId", stageNumber = 1)).doesNotContain(
            gameCard
        )
    }

    @Test
    fun deleteGameCard_success() = runBlockingTest {
        val gameCard = GameCard(
            id = "id",
            gameId = "gameId",
            cardId = "cardId",
        )
        gameCardDao.insertGameCard(gameCard)
        gameCardDao.deleteGameCard(gameCard)

        assertThat(gameCardDao.getGameCards(gameId = "gameId", stageNumber = 1)).doesNotContain(
            gameCard
        )
    }
}