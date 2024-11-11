package com.learningis4fun.gameshub.data.local.daos/*


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.learningis4fun.gameshub.feature_crusade_cards.commons.data.data_source.local.GamesDatabase
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.data.data_source.GameUserCrossRefDao
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.GameCard
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.User
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.GameState
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
@SmallTest
@HiltAndroidTest
class GameUserCrossRefDaoTest {

    @get:Rule
    var hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named("test-db")
    lateinit var db: GamesDatabase
    private lateinit var gameUserCrossRefDao: GameUserCrossRefDao

    @Before
    fun setup() {
        hiltAndroidRule.inject()
        gameUserCrossRefDao = db.cardsDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    */
/**
     * testing Inserting
     *//*

    @Test
    fun insertPlayer() = runBlockingTest {
        val user = User(
            id = UUID.randomUUID().toString(),
            name = "name"
        )
        gameUserCrossRefDao.insertPlayer(user)

        val getPlayers = gameUserCrossRefDao.getPlayers().first()
        assertThat(getPlayers).contains(user)
    }

    @Test
    fun insertGame() = runBlockingTest {
        val gameId = "gameId"
        val game = Game(
            id = gameId,
            name = "name"
        )
        gameUserCrossRefDao.insertGame(game)

        val gameInDb = gameUserCrossRefDao.getGame(gameId)
        assertThat(gameInDb).isEqualTo(game)
    }

    @Test
    fun insertCard() = runBlockingTest {
        val cardId = "cardId"

        val card = Card(
            id = cardId,
            name = "name",
            type = "type",
        )

        gameUserCrossRefDao.insertCard(card)

        val cards = gameUserCrossRefDao.getCards()
        assertThat(cards).contains(card)
    }

    @Test
    fun insertGameCard() = runBlockingTest {
        val gameId = "gameId"
        val cardId = "cardId"
        val gameCardId = "gameCardId"

        val game = Game(
            id = gameId,
            name = "name"
        )
        val card = Card(
            id = cardId,
            name = "name",
            type = "type",
        )

        val gameCard = GameCard(
            id = gameCardId,
            gameId = gameId,
            cardId = cardId
        )

        gameUserCrossRefDao.insertGame(game)
        gameUserCrossRefDao.insertCard(card)
        gameUserCrossRefDao.insertGameCard(gameCard)

        val gameCards = gameUserCrossRefDao.getCardsInDustbin(gameId = gameId).first().map { it.gameCard }
        assertThat(gameCards).contains(gameCard)
    }

    */
/**
     * testing updating
     *//*

    @Test
    fun updatePlayer() = runBlockingTest {
        val user = User(
            id = UUID.randomUUID().toString(),
            name = "name"
        )
        val updatedUser = user.copy(name = "updated-name")
        gameUserCrossRefDao.insertPlayer(user)
        gameUserCrossRefDao.updatePlayer(updatedUser)

        val getPlayers = gameUserCrossRefDao.getPlayers().first()
        assertThat(getPlayers).contains(updatedUser)
    }

    @Test
    fun updateGame() = runBlockingTest {
        val gameId = "gameId"
        val game = Game(
            id = gameId,
            name = "name"
        )
        val updatedGame = game.copy(name = "updated-name", gameState = GameState.Completed.name)
        gameUserCrossRefDao.insertGame(game)
        gameUserCrossRefDao.updateGame(updatedGame)

        val gameInDb = gameUserCrossRefDao.getGame(gameId)
        assertThat(gameInDb).isEqualTo(updatedGame)
    }

    @Test
    fun updateCard() = runBlockingTest {
        val cardId = "cardId"

        val card = Card(
            id = cardId,
            name = "name",
            type = "type",
        )

        val updatedCard = card.copy(name = "updated-type")

        gameUserCrossRefDao.insertCard(card)
        gameUserCrossRefDao.updateCard(updatedCard)

        val cards = gameUserCrossRefDao.getCards()
        assertThat(cards).contains(updatedCard)
    }

    @Test
    fun updateGameCard() = runBlockingTest {
        val gameId = "gameId"
        val cardId = "cardId"
        val gameCardId = "gameCardId"

        val game = Game(
            id = gameId,
            name = "name"
        )
        val card = Card(
            id = cardId,
            name = "name",
            type = "type",
        )

        val gameCard = GameCard(
            id = gameCardId,
            gameId = gameId,
            cardId = cardId
        )
        val playTime = System.currentTimeMillis()
        val updatedGameCard = gameCard.copy(playTime = playTime)

        gameUserCrossRefDao.insertGame(game)
        gameUserCrossRefDao.insertCard(card)
        gameUserCrossRefDao.insertGameCard(gameCard)
        gameUserCrossRefDao.updateGameCard(updatedGameCard)

        val gameCards = gameUserCrossRefDao.getCardsInDustbin(gameId = gameId).first().map { it.gameCard }
        assertThat(gameCards).contains(updatedGameCard)
    }

    */
