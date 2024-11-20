package com.learningis4fun.gameshub.crusade_cards.commons.domain.controller

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.learningis4fun.gameshub.MainCoroutineRule
import com.learningis4fun.gameshub.crusade_cards.common.data.data_source.FakeCrusadeCardsPreferenceManager
import com.learningis4fun.gameshub.crusade_cards.common.domain.repository.*
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.*
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameCardWithCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case.CreateGameUseCase
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case.HandleCardStateUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@ExperimentalStdlibApi
class CrusadeLoggedUserCardsControllerTest {

    private lateinit var crusadeLoggedUserCardsController: CrusadeLoggedUserCardsController
    private lateinit var handleCardStateUseCase: HandleCardStateUseCase
    private lateinit var fakeDefaultRepositoryImpl: FakeDefaultRepositoryImpl
    private lateinit var fakePreferenceManager: FakeCrusadeCardsPreferenceManager
    private lateinit var multiplayerCrusadeCardsController: MultiplayerCrusadeCardsController
    private lateinit var fakeUserRepositoryImpl: FakeUserRepositoryImpl
    private lateinit var fakeCardRepository: FakeCardRepositoryImpl
    private lateinit var fakeGameCardRepositoryImpl: FakeGameCardRepositoryImpl
    private lateinit var fakeGameRepositoryImpl: FakeGameRepositoryImpl
    private lateinit var computerCardsController: CrusadeComputerCardsControllerImpl
    private lateinit var createGameUseCase: CreateGameUseCase

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        fakeDefaultRepositoryImpl = FakeDefaultRepositoryImpl()
        fakeUserRepositoryImpl = FakeUserRepositoryImpl()
        fakeCardRepository = FakeCardRepositoryImpl()
        fakePreferenceManager = FakeCrusadeCardsPreferenceManager()

        fakeGameRepositoryImpl = FakeGameRepositoryImpl(
            userRepositoryImpl = fakeUserRepositoryImpl,
            defaultRepositoryImpl = fakeDefaultRepositoryImpl
        )

        fakeGameCardRepositoryImpl = FakeGameCardRepositoryImpl(
            cardsRepositoryImpl = fakeCardRepository
        )

        fakeGameRepositoryImpl = FakeGameRepositoryImpl(
            userRepositoryImpl = fakeUserRepositoryImpl,
            defaultRepositoryImpl = fakeDefaultRepositoryImpl
        )

        computerCardsController = CrusadeComputerCardsControllerImpl(
            gameRepository = fakeGameRepositoryImpl,
            cardRepository = fakeCardRepository,
            gameCardRepository = fakeGameCardRepositoryImpl
        )

        multiplayerCrusadeCardsController = MultiplayerCrusadeCardsController(
            gameRepository = fakeGameRepositoryImpl,
            cardRepository = fakeCardRepository,
            userRepository = fakeUserRepositoryImpl,
            gameCardRepository = fakeGameCardRepositoryImpl,
            repository = fakeDefaultRepositoryImpl
        )

        handleCardStateUseCase = HandleCardStateUseCase(
            userRepository = fakeUserRepositoryImpl,
            defaultCardRepository = fakeDefaultRepositoryImpl,
            cardsController = multiplayerCrusadeCardsController,
            computerCardsController = computerCardsController,
            preferenceManager = fakePreferenceManager
        )

        crusadeLoggedUserCardsController = CrusadeLoggedUserCardsController(
            handleCardStateUseCase = handleCardStateUseCase,
            gameCardRepository = fakeGameCardRepositoryImpl,
            multiplayerCrusadeCardsController = multiplayerCrusadeCardsController
        )

