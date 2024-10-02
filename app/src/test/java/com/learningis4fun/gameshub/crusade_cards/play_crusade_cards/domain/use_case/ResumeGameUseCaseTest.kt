package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case

import com.google.common.truth.Truth.assertThat
import com.learningis4fun.gameshub.crusade_cards.common.domain.repository.*
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.GameState
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class ResumeGameUseCaseTest {

    private lateinit var pauseGameUseCase: PauseGameUseCase
    private lateinit var createGameUseCase: CreateGameUseCase
    private lateinit var resumeGameUseCase: ResumeGameUseCase
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
        defaultRepositoryImpl = fakeDefaultRepositoryImpl)
        fakeGameCardRepositoryImpl = FakeGameCardRepositoryImpl(fakeCardRepositoryImpl)

        createGameUseCase = CreateGameUseCase(
            repository = fakeDefaultRepositoryImpl,
            gameRepository = fakeGameRepositoryImpl,
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
    fun `resume game, update game state to started`() = runBlockingTest {
        val gameDurationCount = 50L

        fakeUserRepositoryImpl.insertDefaultPlayers()
        val participants = fakeUserRepositoryImpl.getPlayers().first()

        Constants.Cards.cards.forEachIndexed { index, card ->
            fakeCardRepositoryImpl.insertCard(card.copy(id = index.toString()))
        }

        val gameId = createGameUseCase.createGame("test-game", participants)
        //mainViewModel._gameDuration.value = Pair(true, gameDurationCount)
        pauseGameUseCase.pauseGame(gameId, gameDurationCount)
        resumeGameUseCase.resumeGame(gameId)

        val game = fakeGameRepositoryImpl.getGame(gameId).first() ?: return@runBlockingTest

        assertThat(toState(game.gameState)).isInstanceOf(GameState.Started.javaClass)
        assertThat(game.gameDuration).isEqualTo(gameDurationCount)
    }
}