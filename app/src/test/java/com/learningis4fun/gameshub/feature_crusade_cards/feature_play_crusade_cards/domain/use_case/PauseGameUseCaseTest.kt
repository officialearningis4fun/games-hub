package com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.use_case

import com.google.common.truth.Truth.assertThat
import com.learningis4fun.gameshub.feature_crusade_cards.common.domain.repository.*
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.GameState
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.toState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class PauseGameUseCaseTest {

    private lateinit var pauseGameUseCase: PauseGameUseCase
    private lateinit var resumeGameUseCase: ResumeGameUseCase
    private lateinit var createGameUseCase: CreateGameUseCase
    private lateinit var fakeGameRepositoryImpl: FakeGameRepositoryImpl
    private lateinit var fakeDefaultRepositoryImpl: FakeDefaultRepositoryImpl
    private lateinit var fakeGameCardRepositoryImpl: FakeGameCardRepositoryImpl
    private lateinit var fakeUserRepositoryImpl: FakeUserRepositoryImpl
    private lateinit var fakeCardRepositoryImpl: FakeCardRepositoryImpl

    @Before
    fun setup() {
        fakeUserRepositoryImpl = FakeUserRepositoryImpl()
        fakeDefaultRepositoryImpl = FakeDefaultRepositoryImpl()
        fakeCardRepositoryImpl = FakeCardRepositoryImpl()

        fakeGameRepositoryImpl = FakeGameRepositoryImpl(
            userRepositoryImpl = fakeUserRepositoryImpl,
            defaultRepositoryImpl = fakeDefaultRepositoryImpl
        )
        fakeGameCardRepositoryImpl = FakeGameCardRepositoryImpl(fakeCardRepositoryImpl)

        createGameUseCase = CreateGameUseCase(
            repository = fakeDefaultRepositoryImpl,
            gameRepository = fakeGameRepositoryImpl,
            userRepository = fakeUserRepositoryImpl,
            cardRepository = fakeCardRepositoryImpl,
            gameCardRepository = fakeGameCardRepositoryImpl
        )
        pauseGameUseCase = PauseGameUseCase(fakeGameRepositoryImpl)

        resumeGameUseCase = ResumeGameUseCase(
            repository = fakeDefaultRepositoryImpl,
            gameRepository = fakeGameRepositoryImpl
        )
    }

    @Test
    fun `pause game, update game state to paused`() = runBlockingTest {
        val gameDurationCount = 50L

        Constants.Cards.cards.forEachIndexed { index, card ->
            fakeCardRepositoryImpl.insertCard(card.copy(id = index.toString()))
        }
        fakeUserRepositoryImpl.insertDefaultPlayers()
        val participants = fakeUserRepositoryImpl.getPlayers().first()
        val gameId = createGameUseCase.createGame("test-game", participants)
        fakeGameRepositoryImpl.updateRecentGameId(gameId = gameId)

        resumeGameUseCase.resumeGame(currGameId = gameId)

        pauseGameUseCase.pauseGame(gameId, gameDurationCount)

        val expectedGame = fakeGameRepositoryImpl.getGame(gameId = gameId).first()?.copy(
            gameDuration = gameDurationCount
        )

        val game = fakeGameRepositoryImpl.getGame(gameId).first()

        game?.let {
            assertThat(game).isEqualTo(expectedGame)
        }
    }
}