        createGameUseCase = CreateGameUseCase(
            repository = fakeDefaultRepositoryImpl,
            gameRepository = fakeGameRepositoryImpl,
            cardRepository = fakeCardRepository,
            gameCardRepository = fakeGameCardRepositoryImpl
        )
    }

    @Test
    fun `user request to allow others play, return no available card with same id`() =
        runBlockingTest {
            val playerId = "id"
            crusadeLoggedUserCardsController.userRequestToAllowOthersPlay(playerId = playerId)
            val expect = CardStates.NoAvailableCard(playerId = playerId)
            val actual = fakePreferenceManager.recentCardState.first()

            assertThat(actual).isEqualTo(expect)
        }

    @Test
    fun `user request to define joker action, with hit, return hit with same id`() =
        runBlockingTest {

            fakeUserRepositoryImpl.insertDefaultPlayers()
            val playerId = fakeUserRepositoryImpl.getUserId()
            val participants = fakeUserRepositoryImpl.getPlayers().first()

            val card = Constants.Cards.cards.random().copy(
                name = Constants.CardNames.JOKER,
                type = Constants.CardTypes.BLACK_JOKER
            )

            fakeCardRepository.insertCard(card)

            val gameId =
                createGameUseCase.createGame(gameName = "game-name", participants = participants)
            fakeGameRepositoryImpl.updateRecentGameId(gameId)

            val gameCard = fakeGameCardRepositoryImpl.getGameCards(gameId = gameId).first()

            crusadeLoggedUserCardsController.userRequestToDefineJokerAction(
                playerId = playerId,
                action = JokerActions.Hit,
                gameCardId = gameCard.id
            )

            val expect = CardStates.Hit(
                playerId = Constants.Players.ANDROID_PLAYER.id,
                cardsToPick = 4
            )
            val actual = fakePreferenceManager.recentCardState.first()

            assertThat(actual).isEqualTo(expect)
        }

    @Test
    fun `user request to play, return played with same id`() = runBlockingTest {

        val card = Constants.Cards.cards.random().copy(
            name = Constants.CardNames.THREE,
            type = Constants.CardTypes.CLUBS
        )

        fakeCardRepository.insertCard(card)

        fakeUserRepositoryImpl.insertDefaultPlayers()
        val playerId = fakeUserRepositoryImpl.getUserId()
        val participants = fakeUserRepositoryImpl.getPlayers().first()
        val gameId =
            createGameUseCase.createGame(gameName = "game-name", participants = participants)

        fakeGameRepositoryImpl.updateRecentGameId(gameId)
        val gameCard = fakeGameCardRepositoryImpl.getGameCards(gameId = gameId)
            .first()
            .copy(playerId = playerId)

        fakeGameCardRepositoryImpl.updateGameCard(gameCard)

        val userCard = GameCardWithCard(
            card = card,
            gameCard = gameCard
        ).toUserCard()

        crusadeLoggedUserCardsController.userRequestToPlay(
            userCard = userCard,
            canPick = true
        )

        val expect = CardStates.Played(
            playerId = playerId
        )
        val actual = fakePreferenceManager.recentCardState.first()

        assertThat(actual).isEqualTo(expect)
    }

    @Test
    fun `user request to pick, return pick with same id and numOfCards`() = runBlockingTest {
        val playerId = "id"

        crusadeLoggedUserCardsController.userRequestToPick(
            playerId = playerId,
            cardsToPick = 1,
            canPlay = true
        )

        val expect = CardStates.Pick(
            playerId = playerId,
            numOfCardsToPick = 1,
            canPlay = true
        )

        val actual = fakePreferenceManager.recentCardState.first()

        assertThat(actual).isEqualTo(expect)
    }

    @Test
    fun `user request to hitBack, return hit with diff id`() = runBlockingTest {

        val card = Constants.Cards.cards.random().copy(
            name = Constants.CardNames.TWO,
            type = Constants.CardTypes.CLUBS,
            id = "id"
        )

        fakeCardRepository.insertCard(card)

        fakeUserRepositoryImpl.insertDefaultPlayers()
        val participants = fakeUserRepositoryImpl.getPlayers().first()

        val gameId = createGameUseCase.createGame(gameName = "game-name", participants = participants)
        val playerId = fakeUserRepositoryImpl.getUserId()

        val recentCard = fakeCardRepository.getCards().first { it.id == card.id }

        fakeGameCardRepositoryImpl.updateGameCard(
        gameCard = fakeGameCardRepositoryImpl.getGameCards(gameId = gameId).first()
        )
        val gameCard = fakeGameCardRepositoryImpl.getGameCards(gameId = gameId).first()

        val userCard = GameCardWithCard(
            card = recentCard,
            gameCard = gameCard
        ).toUserCard()

        crusadeLoggedUserCardsController.userRequestToHitBack(
            userCard = userCard,
            playerId = playerId,
            cardsToPick = 2
        )

        val expect = CardStates.Hit(
            playerId = fakeUserRepositoryImpl.getAndroidUserId(),
            cardsToPick = 4
        )
        val actual = fakePreferenceManager.recentCardState.first()

        assertThat(actual).isEqualTo(expect)
    }

    @Test
    fun `user request to change card type, return changedType with same id`() = runBlockingTest {
        val playerId = "id"

        crusadeLoggedUserCardsController.userRequestToChangeCardType(
            playerId = playerId,
            type = Constants.CardTypes.CLUBS
        )

        val expect = CardStates.ChangedType(
            playerId = playerId
        )

        val actual = fakePreferenceManager.recentCardState.first()

        assertThat(actual).isEqualTo(expect)
    }

    @Test
    fun `user request to pickCards, return success`() = runBlockingTest {

        val playerId = "player-id"

        val cardState = CardStates.Play(
            playerId = playerId
        )

        val actual = crusadeLoggedUserCardsController.userRequestToPickCards(
            currentCardState = cardState,
            playerId = playerId
        )

        val expect = Response.Success::class.java

        assertThat(actual).isInstanceOf(expect)
    }

    @Test
    fun `user request to changeCardTypes, return success`() = runBlockingTest {

        val playerId = "player-id"

        val cardState = CardStates.ChangeType(
            playerId = playerId
        )

        val actual = crusadeLoggedUserCardsController.userRequestToChangeCardType(
            currentCardState = cardState,
            playerId = playerId,
            type = Constants.CardTypes.CLUBS
        )

        val expect = Response.Success::class.java

        assertThat(actual).isInstanceOf(expect)
    }


}