package com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.presentation.play.cards

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.learningis4fun.gameshub.MainCoroutineRule
import com.learningis4fun.gameshub.feature_crusade_cards.common.data.data_source.FakeCrusadeCardsPreferenceManager
import com.learningis4fun.gameshub.feature_crusade_cards.common.domain.repository.*
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.controller.CrusadeComputerCardsController
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.controller.CrusadeLoggedUserCardsController
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.controller.MultiplayerCrusadeCardsController
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.use_case.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
class PlayCardsViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var playCardsViewModel: PlayCardsViewModel

    private lateinit var fakeDefaultRepositoryImpl : FakeDefaultRepositoryImpl
    private lateinit var fakePreference: FakeCrusadeCardsPreferenceManager
    private lateinit var fakeUserRepositoryImpl : FakeUserRepositoryImpl
    private lateinit var fakeGameRepositoryImpl : FakeGameRepositoryImpl
    private lateinit var fakeCardRepositoryImpl : FakeCardRepositoryImpl

    private lateinit var fakeGameCardRepositoryImpl: FakeGameCardRepositoryImpl

    private lateinit var crusadeComputerCardsController: CrusadeComputerCardsController
    private lateinit var cardsController: MultiplayerCrusadeCardsController
    private lateinit var loggedUserCardsController: CrusadeLoggedUserCardsController

    private lateinit var handleCardStateUseCase: HandleCardStateUseCase
    private lateinit var createGameUseCase:CreateGameUseCase

    private lateinit var resumeGameUseCase: ResumeGameUseCase

    private lateinit var pauseGameUseCase: PauseGameUseCase
    private lateinit var endGameUseCase: EndGameUseCase


    @Before
    fun setUp() {
        fakeDefaultRepositoryImpl = FakeDefaultRepositoryImpl()
        fakePreference = FakeCrusadeCardsPreferenceManager()
        fakeUserRepositoryImpl = FakeUserRepositoryImpl()
        fakeGameRepositoryImpl = FakeGameRepositoryImpl(
            userRepositoryImpl = fakeUserRepositoryImpl,
            defaultRepositoryImpl = fakeDefaultRepositoryImpl
        )
        fakeCardRepositoryImpl = FakeCardRepositoryImpl()
        fakeGameCardRepositoryImpl = FakeGameCardRepositoryImpl(
            cardsRepositoryImpl = fakeCardRepositoryImpl
        )
        crusadeComputerCardsController = CrusadeComputerCardsController(
            gameRepository = fakeGameRepositoryImpl,
            cardRepository = fakeCardRepositoryImpl,
            gameCardRepository = fakeGameCardRepositoryImpl
        )
        cardsController = MultiplayerCrusadeCardsController(
            gameRepository = fakeGameRepositoryImpl,
            cardRepository = fakeCardRepositoryImpl,
            userRepository = fakeUserRepositoryImpl,
            gameCardRepository = fakeGameCardRepositoryImpl,
            repository = fakeDefaultRepositoryImpl
        )
        handleCardStateUseCase = HandleCardStateUseCase(
            userRepository = fakeUserRepositoryImpl,
            defaultCardRepository = fakeDefaultRepositoryImpl,
            cardsController = cardsController,
            computerCardsController = crusadeComputerCardsController,
            preferenceManager = fakePreference
        )
        loggedUserCardsController = CrusadeLoggedUserCardsController(
            handleCardStateUseCase = handleCardStateUseCase,
            gameCardRepository = fakeGameCardRepositoryImpl,
            multiplayerCrusadeCardsController = cardsController
        )
        resumeGameUseCase = ResumeGameUseCase(
            repository = fakeDefaultRepositoryImpl,
            gameRepository = fakeGameRepositoryImpl
        )
        pauseGameUseCase = PauseGameUseCase(
            gameRepository = fakeGameRepositoryImpl
        )
        endGameUseCase = EndGameUseCase(
            repository = fakeDefaultRepositoryImpl,
            gameRepository = fakeGameRepositoryImpl,
            gameCardRepository = fakeGameCardRepositoryImpl,
            preferenceManager = fakePreference
        )

        createGameUseCase = CreateGameUseCase(
            repository = fakeDefaultRepositoryImpl,
            gameRepository = fakeGameRepositoryImpl,
            userRepository = fakeUserRepositoryImpl,
            cardRepository = fakeCardRepositoryImpl,
            gameCardRepository = fakeGameCardRepositoryImpl
        )
        playCardsViewModel = PlayCardsViewModel(
            repository = fakeDefaultRepositoryImpl,
            gameRepository = fakeGameRepositoryImpl,
            cardRepository = fakeCardRepositoryImpl,
            userRepository = fakeUserRepositoryImpl,
            handleCardStateUseCase = handleCardStateUseCase,
            resumeGameUseCase = resumeGameUseCase,
            pauseGameUseCase = pauseGameUseCase,
            endGameUseCase = endGameUseCase,
            cardsController = cardsController,
            defaultCoroutineDispatcher = mainCoroutineRule.dispatcher,
            gameCardRepository = fakeGameCardRepositoryImpl,
            loggedUserCardsController = loggedUserCardsController,
            preference = fakePreference
        )
    }

    @Test
    fun `start or resume game, start timer from previous duration`() = runBlockingTest{
        val prevDuration = 50L

        Constants.Cards.cards.forEachIndexed { index, card ->
            fakeCardRepositoryImpl.insertCard(card.copy(id = index.toString()))
        }

        fakeUserRepositoryImpl.insertDefaultPlayers()
        val participants = fakeUserRepositoryImpl.getPlayers().first()
        val gameId = createGameUseCase.createGame("game-name", participants)
        fakeGameRepositoryImpl.updateRecentGameId(gameId)
        val game = fakeGameRepositoryImpl.getGame(gameId).first() ?: Game()

        fakeGameRepositoryImpl.updateGame(game.copy(gameDuration = prevDuration))

       val actualDuration = playCardsViewModel.adjustGameTimer(
            currGameId = gameId,
            gameDuration = Pair(true, prevDuration),
            lastDuration = 50
        ).second

        assertThat(actualDuration).isGreaterThan(prevDuration)
    }
}