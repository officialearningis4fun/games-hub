package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.learningis4fun.gameshub.MainCoroutineRule
import com.learningis4fun.gameshub.crusade_cards.common.domain.repository.*
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.GameUserCrossRef
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.GameCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AssignCardsUseCaseTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeUserRepositoryImpl: FakeUserRepositoryImpl
    private lateinit var fakeGameRepositoryImpl: FakeGameRepositoryImpl
    private lateinit var fakeCardRepositoryImpl: FakeCardRepositoryImpl
    private lateinit var fakeGameCardRepositoryImpl: FakeGameCardRepositoryImpl
    private lateinit var assignCardsUseCase: AssignCardsUseCase
    private lateinit var fakeDefaultRepositoryImpl: FakeDefaultRepositoryImpl

    @Before
    fun setup() {
        fakeUserRepositoryImpl = FakeUserRepositoryImpl()
        fakeDefaultRepositoryImpl = FakeDefaultRepositoryImpl()
        fakeGameRepositoryImpl = FakeGameRepositoryImpl(
            userRepositoryImpl = fakeUserRepositoryImpl,
        defaultRepositoryImpl = fakeDefaultRepositoryImpl)
        fakeCardRepositoryImpl = FakeCardRepositoryImpl()
        fakeGameCardRepositoryImpl = FakeGameCardRepositoryImpl(fakeCardRepositoryImpl)
        assignCardsUseCase = AssignCardsUseCase(
            repository = fakeDefaultRepositoryImpl,
            gameCardRepository = fakeGameCardRepositoryImpl,
            gameRepository = fakeGameRepositoryImpl
        )
    }

    @Test
    fun `assign cards, verify each user assigned cards`() = runBlockingTest {

        val numberOfCardsToAssign = 5
        val userId = "1"
        val otherUserId = "2"
        val playerIds = listOf(userId, otherUserId)
        val gameId = "id"

        playerIds.forEach {
            fakeUserRepositoryImpl.savePlayer(Player(uniqueId = it))
            fakeDefaultRepositoryImpl.insertGameUserCrossRef(
                GameUserCrossRef(gameId = gameId, userId = it)
            )
        }
        val cards = Constants.Cards.cards
        cards.forEachIndexed { index, card ->
            fakeCardRepositoryImpl.insertCard(card.copy(id = "$index"))
        }
        val game = Game(id = gameId, name = "name")
        fakeGameRepositoryImpl.insertGame(game)

        val gameCards = cards.mapIndexed { index, card ->
            GameCard(
                id = "$index",
                gameId = gameId,
                card.id,
            )
        }

        gameCards.forEach {
            fakeGameCardRepositoryImpl.insertGameCard(it)
        }

        assignCardsUseCase.assignCards(
            gameId = gameId,
            howManyCardsPerPlayer = numberOfCardsToAssign
        )

        val numOfUserCards = fakeGameCardRepositoryImpl.numOfUnPlayedCardsForPlayer(
            gameId = gameId,
            playerId = userId
        ).first()

        assertThat(numOfUserCards).isEqualTo(numberOfCardsToAssign)
    }
}