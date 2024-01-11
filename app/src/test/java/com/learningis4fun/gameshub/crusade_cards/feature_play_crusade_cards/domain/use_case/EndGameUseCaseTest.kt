package com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.domain.use_case

import com.google.common.truth.Truth
import com.learningis4fun.gameshub.crusade_cards.common.data.data_source.FakeCrusadeCardsPreferenceManager
import com.learningis4fun.gameshub.crusade_cards.common.domain.repository.*
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.GameState
import com.learningis4fun.gameshub.models.EndGameTestResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class EndGameUseCaseTest {

    private lateinit var endGameUseCase: EndGameUseCase
    private lateinit var createGameUseCase: CreateGameUseCase
    private lateinit var fakeGameRepositoryImpl: FakeGameRepositoryImpl
    private lateinit var fakeDefaultRepositoryImpl: FakeDefaultRepositoryImpl
    private lateinit var fakePreferenceManager: FakeCrusadeCardsPreferenceManager
    private lateinit var fakeGameCardRepositoryImpl: FakeGameCardRepositoryImpl
    private lateinit var fakeUserRepositoryImpl: FakeUserRepositoryImpl
    private lateinit var fakeCardRepositoryImpl: FakeCardRepositoryImpl

    @Before
    fun setup() {
        fakeUserRepositoryImpl = FakeUserRepositoryImpl()
        fakeDefaultRepositoryImpl = FakeDefaultRepositoryImpl()
        fakeCardRepositoryImpl = FakeCardRepositoryImpl()
        fakePreferenceManager = FakeCrusadeCardsPreferenceManager()
        fakeGameRepositoryImpl = FakeGameRepositoryImpl(
            userRepositoryImpl = fakeUserRepositoryImpl,
        defaultRepositoryImpl = fakeDefaultRepositoryImpl)
        fakeGameCardRepositoryImpl = FakeGameCardRepositoryImpl(fakeCardRepositoryImpl)

        createGameUseCase = CreateGameUseCase(
            repository = fakeDefaultRepositoryImpl,
            gameRepository = fakeGameRepositoryImpl,
            cardRepository = fakeCardRepositoryImpl,
            gameCardRepository = fakeGameCardRepositoryImpl
        )
        endGameUseCase = EndGameUseCase(
            repository = fakeDefaultRepositoryImpl,
            gameCardRepository = fakeGameCardRepositoryImpl,
            gameRepository = fakeGameRepositoryImpl,
            preferenceManager = fakePreferenceManager
        )
    }


    @Test
    fun `end game, update game state to completed and delete all gameCards, delete cardState change playCardState to navigateToWinScreen, `() =
        runBlockingTest {
            val winnerName = "name"
            val expected = EndGameTestResult(
                gameCardsSize = 0,
                gameIdEmpty = true,
                gameState = GameState.Completed.name,
                cardState = CardStates.Start,
                gamePlayersSize = 0
            )

            val gameDurationCount = 50L

            val cards = Constants.Cards.cards
            cards.forEachIndexed { index, card ->
                fakeCardRepositoryImpl.insertCard(card.copy(id = "$index"))
            }

            val participants = listOf(
                Player(uniqueId = "1"),
                Player(uniqueId = "2")
            )

            val gameId = createGameUseCase.createGame("test-game", participants)

            endGameUseCase.endGame(
                currGameId = gameId,
                duration = gameDurationCount,
                winnerId = winnerName
            )

            val gameCardsSize = fakeGameCardRepositoryImpl.getGameCards(gameId).size
            val isGameIdEmpty = fakeGameRepositoryImpl.getCurrentGameId().first().isEmpty()
            val gameState = fakeGameRepositoryImpl.getGame(gameId)
                .first()
                ?.gameState ?: return@runBlockingTest

            val cardState = fakePreferenceManager.recentCardState.first()

            var gamePlayersSize = 2

            val job1 = launch {
                gamePlayersSize = fakeGameRepositoryImpl.getGameWithPlayers(gameId)
                    .first()
                    .players
                    .size
            }

            val actual = EndGameTestResult(
                gameCardsSize = gameCardsSize,
                gameIdEmpty = isGameIdEmpty,
                gameState = gameState,
                cardState = cardState,
                gamePlayersSize = gamePlayersSize
            )

            Truth.assertThat(actual).isEqualTo(expected)
            job1.cancel()
        }
}