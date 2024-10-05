package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.learningis4fun.gameshub.MainCoroutineRule
import com.learningis4fun.gameshub.crusade_cards.common.domain.repository.*
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.models.CreateGameTestResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CreateGameUseCaseTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeUserRepositoryImpl: FakeUserRepositoryImpl
    private lateinit var fakeGameRepositoryImpl: FakeGameRepositoryImpl
    private lateinit var fakeCardRepositoryImpl: FakeCardRepositoryImpl
    private lateinit var fakeGameCardRepositoryImpl: FakeGameCardRepositoryImpl
    private lateinit var createGameUseCase: CreateGameUseCase
    private lateinit var fakeDefaultRepositoryImpl: FakeDefaultRepositoryImpl

    @Before
    fun setup() {
        fakeDefaultRepositoryImpl = FakeDefaultRepositoryImpl()
        fakeUserRepositoryImpl = FakeUserRepositoryImpl()
        fakeGameRepositoryImpl = FakeGameRepositoryImpl(
            userRepositoryImpl = fakeUserRepositoryImpl,
        defaultRepositoryImpl = fakeDefaultRepositoryImpl)
        fakeCardRepositoryImpl = FakeCardRepositoryImpl()
        fakeGameCardRepositoryImpl = FakeGameCardRepositoryImpl(fakeCardRepositoryImpl)

        createGameUseCase = CreateGameUseCase(
            repository = fakeDefaultRepositoryImpl,
            gameRepository = fakeGameRepositoryImpl,
            cardRepository = fakeCardRepositoryImpl,
            gameCardRepository = fakeGameCardRepositoryImpl
        )
    }

    @Test
    fun `create game with empty cards, verify the players not assigned to game, numOfCards empty`() =
        runBlockingTest {

            val participants = listOf(
                Player(uniqueId = "1"),
                Player(uniqueId = "2")
            )

            participants.forEach {
                fakeUserRepositoryImpl.savePlayer(it)
            }

            val gameId = createGameUseCase.createGame("gameName", participants)

            val expected = CreateGameTestResult(
                gameIdNotEmpty = false,
                numOfGameCards = 0
            )

            val numOfGameCards = fakeGameCardRepositoryImpl.getNumOfCurrentGameCards(gameId = gameId).first()

            val actual = CreateGameTestResult(
                gameIdNotEmpty = gameId.isNotEmpty(),
                numOfGameCards = numOfGameCards
            )

            Truth.assertThat(expected).isEqualTo(actual)
        }

    @Test
    fun `create game with empty participants, verify the players not assigned to game, numOfCards empty`() =
        runBlockingTest {

            val participants = emptyList<Player>()

            participants.forEach {
                fakeUserRepositoryImpl.savePlayer(it)
            }

            val gameId = createGameUseCase.createGame("gameName", participants)

            val expected = CreateGameTestResult(
                gameIdNotEmpty = false,
                numOfGameCards = 0
            )

            val numOfGameCards = fakeGameCardRepositoryImpl.getNumOfCurrentGameCards(gameId = gameId).first()

            val actual = CreateGameTestResult(
                gameIdNotEmpty = gameId.isNotEmpty(),
                numOfGameCards = numOfGameCards
            )

            Truth.assertThat(expected).isEqualTo(actual)
        }

    @Test
    fun `create game, verify players assigned to game, numOfCards equal numOfGameCards`() =
        runBlockingTest {

            val cards = Constants.Cards.cards
            cards.forEach { card ->
                fakeCardRepositoryImpl.insertCard(card)
            }
            val participants = listOf(
                Player(uniqueId = "1"),
                Player(uniqueId = "2")
            )

            participants.forEach {
                fakeUserRepositoryImpl.savePlayer(it)
            }

            val gameId = createGameUseCase.createGame("gameName", participants)

            val expected = CreateGameTestResult(
                gameIdNotEmpty = true,
                numOfGameCards = cards.size
            )

            val numOfGameCards = fakeGameCardRepositoryImpl.getNumOfCurrentGameCards(gameId = gameId).first()

            val actual = CreateGameTestResult(
                gameIdNotEmpty = gameId.isNotEmpty(),
                numOfGameCards = numOfGameCards
            )

            Truth.assertThat(actual).isEqualTo(expected)
        }
}