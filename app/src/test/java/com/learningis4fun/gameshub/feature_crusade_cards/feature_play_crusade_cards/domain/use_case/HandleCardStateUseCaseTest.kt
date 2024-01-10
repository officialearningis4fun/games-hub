package com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.use_case

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.learningis4fun.gameshub.MainCoroutineRule
import com.learningis4fun.gameshub.feature_crusade_cards.common.data.data_source.FakeCrusadeCardsPreferenceManager
import com.learningis4fun.gameshub.feature_crusade_cards.common.domain.repository.*
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.controller.CrusadeComputerCardsController
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.controller.MultiplayerCrusadeCardsController
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.CardStatesDefaultMsg
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.mappers.toGameCard
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.GameCard
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.GamePlayerActions
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.UserCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
class HandleCardStateUseCaseTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var handleCardStateUseCase: HandleCardStateUseCase
    private lateinit var fakeCrusadeCardsPreferenceManager: FakeCrusadeCardsPreferenceManager
    private lateinit var createGameUseCase: CreateGameUseCase
    private lateinit var fakeDefaultRepositoryImpl: FakeDefaultRepositoryImpl
    private lateinit var fakeGameRepositoryImpl: FakeGameRepositoryImpl
    private lateinit var fakeCardRepositoryImpl: FakeCardRepositoryImpl
    private lateinit var fakeGameCardRepositoryImpl: FakeGameCardRepositoryImpl
    private lateinit var fakeUserRepositoryImpl: FakeUserRepositoryImpl
    private lateinit var crusadeComputerCardsController: CrusadeComputerCardsController

    @Before
    fun setup() {
        fakeDefaultRepositoryImpl = FakeDefaultRepositoryImpl()
        fakeUserRepositoryImpl = FakeUserRepositoryImpl()
        fakeCrusadeCardsPreferenceManager = FakeCrusadeCardsPreferenceManager()
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

        handleCardStateUseCase = HandleCardStateUseCase(
            userRepository = fakeUserRepositoryImpl,
            defaultCardRepository = fakeDefaultRepositoryImpl,
            cardsController = MultiplayerCrusadeCardsController(
                repository = fakeDefaultRepositoryImpl,
                gameRepository = fakeGameRepositoryImpl,
                cardRepository = fakeCardRepositoryImpl,
                gameCardRepository = fakeGameCardRepositoryImpl,
                userRepository = fakeUserRepositoryImpl
            ),
            computerCardsController = CrusadeComputerCardsController(
                gameRepository = fakeGameRepositoryImpl,
                cardRepository = fakeCardRepositoryImpl,
                gameCardRepository = fakeGameCardRepositoryImpl
            ),
            preferenceManager = fakeCrusadeCardsPreferenceManager
        )

        createGameUseCase = CreateGameUseCase(
            repository = fakeDefaultRepositoryImpl,
            gameRepository = fakeGameRepositoryImpl,
            userRepository = fakeUserRepositoryImpl,
            cardRepository = fakeCardRepositoryImpl,
            gameCardRepository = fakeGameCardRepositoryImpl
        )
    }

    @Test
    fun `handle card state com from chance, update the listener to play with same id`() =
        runBlockingTest {
            val playerId = Constants.Players.ANDROID_PLAYER.id
            val expectedState = CardStates.Play(
                playerId = playerId
            )

            val initialState = CardStates.Chance(
                playerId = playerId
            )
            // give a computer a card
            // start game
            handleCardStateUseCase.handleCardState(initialState)

            val resultState = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(resultState).isEqualTo(expectedState)
        }

    @Test
    fun `handle card state user from chance, update the listener to play with same id`() =
        runBlockingTest {
            val playerId = Constants.Players.GUEST_PLAYER.id
            val expectedState = CardStates.Play(
                playerId = playerId
            )

            val initialState = CardStates.Chance(
                playerId = playerId
            )
            handleCardStateUseCase.handleCardState(initialState)

            val resultState = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(resultState).isEqualTo(expectedState)
        }

    @Test
    fun `handle card state com from define-card-action, update the listener to hit with diff id or change-type with same id`() =
        runBlockingTest {
            val playerId = Constants.Players.ANDROID_PLAYER.id
            val expectedPlayerId = Constants.Players.GUEST_PLAYER.id

            val expectedHitState =
                CardStates.Hit(
                    playerId = expectedPlayerId,
                    cardsToPick = 4
                )
            val expectedChangeTypeState =
                CardStates.ChangeType(playerId = playerId)

            val expectedStates = listOf(expectedHitState, expectedChangeTypeState)

            val card = Constants.TestData.card1.copy(
                name = Constants.CardNames.JOKER,
                type = Constants.CardTypes.BLACK_JOKER
            )
            val userCard = UserCard(
                gameCardId = "gameCardId",
                playerId = playerId,
                cardId = card.id,
                name = card.name,
                gameId = "gameId",
                type = card.type,
                isPlayed = true
            )

            val initialState = CardStates.DefineCardAction(
                playerId = userCard.playerId,
                gameCardId = userCard.gameCardId
            )
            // give a computer a card
            // start game
            //handleCardStateUseCase.startACardsGame("gameName")
            handleCardStateUseCase.handleCardState(initialState)

            val resultState = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(expectedStates).contains(resultState)
        }

    @ExperimentalStdlibApi
    @Test
    fun `handle card state user from define-card-action, update playCardEvents to PrompUserDefineCardAction with same id`() =
        runBlockingTest {
            val playerId = Constants.Players.GUEST_PLAYER.id

            val card = Constants.TestData.card1.copy(
                name = Constants.CardNames.JOKER,
                type = Constants.CardTypes.BLACK_JOKER
            )
            val userCard = UserCard(
                gameCardId = "gameCardId",
                playerId = playerId,
                cardId = card.id,
                name = card.name,
                gameId = "gameId",
                type = card.type,
                isPlayed = true
            )

            val expected = GamePlayerActions.PromptPlayerDefineJokerCardAction(
                playerId = playerId,
                gameCardId = userCard.gameCardId
            )

            val initialState = CardStates.DefineCardAction(
                playerId = userCard.playerId,
                gameCardId = userCard.gameCardId
            )
            // give a computer a card
            // start game

            val actual = handleCardStateUseCase.handleCardState(initialState)

            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `handle card state com from hit with valid card, update the listener to hit with diff id`() =
        runBlockingTest {
            val playerId = Constants.Players.ANDROID_PLAYER.id

            val expectedState = CardStates.Hit(
                playerId = Constants.Players.GUEST_PLAYER.id,
                cardsToPick = 6
            )
            val startingNumOfCardToPick = 2
            fakeUserRepositoryImpl.insertDefaultPlayers()
            val participants = fakeUserRepositoryImpl.getPlayers().first()

            val card = Constants.TestData.card1.copy(
                name = Constants.CardNames.JOKER,
                type = Constants.CardTypes.BLACK_JOKER
            )
            fakeCardRepositoryImpl.insertCard(card)

            val gameId = createGameUseCase.createGame("game", participants)

            val gameCard = GameCard(
                id = "game-card-id",
                gameId = gameId,
                playerId = playerId,
                cardId = card.id)

            fakeGameCardRepositoryImpl.insertGameCard(gameCard)

            val initialState = CardStates.Hit(
                playerId = playerId,
                cardsToPick = startingNumOfCardToPick
            )
            handleCardStateUseCase.handleCardState(initialState)

            val resultState = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(resultState).isEqualTo(expectedState)
        }

    @Test
    fun `handle card state user from hit, promptUserHitBackOrPick`() =
        runBlockingTest {
            val playerId = Constants.Players.GUEST_PLAYER.id
            val expectedNumOfCardsToPick = 6

            val expectedState = GamePlayerActions.PromptPlayerHitBackOrPick(
                playerId = playerId,
                cardsToPick = expectedNumOfCardsToPick,
                message = handleCardStateUseCase.promptUserHitBackOrPickMessage(
                    expectedNumOfCardsToPick
                )
            )

            val initialState = CardStates.Hit(
                playerId = playerId,
                cardsToPick = expectedNumOfCardsToPick
            )

            val job0 = launch {
                handleCardStateUseCase.handleCardState(initialState)
            }

            val resultState = handleCardStateUseCase.handleCardState(initialState)

            assertThat(resultState).isEqualTo(expectedState)
        }

    @Test
    fun `handle card state com from hit with invalid card, update the listener to pick with same id`() =
        runBlockingTest {
            val playerId = Constants.Players.ANDROID_PLAYER.id

            val expectedNumOfCardsToPick = 2

            val expectedState = CardStates.Pick(
                playerId = playerId,
                numOfCardsToPick = expectedNumOfCardsToPick,
                canPlay = false
            )

            val card = Constants.TestData.card1.copy(
                name = Constants.CardNames.THREE,
                type = Constants.CardTypes.CLUBS
            )
            val userCard = UserCard(
                gameCardId = "gameCardId",
                playerId = playerId,
                cardId = card.id,
                name = card.name,
                gameId = "gameId",
                type = card.type,
                isPlayed = false
            )

            // give com a joker
            // create game
            val game = Constants.TestData.game
            fakeGameRepositoryImpl.insertGame(game)
            fakeCardRepositoryImpl.insertCard(card)
            fakeGameCardRepositoryImpl.insertGameCard(userCard.toGameCard())

            val initialState = CardStates.Hit(
                playerId = playerId,
                cardsToPick = expectedNumOfCardsToPick
            )
            handleCardStateUseCase.handleCardState(initialState)

            val resultState = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(resultState).isEqualTo(expectedState)
        }

    @Test
    fun `handle card state com from pick, update the listener to picked with same id`() =
        runBlockingTest {
            val playerId = Constants.Players.ANDROID_PLAYER.id

            val expectedNumOfCardsToPick = 1
            val expectedState = CardStates.Picked(
                playerId = playerId,
                cardsPicked = expectedNumOfCardsToPick,
                canPlay = true
            )

            val participants = listOf(
                Player(uniqueId = "1"),
                Player(uniqueId = "2")
            )
            fakeCardRepositoryImpl.insertCard(
                Constants.Cards.cards.random()
            )

            val gameId = createGameUseCase.createGame("gameName", participants)

            val initialState = CardStates.Pick(
                playerId = playerId,
                numOfCardsToPick = expectedNumOfCardsToPick
            )
            handleCardStateUseCase.handleCardState(initialState)

            val resultState = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(resultState).isEqualTo(expectedState)
        }

    @Test
    fun `handle card state user from pick, update the listener to picked with same id`() =
        runBlockingTest {
            val playerId = Constants.Players.GUEST_PLAYER.id

            val expectedNumOfCardsToPick = 1

            val expectedState = CardStates.Picked(
                playerId = playerId,
                cardsPicked = expectedNumOfCardsToPick,
                canPlay = true
            )

            fakeUserRepositoryImpl.insertDefaultPlayers()
            val participants = fakeUserRepositoryImpl.getPlayers().first()

            fakeCardRepositoryImpl.insertCard(
                Constants.Cards.cards.random()
            )

            val gameId = createGameUseCase.createGame("gameName", participants)
            fakeGameRepositoryImpl.updateRecentGameId(gameId)

            val initialState = CardStates.Pick(
                playerId = playerId,
                numOfCardsToPick = expectedNumOfCardsToPick
            )

            handleCardStateUseCase.handleCardState(initialState)

            val resultState = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(resultState).isEqualTo(expectedState)
        }

    @Test
    fun `handle card state com from picked with canPlay, update the listener to play with same id`() =
        runBlockingTest {
            val playerId = Constants.Players.ANDROID_PLAYER.id

            val expectedState = CardStates.Play(
                playerId = playerId,
                canPick = false,
                message = CardStatesDefaultMsg.PlayOnly
            )

            val cardsPicked = 1

            val initialState = CardStates.Picked(
                playerId = playerId,
                canPlay = true,
                cardsPicked = cardsPicked
            )
            handleCardStateUseCase.handleCardState(initialState)

            val resultState = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(resultState).isEqualTo(expectedState)
        }

    @Test
    fun `handle card state user from picked with canPlay, update the listener to play with same id`() =
        runBlockingTest {
            val playerId = Constants.Players.GUEST_PLAYER.id

            val expectedState = CardStates.Play(
                playerId = playerId,
                canPick = false,
                message = CardStatesDefaultMsg.PlayOnly
            )
            val cardsPicked = 1

            val initialState = CardStates.Picked(
                playerId = playerId,
                canPlay = true,
                cardsPicked = cardsPicked
            )
            handleCardStateUseCase.handleCardState(initialState)

            val resultState = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(resultState).isEqualTo(expectedState)
        }

    @Test
    fun `handle card state com from picked with canPlay-false, update the listener to user play`() =
        runBlockingTest {
            val playerId = Constants.Players.ANDROID_PLAYER.id

            val expectedState = CardStates.Play(
                playerId = Constants.Players.GUEST_PLAYER.id
            )
            val cardsPicked = 1

            val initialState = CardStates.Picked(
                playerId = playerId,
                canPlay = false,
                cardsPicked = cardsPicked
            )
            handleCardStateUseCase.handleCardState(initialState)

            val resultState = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(resultState).isEqualTo(expectedState)
        }

    @Test
    fun `handle card state user from picked with canPlay-false, update the listener to com play`() =
        runBlockingTest {
            val playerId = Constants.Players.GUEST_PLAYER.id

            val expectedState = CardStates.Play(
                playerId = Constants.Players.ANDROID_PLAYER.id
            )
            val cardsPicked = 1

            val initialState = CardStates.Picked(
                playerId = playerId,
                canPlay = false,
                cardsPicked = cardsPicked
            )
            handleCardStateUseCase.handleCardState(initialState)

            val resultState = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(resultState).isEqualTo(expectedState)

        }

    // com play to other states omitted
    @Test
    fun `handle card state com from play state with valid card not seven, jack, eight, joker or two, update the listener to played state`() =
        runBlockingTest {
            val playerId = Constants.Players.ANDROID_PLAYER.id

            val game = Constants.TestData.game
            val card = Constants.TestData.card1.copy(name = Constants.CardNames.THREE)
            val gameCard = GameCard(
                id = "game-card-id",
                gameId = game.id,
                cardId = card.id,
                playerId = playerId
            )
            fakeGameRepositoryImpl.insertGame(game)
            fakeGameRepositoryImpl.updateRecentGameId(game.id)
            fakeCardRepositoryImpl.insertCard(card)
            fakeGameCardRepositoryImpl.insertGameCard(gameCard)

            val initialState = CardStates.Play(playerId = playerId, canPick = true)

            handleCardStateUseCase.handleCardState(initialState)

            val resultState = fakeCrusadeCardsPreferenceManager.recentCardState.first()
            val expectedState = CardStates.Played(
                playerId = playerId
            )

            assertThat(resultState).isEqualTo(expectedState)
        }

    @Test
    fun `handle card state com from play state with cardName or cardType different, update the listener to pick state`() =
        runBlockingTest {

            val card = Card(
                name = Constants.CardNames.THREE,
                type = Constants.CardTypes.CLUBS
            )
            fakeUserRepositoryImpl.insertDefaultPlayers()
            val participants = fakeUserRepositoryImpl.getPlayers().first()
            val playerId = fakeUserRepositoryImpl.getAndroidUserId()

            fakeCardRepositoryImpl.insertCard(card)
            val gameId = createGameUseCase.createGame(gameName = "game", participants = participants)

            fakeGameRepositoryImpl.updateRecentGameId(gameId)
            fakeDefaultRepositoryImpl.changeCurrentCardType(Constants.CardTypes.DIAMOND)

            val initialState = CardStates.Play(playerId = playerId)

            handleCardStateUseCase.handleCardState(initialState)

            val resultState = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            val expectedState = CardStates.Pick(
                playerId = playerId
            )

            assertThat(resultState).isEqualTo(expectedState)
        }

    // other from play states coming soon...

    @Test
    fun `handle card state user from play , update playCardState to PromptUserPlayOrPick`() =
        runBlockingTest {
            val playerId = Constants.Players.GUEST_PLAYER.id
            val expected = GamePlayerActions.PromptPlayerPlayOrPick(
                playerId = playerId,
                canPick = true
            )

            val initialState = CardStates.Play(playerId = playerId, canPick = true, message = "")

            val actual = handleCardStateUseCase.handleCardState(initialState)

            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `handle card state com from played, update the listener to user play`() =
        runBlockingTest {
            val playerId = Constants.Players.ANDROID_PLAYER.id

            val expectedState = CardStates.Play(
                playerId = Constants.Players.GUEST_PLAYER.id
            )

            val initialState = CardStates.Played(
                playerId = playerId
            )
            handleCardStateUseCase.handleCardState(initialState)

            val resultState = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(resultState).isEqualTo(expectedState)
        }

    @Test
    fun `handle card state user from played, update the listener to com play`() =
        runBlockingTest {
            val playerId = Constants.Players.GUEST_PLAYER.id

            val expectedState = CardStates.Play(
                playerId = Constants.Players.ANDROID_PLAYER.id
            )

            val initialState = CardStates.Played(
                playerId = playerId
            )
            handleCardStateUseCase.handleCardState(initialState)

            val resultState = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(resultState).isEqualTo(expectedState)

        }

    @Test
    fun `handle card state user from played to making com have many chances to play, update the listener to com pla`() =
        runBlockingTest {

            // played user
            // com play
            // com no card available
            // com pick
            // com picked
            // user play

            val cards = Constants.Cards.cards
                .filter { it.type == Constants.CardTypes.CLUBS }
                .filter { it.name != Constants.CardNames.EIGHT }
            cards.forEachIndexed { index, card ->
                fakeCardRepositoryImpl.insertCard(card.copy(id = "$index"))
            }
            fakeDefaultRepositoryImpl.changeCurrentCardType(Constants.CardTypes.DIAMOND)
            fakeUserRepositoryImpl.insertDefaultPlayers()
            val participants = fakeUserRepositoryImpl.getPlayers().first()

            val gameId = createGameUseCase.createGame(gameName = "game", participants = participants)

            val userId = fakeUserRepositoryImpl.getUserId()
            val comUserId = fakeUserRepositoryImpl.getAndroidUserId()

            fakeGameCardRepositoryImpl.getGameCards(gameId = gameId).take(4)
                .forEach {
                    fakeGameCardRepositoryImpl.updateGameCard(
                        it.copy(playerId = comUserId)
                    )
                }

            val expectedState = CardStates.Play(
                playerId = comUserId
            )

            var currCardState : CardStates = CardStates.Played(
                playerId = userId
            )
            (1..6).forEach {
                handleCardStateUseCase.handleCardState(currCardState)
                currCardState = fakeCrusadeCardsPreferenceManager.recentCardState.first()
            }

            assertThat(currCardState).isEqualTo(expectedState)

        }

    @Test
    fun `handle card state com from change-type, update the listener to com changed-type`() =
        runBlockingTest {


            fakeUserRepositoryImpl.insertDefaultPlayers()
            val playerId = fakeUserRepositoryImpl.getAndroidUserId()

            Constants.Cards.cards.forEachIndexed { index, card ->
                fakeCardRepositoryImpl.insertCard(card.copy(id = index.toString()))
            }
            val gameId = createGameUseCase.createGame(
                gameName = "game",
                participants = fakeUserRepositoryImpl.getPlayers().first())

            val cards = fakeCardRepositoryImpl.getCards().take(5)
            cards.forEachIndexed { index, card ->
                fakeGameCardRepositoryImpl.insertGameCard(
                    GameCard(
                        id = index.toString(),
                        gameId = gameId,
                        cardId = card.id,
                        playerId = playerId
                    )
                )
            }


            val expectedState = CardStates.ChangedType(
                playerId = playerId
            )

            val initialState = CardStates.ChangeType(
                playerId = playerId
            )

            handleCardStateUseCase.handleCardState(initialState)

            val resultState = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(resultState).isEqualTo(expectedState)
        }

    @Test
    fun `handle card state user from change-type, update playCardState to PrompUserChangeCardType with same id`() =
        runBlockingTest {
            val playerId = Constants.Players.GUEST_PLAYER.id

            val expected = GamePlayerActions.PromptPlayerChangeCardType(
                playerId = playerId
            )

            val initialState = CardStates.ChangeType(
                playerId = playerId
            )

            handleCardStateUseCase.handleCardState(initialState)

            val actual = handleCardStateUseCase.handleCardState(initialState)

            assertThat(actual).isEqualTo(expected)

        }

    @Test
    fun `handle card state with start state, update the listener to play state`() =
        runBlockingTest {
            val playerId = Constants.Players.ANDROID_PLAYER.id

            val cards = Constants.Cards.cards
            cards.forEach { card ->
                fakeCardRepositoryImpl.insertCard(card)
            }

            val initialState = CardStates.Start
            handleCardStateUseCase.handleCardState(initialState)

            val resultState = fakeCrusadeCardsPreferenceManager.recentCardState.first()
            val expectedState = CardStates.Play(
                playerId = playerId,
                canPick = false
            )
            assertThat(resultState).isEqualTo(expectedState)
        }

    // saveCardStateToPreferences

    @Test
    fun `saveCardStateToPreferences with play, expect play with same data`() =
        runBlockingTest {
            val playerId = Constants.Players.ANDROID_PLAYER.id

            val expected = CardStates.Play(
                playerId = playerId,
                canPick = true
            )
            handleCardStateUseCase.saveCardStateToPreferences(expected)

            val actual = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `saveCardStateToPreferences with played, expect played with same data`() =
        runBlockingTest {
            val playerId = Constants.Players.ANDROID_PLAYER.id

            val expected = CardStates.Played(
                playerId = playerId,
                message = "message"
            )
            handleCardStateUseCase.saveCardStateToPreferences(expected)

            val actual = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `saveCardStateToPreferences with pick, expect pick with same data`() =
        runBlockingTest {
            val playerId = Constants.Players.ANDROID_PLAYER.id

            val expected = CardStates.Pick(
                playerId = playerId,
                numOfCardsToPick = 5,
                canPlay = true
            )
            handleCardStateUseCase.saveCardStateToPreferences(expected)

            val actual = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `saveCardStateToPreferences with picked, expect picked with same data`() =
        runBlockingTest {
            val playerId = Constants.Players.ANDROID_PLAYER.id

            val expected = CardStates.Picked(
                playerId = playerId,
                canPlay = true,
                cardsPicked = 5,
                message = "message"
            )
            handleCardStateUseCase.saveCardStateToPreferences(expected)

            val actual = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `saveCardStateToPreferences with hit, expect hit with same data`() =
        runBlockingTest {
            val playerId = Constants.Players.ANDROID_PLAYER.id

            val expected = CardStates.Hit(
                playerId = playerId,
                cardsToPick = 5,
                message = "message"
            )
            handleCardStateUseCase.saveCardStateToPreferences(expected)

            val actual = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `saveCardStateToPreferences with changeType, expect changeType with same data`() =
        runBlockingTest {
            val playerId = Constants.Players.ANDROID_PLAYER.id

            val expected = CardStates.ChangeType(
                playerId = playerId
            )
            handleCardStateUseCase.saveCardStateToPreferences(expected)

            val actual = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `saveCardStateToPreferences with changedType, expect changedType with same data`() =
        runBlockingTest {
            val playerId = Constants.Players.ANDROID_PLAYER.id

            val expected = CardStates.ChangedType(
                playerId = playerId,
                message = "message"
            )
            handleCardStateUseCase.saveCardStateToPreferences(expected)

            val actual = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `saveCardStateToPreferences with defineCardAction, expect defineCardAction with same data`() =
        runBlockingTest {
            val playerId = Constants.Players.ANDROID_PLAYER.id

            val expected = CardStates.DefineCardAction(
                playerId = playerId,
                gameCardId = "game-card-id"
            )
            handleCardStateUseCase.saveCardStateToPreferences(expected)

            val actual = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `saveCardStateToPreferences with chance, expect chance with same data`() =
        runBlockingTest {
            val playerId = Constants.Players.ANDROID_PLAYER.id

            val expected = CardStates.Chance(
                playerId = playerId
            )
            handleCardStateUseCase.saveCardStateToPreferences(expected)

            val actual = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `saveCardStateToPreferences with noAvailableCard, expect noAvailableCard with same data`() =
        runBlockingTest {
            val playerId = Constants.Players.ANDROID_PLAYER.id

            val expected = CardStates.NoAvailableCard(
                playerId = playerId
            )
            handleCardStateUseCase.saveCardStateToPreferences(expected)

            val actual = fakeCrusadeCardsPreferenceManager.recentCardState.first()

            assertThat(actual).isEqualTo(expected)
        }

}