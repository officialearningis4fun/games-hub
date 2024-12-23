package com.learningis4fun.gameshub.crusade_cards.feature_start


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.learningis4fun.gameshub.MainCoroutineRule
import com.learningis4fun.gameshub.crusade_cards.common.data.data_source.FakeCrusadeCardsPreferenceManager
import com.learningis4fun.gameshub.crusade_cards.common.domain.repository.*
import com.learningis4fun.gameshub.crusade_cards.commons.domain.controller.CrusadeComputerCardsControllerImpl
import com.learningis4fun.gameshub.crusade_cards.commons.domain.controller.CrusadeLoggedUserCardsController
import com.learningis4fun.gameshub.crusade_cards.commons.domain.controller.MultiplayerCrusadeCardsController
import com.learningis4fun.gameshub.crusade_cards.domain.use_cases.AssignCardsUseCase
import com.learningis4fun.gameshub.crusade_cards.domain.use_cases.CreateGameUseCase
import com.learningis4fun.gameshub.crusade_cards.domain.use_cases.EndGameUseCase
import com.learningis4fun.gameshub.crusade_cards.domain.use_cases.HandleCardStateUseCase
import com.learningis4fun.gameshub.crusade_cards.domain.use_cases.PauseGameUseCase
import com.learningis4fun.gameshub.crusade_cards.domain.use_cases.ResumeGameUseCase
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case.*
import com.learningis4fun.gameshub.crusade_cards.presentation.start.CardsStartViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule

@ExperimentalCoroutinesApi
@ExperimentalStdlibApi
class CardsStartViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var cardsStartViewModel: CardsStartViewModel

    private lateinit var fakeDefaultRepositoryImpl: FakeDefaultRepositoryImpl
    private lateinit var fakePreference: FakeCrusadeCardsPreferenceManager
    private lateinit var fakeUserRepositoryImpl: FakeUserRepositoryImpl
    private lateinit var fakeGameRepositoryImpl: FakeGameRepositoryImpl
    private lateinit var fakeCardRepositoryImpl: FakeCardRepositoryImpl

    private lateinit var fakeGameCardRepositoryImpl: FakeGameCardRepositoryImpl

    private lateinit var crusadeComputerCardsControllerImpl: CrusadeComputerCardsControllerImpl
    private lateinit var cardsController: MultiplayerCrusadeCardsController
    private lateinit var loggedUserCardsController: CrusadeLoggedUserCardsController

    private lateinit var handleCardStateUseCase: HandleCardStateUseCase
    private lateinit var createGameUseCase: CreateGameUseCase

    private lateinit var resumeGameUseCase: ResumeGameUseCase

    private lateinit var pauseGameUseCase: PauseGameUseCase
    private lateinit var endGameUseCase: EndGameUseCase
    private lateinit var assignCardsUseCase: AssignCardsUseCase

    private lateinit var getAllCards: FakeGetAllCardsImpl


    /*@Before
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
            cardRepository = fakeCardRepositoryImpl,
            gameCardRepository = fakeGameCardRepositoryImpl
        )

        assignCardsUseCase = AssignCardsUseCase(
            repository = fakeDefaultRepositoryImpl,
            gameRepository = fakeGameRepositoryImpl,
            gameCardRepository = fakeGameCardRepositoryImpl
        )

        getAllCards = FakeGetAllCardsImpl()

        cardsStartViewModel = CardsStartViewModel(
            repository = fakeDefaultRepositoryImpl,
            createGameUseCase = createGameUseCase,
            assignCardsUseCase = assignCardsUseCase,
            resumeGameUseCase = resumeGameUseCase,
            gameRepository = fakeGameRepositoryImpl,
            userRepository = fakeUserRepositoryImpl,
            gameCardRepository = fakeGameCardRepositoryImpl
        )
    }

    @Test
    fun onChallengeAIBtnClick() = runBlockingTest {

        Constants.Cards.cards.forEachIndexed { index, card ->
            fakeCardRepositoryImpl.insertCard(card.copy(id = index.toString()))
        }
        fakeUserRepositoryImpl.insertDefaultPlayers()
        val participants = fakeUserRepositoryImpl.getPlayers().first()

        val playerId = fakeUserRepositoryImpl.getAndroidUserId()
        val gameId = createGameUseCase.createGame("game", participants)

        cardsStartViewModel.onChallengeAIBtnClick()
        val expect = CardsStartChallengeAIBtnClickTestResult(
            screenEvent = CardsStartViewModel.CardsStartScreenEvents.NavigateToPlayCardsScreen,
            isGameIdEmpty = false,
            hasAssigedCards = true
        )

        val hasAssignedCards = fakeGameCardRepositoryImpl.getUnPlayedCardsForPlayer(
            playerId = playerId,
            gameId = gameId
        )
            .firstOrNull()
            .isNullOrEmpty()
            .not()

        val actual = CardsStartChallengeAIBtnClickTestResult(
            screenEvent = cardsStartViewModel.cardsStartScreenEvents.first(),
            isGameIdEmpty = fakeGameRepositoryImpl.getCurrentGameId().first().isEmpty(),
            hasAssigedCards = hasAssignedCards
        )

        // screen event, created game, assigned cards

        assertThat(actual).isEqualTo(expect)

    }

    @Test
    fun onResumeGameBtnClick() = runBlockingTest {

        Constants.Cards.cards.forEachIndexed { index, card ->
            fakeCardRepositoryImpl.insertCard(card.copy(id = index.toString()))
        }
        fakeUserRepositoryImpl.insertDefaultPlayers()
        val participants = fakeUserRepositoryImpl.getPlayers().first()
        val gameId = createGameUseCase.createGame(gameName = "game", participants = participants)
        pauseGameUseCase.pauseGame(currGameId = gameId, gameDuration = 0L)

        cardsStartViewModel.onResumeGameBtnClick()
        val expect = CardsStartViewModel.CardsStartScreenEvents.NavigateToPlayCardsScreen
        val actual = cardsStartViewModel.cardsStartScreenEvents.first()

        assertThat(actual).isEqualTo(expect)
    }

    @Test
    fun onAccountBtnClick() = runBlockingTest {
        cardsStartViewModel.onAccountBtnClick()
        val expect = CardsStartViewModel.CardsStartScreenEvents.NavigateToAccountScreen
        val actual = cardsStartViewModel.cardsStartScreenEvents.first()

        assertThat(expect).isEqualTo(actual)
    }*/
}