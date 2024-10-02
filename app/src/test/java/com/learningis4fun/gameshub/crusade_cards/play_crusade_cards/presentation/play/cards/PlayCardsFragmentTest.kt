package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.presentation.play.cards

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.learningis4fun.gameshub.MainCoroutineRule
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.common.data.data_source.FakeCrusadeCardsPreferenceManager
import com.learningis4fun.gameshub.crusade_cards.common.domain.repository.*
import com.learningis4fun.gameshub.crusade_cards.commons.domain.controller.CrusadeComputerCardsController
import com.learningis4fun.gameshub.crusade_cards.commons.domain.controller.CrusadeLoggedUserCardsController
import com.learningis4fun.gameshub.crusade_cards.commons.domain.controller.MultiplayerCrusadeCardsController
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case.*
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.presentation.PlayCardsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
class PlayCardsFragmentTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var playCardsViewModel: PlayCardsViewModel
    private lateinit var defaultRepository: FakeDefaultRepositoryImpl
    private lateinit var fakePreference: FakeCrusadeCardsPreferenceManager
    private lateinit var fakeGameRepository: FakeGameRepositoryImpl
    private lateinit var fakeCardRepository: FakeCardRepositoryImpl
    private lateinit var fakeUserRepository: FakeUserRepositoryImpl
    private lateinit var handleCardStateUseCase: HandleCardStateUseCase
    private lateinit var assignCardUseCase: AssignCardsUseCase
    private lateinit var createGameUseCase: CreateGameUseCase
    private lateinit var resumeGameUseCase: ResumeGameUseCase
    private lateinit var pauseGameUseCase: PauseGameUseCase
    private lateinit var endGameUseCase: EndGameUseCase
    private lateinit var cardsController: MultiplayerCrusadeCardsController
    private lateinit var loggedUserCardsController: CrusadeLoggedUserCardsController
    private lateinit var fakeGameCardRepository: FakeGameCardRepositoryImpl

    private lateinit var computerCardsController: CrusadeComputerCardsController

    @Before
    fun setup() {
        defaultRepository = FakeDefaultRepositoryImpl()
        fakeUserRepository = FakeUserRepositoryImpl()
        fakePreference = FakeCrusadeCardsPreferenceManager()
        fakeGameRepository = FakeGameRepositoryImpl(
            userRepositoryImpl = fakeUserRepository,
        defaultRepositoryImpl = defaultRepository)

        fakeCardRepository = FakeCardRepositoryImpl()

        fakeUserRepository = FakeUserRepositoryImpl()

        fakeGameCardRepository = FakeGameCardRepositoryImpl(
            cardsRepositoryImpl = fakeCardRepository
        )

        computerCardsController = CrusadeComputerCardsController(
            gameRepository = fakeGameRepository,
            cardRepository = fakeCardRepository,
            gameCardRepository = fakeGameCardRepository
        )

        cardsController = MultiplayerCrusadeCardsController(
            gameRepository = fakeGameRepository,
            cardRepository = fakeCardRepository,
            userRepository = fakeUserRepository,
            gameCardRepository = fakeGameCardRepository,
            repository = defaultRepository
        )

        handleCardStateUseCase = HandleCardStateUseCase(
            userRepository = fakeUserRepository,
            defaultCardRepository = defaultRepository,
            cardsController = cardsController,
            computerCardsController = computerCardsController,
            preferenceManager = fakePreference
        )

        assignCardUseCase = AssignCardsUseCase(
            repository = defaultRepository,
            gameCardRepository = fakeGameCardRepository,
            gameRepository = fakeGameRepository
        )

        resumeGameUseCase = ResumeGameUseCase(
            repository = defaultRepository,
            gameRepository = fakeGameRepository
        )

        pauseGameUseCase = PauseGameUseCase(
            gameRepository = fakeGameRepository
        )

        endGameUseCase = EndGameUseCase(
            repository = defaultRepository,
            gameRepository = fakeGameRepository,
            gameCardRepository = fakeGameCardRepository,
            preferenceManager = fakePreference
        )

        createGameUseCase = CreateGameUseCase(
            repository = defaultRepository,
            gameRepository = fakeGameRepository,
            cardRepository = fakeCardRepository,
            gameCardRepository = fakeGameCardRepository
        )

        loggedUserCardsController = CrusadeLoggedUserCardsController(
            handleCardStateUseCase = handleCardStateUseCase,
            gameCardRepository = fakeGameCardRepository,
            multiplayerCrusadeCardsController = cardsController
        )

        playCardsViewModel = PlayCardsViewModel(
            preference = fakePreference,
            gameRepository = fakeGameRepository,
            cardRepository = fakeCardRepository,
            userRepository = fakeUserRepository,
            handleCardStateUseCase = handleCardStateUseCase,
            resumeGameUseCase = resumeGameUseCase,
            pauseGameUseCase = pauseGameUseCase,
            endGameUseCase = endGameUseCase,
            loggedUserCardsController = loggedUserCardsController,
            gameCardRepository = fakeGameCardRepository,
            defaultCoroutineDispatcher = mainCoroutineRule.dispatcher
        )
    }

    @Test
    fun `onAllowOthersPlayBtnClick, change card state to noAvailableCard with same id`() = runBlockingTest {
        fakeUserRepository.insertDefaultPlayers()
        val participants = fakeUserRepository.getPlayers().first()
        val playerId = fakeUserRepository.getUserId()

        playCardsViewModel.onAllowOthersPlayBtnClick()

        val expect = CardStates.NoAvailableCard(
            playerId = playerId
        )
        val actual = fakePreference.recentCardState.first()

        assertThat(actual).isEqualTo(expect)
    }

    @Test
    fun `onUserCardClick, change card state to played`() = runBlockingTest {

        val cards = Constants.Cards.cards
            .filter { it.name == Constants.CardNames.THREE }

        fakeUserRepository.insertDefaultPlayers()
        val participants = fakeUserRepository.getPlayers().first()
        val playerId = fakeUserRepository.getUserId()

        val card = cards.first().copy(id = "id-card")
        fakeCardRepository.insertCard(card)

        val gameId = createGameUseCase.createGame(gameName ="Game-name", participants = participants )

        val gameCard = fakeGameCardRepository.getGameCards(gameId = gameId).first().copy(playerId = playerId)

        fakeGameCardRepository.updateGameCard(gameCard)

        val randomUserCard = fakeGameCardRepository.getUnPlayedCardsForPlayer(
            gameId = gameId,
            playerId = playerId)
            .first()
            .first()

        // user need to have permission to play
        val cardState = CardStates.Play(
            playerId = playerId
        )
        handleCardStateUseCase.saveCardStateToPreferences(cardState)

        playCardsViewModel.onUserCardClick(randomUserCard)

        val expect = CardStates.Played(
            playerId = playerId
        )
        val actual = fakePreference.recentCardState.first()

        assertThat(actual).isEqualTo(expect)
    }

    @Test
    fun `onDustbinClick, change card state to picked`() = runBlockingTest {

        val cards = Constants.Cards.cards.take(6)

        fakeUserRepository.insertDefaultPlayers()
        val participants = fakeUserRepository.getPlayers().first()
        val playerId = fakeUserRepository.getUserId()

        cards.forEachIndexed { index, card ->
            fakeCardRepository.insertCard(card.copy(id = index.toString()))
        }

        val gameId = createGameUseCase.createGame(gameName ="Game-name", participants = participants)
        assignCardUseCase.assignCards(
            gameId = gameId,
            howManyCardsPerPlayer = 2
        )

        // user need to have permission to play
        val cardState = CardStates.Play(
            playerId = playerId
        )
        handleCardStateUseCase.saveCardStateToPreferences(cardState)

        playCardsViewModel.onDustbinClick()

        val expect = CardStates.Pick(
            playerId = playerId
        )
        val actual = fakePreference.recentCardState.first()

        assertThat(actual).isEqualTo(expect)
    }
}