/**
     * testing deleting
     *//*

    @Test
    fun deletePlayer() = runBlockingTest {
        val user = User(
            id = UUID.randomUUID().toString(),
            name = "name"
        )
        gameUserCrossRefDao.insertPlayer(user)
        gameUserCrossRefDao.deletePlayer(user)

        val getPlayers = gameUserCrossRefDao.getPlayers().first()
        assertThat(getPlayers).isEmpty()
    }

    @Test
    fun deleteGame() = runBlockingTest {
        val gameId = "gameId"
        val game = Game(
            id = gameId,
            name = "name"
        )
        gameUserCrossRefDao.insertGame(game)
        gameUserCrossRefDao.deleteGame(game)

        val gameInDb = gameUserCrossRefDao.getGame(gameId)
        assertThat(gameInDb).isNull()
    }

    @Test
    fun deleteCard() = runBlockingTest {
        val cardId = "cardId"

        val card = Card(
            id = cardId,
            name = "name",
            type = "type",
        )

        gameUserCrossRefDao.insertCard(card)
        gameUserCrossRefDao.deleteCard(card)

        val cards = gameUserCrossRefDao.getCards()
        assertThat(cards).isEmpty()
    }

    @Test
    fun deleteGameCard() = runBlockingTest {
        val gameId = "gameId"
        val cardId = "cardId"
        val gameCardId = "gameCardId"

        val game = Game(
            id = gameId,
            name = "name"
        )
        val card = Card(
            id = cardId,
            name = "name",
            type = "type",
        )

        val gameCard = GameCard(
            id = gameCardId,
            gameId = gameId,
            cardId = cardId
        )

        gameUserCrossRefDao.insertGame(game)
        gameUserCrossRefDao.insertCard(card)
        gameUserCrossRefDao.insertGameCard(gameCard)
        gameUserCrossRefDao.deleteGameCard(gameCard)

        val gameCards = gameUserCrossRefDao.getCardsInDustbin(gameId = gameId).first().map {
            it.gameCard
        }
        assertThat(gameCards).isEmpty()
    }

    */
/**
     * Testing querying of complex data from db
     *//*

    @Test
    fun numOfUnPlayedCardsForPlayer() = runBlockingTest {
        val gameId = "gameId"
        val playerId = "1"
        val numberOfCards = 4

        val game = Game(
            id = gameId,
            name = "name"
        )

        gameUserCrossRefDao.insertGame(game)

        for (i in 1..numberOfCards) {
            val card = Card(
                id = "cardId$i",
                name = "name",
                type = "type",
            )

            val gameCard = GameCard(
                id = "gameCardId$i",
                gameId = gameId,
                cardId = card.id,
                playerId = playerId
            )
            gameUserCrossRefDao.insertCard(card)
            gameUserCrossRefDao.insertGameCard(gameCard)
        }

        val numOfUnPlayedCardsFroPlayer =
            gameUserCrossRefDao.numOfUnPlayedCardsForPlayer(gameId, playerId).first()
        assertThat(numOfUnPlayedCardsFroPlayer).isEqualTo(numberOfCards)

    }

    @Test
    fun getTopThreePlayedCards() = runBlockingTest {
        val gameId = "gameId"
        val playerId = "1"
        val numberOfCards = 5

        val playerIds = listOf("2", "1")

        val expectedSize = 3

        val game = Game(
            id = gameId,
            name = "name"
        )

        gameUserCrossRefDao.insertGame(game)

        for (i in 1..numberOfCards) {
            val card = Card(
                id = "cardId$i",
                name = "name",
                type = "type",
            )

            val gameCard = GameCard(
                id = "gameCardId$i",
                gameId = gameId,
                cardId = card.id,
                playerId = playerId,
                isPlayed = true,
                playTime = System.currentTimeMillis()
            )
            gameUserCrossRefDao.insertCard(card)
            gameUserCrossRefDao.insertGameCard(gameCard)
        }

        val topThreeCards = gameUserCrossRefDao.getTopThreePlayedCards(gameId, playerIds).first().map { it.gameCard }
        val firstCardTime = topThreeCards.first().playTime
        val secondCardTime = topThreeCards[1].playTime
        val lastCardTime = topThreeCards.last().playTime
        assertThat(topThreeCards.size).isAtMost(expectedSize)
        //assertThat(topThreeCards).isInOrder(Comparator.comparingLong(GameCard::playTime))
        assertThat(secondCardTime).isLessThan(firstCardTime)
        assertThat(lastCardTime).isLessThan(secondCardTime)
    }

    //non completed game

    // gameUserCrossRef

    // gameWithPlayers

}*/
