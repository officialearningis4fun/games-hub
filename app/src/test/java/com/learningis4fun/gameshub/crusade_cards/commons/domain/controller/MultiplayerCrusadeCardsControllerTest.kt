package com.learningis4fun.gameshub.crusade_cards.commons.domain.controller

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.learningis4fun.gameshub.FakeRepository
import com.learningis4fun.gameshub.MainCoroutineRule
import com.learningis4fun.gameshub.crusade_cards.common.domain.repository.*
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.*
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameCardWithCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.mappers.toGameCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.GameCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.UserCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case.CreateGameUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@ExperimentalStdlibApi
class MultiplayerCrusadeCardsControllerTest {

    private lateinit var cardsController: MultiplayerCrusadeCardsController
    private lateinit var repository: FakeRepository
    private lateinit var fakeDefaultRepositoryImpl: FakeDefaultRepositoryImpl
    private lateinit var fakeUserRepositoryImpl: FakeUserRepositoryImpl
    private lateinit var fakeGameCardRepositoryImpl: FakeGameCardRepositoryImpl
    private lateinit var fakeCardRepositoryImpl: FakeCardRepositoryImpl
    private lateinit var fakeGameRepositoryImpl: FakeGameRepositoryImpl
    private lateinit var crusadeComputerCardsController: CrusadeComputerCardsController
    private lateinit var createGameUseCase: CreateGameUseCase

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        fakeCardRepositoryImpl = FakeCardRepositoryImpl()
        fakeUserRepositoryImpl = FakeUserRepositoryImpl()
        fakeDefaultRepositoryImpl = FakeDefaultRepositoryImpl()

        fakeGameRepositoryImpl = FakeGameRepositoryImpl(
            userRepositoryImpl = fakeUserRepositoryImpl,
            defaultRepositoryImpl = fakeDefaultRepositoryImpl
        )

        fakeGameCardRepositoryImpl = FakeGameCardRepositoryImpl(
            cardsRepositoryImpl = fakeCardRepositoryImpl
        )

        repository = FakeRepository(
            gameCardRepositoryImpl = fakeGameCardRepositoryImpl,
            cardRepositoryImpl = fakeCardRepositoryImpl
        )

        crusadeComputerCardsController = CrusadeComputerCardsController(
            gameRepository = fakeGameRepositoryImpl,
            cardRepository = fakeCardRepositoryImpl,
            gameCardRepository = fakeGameCardRepositoryImpl
        )
        cardsController = MultiplayerCrusadeCardsController(
            repository = fakeDefaultRepositoryImpl,
            gameRepository = fakeGameRepositoryImpl,
            cardRepository = fakeCardRepositoryImpl,
            gameCardRepository = fakeGameCardRepositoryImpl,
            userRepository = fakeUserRepositoryImpl
        )

        createGameUseCase = CreateGameUseCase(
            repository = fakeDefaultRepositoryImpl,
            gameRepository = fakeGameRepositoryImpl,
            cardRepository = fakeCardRepositoryImpl,
            gameCardRepository = fakeGameCardRepositoryImpl
        )
    }

    @Test
    fun `request play card with empty playerId, gameId and empty userCard, returns error`() =
        runBlockingTest {

            val cardState = cardsController.requestPlay(
                playerId = "",
                gameId = "",
                currentCardType = Constants.CardTypes.HEART,
                currentCardName = Constants.CardNames.TWO,
                userCard = UserCard(),
                canPick = true
            )
            assertThat(cardState).isInstanceOf(CardStates.Error::class.java)
        }

    @Test
    fun `request play card with not available playerId, gameId, returns error`() = runBlockingTest {
        val userCard = Constants.TestData.userCard.copy(name = Constants.CardNames.THREE)
        val cardState = cardsController.requestPlay(
            playerId = Constants.TestData.absentPlayerId,
            gameId = Constants.TestData.absentGameId,
            currentCardType = Constants.CardTypes.HEART,
            currentCardName = Constants.CardNames.TWO,
            userCard = userCard,
            canPick = true
        )
        assertThat(cardState).isInstanceOf(CardStates.Error::class.java)
    }

    @Test
    fun `request play card with valid details, returns not error`() = runBlockingTest {

        val userCard = Constants.TestData.userCard.copy(name = Constants.CardNames.THREE)
        val card = Card(
            id = userCard.cardId,
            name = userCard.name,
            type = userCard.type
        )
        fakeCardRepositoryImpl.insertCard(card)
        fakeGameCardRepositoryImpl.insertGameCard(
            gameCard = userCard.toGameCard().copy(isPlayed = false)
        )
        val cardState = cardsController.requestPlay(
            playerId = Constants.TestData.playerId,
            gameId = Constants.TestData.gameId,
            currentCardType = Constants.CardTypes.HEART,
            currentCardName = Constants.CardNames.TWO,
            userCard = userCard,
            canPick = true
        )
        assertThat(cardState).isNotInstanceOf(CardStates.Error::class.java)
    }

    @Test
    fun `should card be played with cardName as eight, returns true`() {
        val cardName = Constants.CardNames.EIGHT
        val currentName = Constants.CardNames.THREE

        val result = cardsController.shouldCardBePlayed(
            cardName = cardName,
            cardType = Constants.CardTypes.CLUBS,
            currentCardName = currentName,
            currentCardType = Constants.CardTypes.HEART
        )

        assertThat(result).isTrue()
    }

    @Test
    fun `should card be played with cardName as joker, returns true`() {
        val cardName = Constants.CardNames.JOKER

        val result = cardsController.shouldCardBePlayed(
            cardName = cardName,
            cardType = Constants.CardTypes.CLUBS,
            currentCardName = Constants.CardNames.FIVE,
            currentCardType = Constants.CardTypes.HEART
        )

        assertThat(result).isTrue()
    }

    @Test
    fun `should card be played with same cardType as currentCardType, returns true`() {
        val cardType = Constants.CardTypes.CLUBS
        val currentType = Constants.CardTypes.CLUBS

        val result = cardsController.shouldCardBePlayed(
            cardName = Constants.CardNames.FIVE,
            cardType = cardType,
            currentCardName = Constants.CardNames.SEVEN,
            currentCardType = currentType
        )

        assertThat(result).isTrue()
    }

    @Test
    fun `should card be played with same cardName currentCardName, returns true`() {
        val cardName = Constants.CardNames.THREE
        val currentName = Constants.CardNames.THREE

        val result = cardsController.shouldCardBePlayed(
            cardName = cardName,
            cardType = Constants.CardTypes.CLUBS,
            currentCardName = currentName,
            currentCardType = Constants.CardTypes.HEART
        )

        assertThat(result).isTrue()
    }

    @Test
    fun `should card be played with card not eight or joker & different cardType and currentCardTpe, returns false`() {
        val cardType = Constants.CardTypes.CLUBS
        val currentCardType = Constants.CardTypes.HEART

        val result = cardsController.shouldCardBePlayed(
            cardName = Constants.CardNames.FIVE,
            cardType = cardType,
            currentCardName = Constants.CardNames.KING,
            currentCardType = currentCardType
        )

        assertThat(result).isFalse()
    }

    // play card tests
    @Test
    fun `play card with eight, return change card`() = runBlockingTest {
        val userCard = Constants.TestData.userCard.copy(name = Constants.CardNames.EIGHT)
        val card = Card(
            id = userCard.cardId,
            name = userCard.name,
            type = userCard.type
        )
        fakeCardRepositoryImpl.insertCard(card)
        fakeGameCardRepositoryImpl.insertGameCard(userCard.toGameCard().copy(isPlayed = false))

        val result = cardsController.playCard(
            userCard = userCard
        )

        assertThat(result).isInstanceOf(CardStates.ChangeType::class.java)
    }

    @Test
    fun `play card with two, return hit`() = runBlockingTest {
        val card = Card(
            id = "id",
            name = Constants.CardNames.TWO,
            type = Constants.CardTypes.CLUBS
        )
        fakeUserRepositoryImpl.insertDefaultPlayers()
        fakeCardRepositoryImpl.insertCard(card)
        val userId = fakeUserRepositoryImpl.getUserId()

        val participants = fakeUserRepositoryImpl.getPlayers().first()
        val gameId = createGameUseCase.createGame(gameName = "game", participants = participants)

        val gameCard = fakeGameCardRepositoryImpl
            .getGameCards(gameId = gameId)
            .first()
            .copy(playerId = userId)

        fakeGameCardRepositoryImpl.updateGameCard(gameCard)

        val userCard = fakeGameCardRepositoryImpl.getUnPlayedCardsForPlayer(
            gameId = gameId,
            playerId = userId)
            .first()
            .first()

        val expect = CardStates.Hit(
            playerId = Constants.Players.ANDROID_PLAYER.id,
            cardsToPick = 2
        )

        val result = cardsController.playCard(
            userCard = userCard
        )

        assertThat(result).isEqualTo(expect)
    }

    @Test
    fun `play card with seven or jack, return chance`() = runBlockingTest {
        val card = Card(
            id = "card-id",
            name = Constants.CardNames.SEVEN,
            type = Constants.CardTypes.CLUBS
        )
        fakeUserRepositoryImpl.insertDefaultPlayers()
        fakeCardRepositoryImpl.insertCard(card)

        val comUserId = fakeUserRepositoryImpl.getAndroidUserId()

        val participants = fakeUserRepositoryImpl.getPlayers().first()

        val gameId = createGameUseCase.createGame(gameName = "game", participants = participants)
        fakeGameRepositoryImpl.updateRecentGameId(gameId)

        val gameCard = fakeGameCardRepositoryImpl
            .getGameCards(gameId = gameId)
            .first()
            .copy(playerId = comUserId)

        fakeGameCardRepositoryImpl.updateGameCard(gameCard = gameCard)

        val userCard = fakeGameCardRepositoryImpl.getUnPlayedCardsForPlayer(
            gameId = gameId,
            playerId = comUserId
        )
            .first()
            .first()

        val result = cardsController.playCard(
            userCard = userCard
        )
        val expect = CardStates.Chance(playerId = comUserId)

        assertThat(result).isEqualTo(expect)
    }

    // pick cards from library tests
    @Test
    fun `pick cards with empty player id or gameId or cards to pick is zero, return error`() =
        runBlockingTest {
            val userCard = Constants.TestData.userCard.copy(name = Constants.CardNames.SEVEN)
            fakeGameCardRepositoryImpl.insertGameCard(
                userCard.toGameCard().copy(isPlayed = false, playerId = "")
            )
            fakeGameCardRepositoryImpl.insertGameCard(
                userCard.toGameCard()
                    .copy(id = "gameCardId2", cardId = "cardId2", isPlayed = false, playerId = "")
            )

            val result = cardsController.pickCardsFromLibrary(
                playerId = "",
                gameId = "",
                numOfCardsToPick = 2
            )

            assertThat(result).isInstanceOf(CardStates.Error::class.java)
        }

    @Test
    fun `pick cards with valid input, return picked`() = runBlockingTest {
        val userCard = Constants.TestData.userCard.copy(name = Constants.CardNames.SEVEN)
        val cardsToPick = 2

        for (i in 1..4) {
            fakeGameCardRepositoryImpl.insertGameCard(
                userCard.toGameCard().copy(
                    id = "${userCard.gameCardId}$i",
                    cardId = "${userCard.cardId}$i",
                    isPlayed = false,
                    playerId = ""
                )
            )
        }

        val result = cardsController.pickCardsFromLibrary(
            playerId = Constants.TestData.playerId,
            gameId = Constants.TestData.gameId,
            numOfCardsToPick = cardsToPick
        )

        assertThat(result).isInstanceOf(CardStates.Picked::class.java)
    }

    @Test
    fun `pick cards with valid input but no cards in dustbin, return picked`() = runBlockingTest {
        val userCard = Constants.TestData.userCard.copy(name = Constants.CardNames.SEVEN)
        val cardsToPick = 2

        for (i in 1..4) {
            fakeGameCardRepositoryImpl.insertGameCard(
                userCard.toGameCard().copy(
                    id = "${userCard.gameCardId}$i",
                    cardId = "${userCard.cardId}$i",
                    isPlayed = false,
                    playerId = ""
                )
            )
        }

        val result = cardsController.pickCardsFromLibrary(
            playerId = Constants.TestData.playerId,
            gameId = Constants.TestData.gameId,
            numOfCardsToPick = cardsToPick
        )

        assertThat(result).isInstanceOf(CardStates.Picked::class.java)
    }

    @Test
    fun `pick cards with num of cards specified, return picked with a specified number cards`() =
        runBlockingTest {
            val userCard = Constants.TestData.userCard.copy(name = Constants.CardNames.SEVEN)
            val numOfCardsList = (3..9)
            val numOfCardsToPick = numOfCardsList.random() - 2

            for (i in numOfCardsList) {
                fakeGameCardRepositoryImpl.insertGameCard(
                    userCard.toGameCard().copy(
                        id = "${userCard.gameCardId}$i",
                        cardId = "${userCard.cardId}$i",
                        isPlayed = false,
                        playerId = ""
                    )
                )
            }


            val result = cardsController.pickCardsFromLibrary(
                playerId = Constants.TestData.playerId,
                gameId = Constants.TestData.gameId,
                numOfCardsToPick = numOfCardsToPick
            )

            if (result is CardStates.Picked) {
                assertThat(result.cardsPicked).isEqualTo(numOfCardsToPick)
            }
        }

    // test allowChangeType

    @Test
    fun `allow change card type with empty playerId and type, return error`() = runBlockingTest {

        val result = cardsController.allowChangeType(
            playerId = "",
            type = ""
        )
        assertThat(result).isInstanceOf(Response.Error::class.java)
    }

    @Test
    fun `allow change card type with valid inputs, return true`() = runBlockingTest {
        val cardType = Constants.CardTypes.CLUBS
        val playerId = Constants.TestData.playerId

        val result = cardsController.allowChangeType(
            playerId = playerId,
            type = cardType
        )
        assertThat(result).isInstanceOf(Response.Success::class.java)
    }

    // test allow hit

    @Test
    fun `allow hit with empty playerId & cardName, return error`() = runBlockingTest {
        val result = cardsController.allowHit(
            playerId = "",
            prevCardsToPick = 9,
            cardName = ""
        )
        assertThat(result).isInstanceOf(CardStates.Error::class.java)
    }

    @Test
    fun `allow hit with wrong card to hit playerId & cardName, return error`() = runBlockingTest {
        val result = cardsController.allowHit(
            playerId = Constants.TestData.playerId,
            prevCardsToPick = 4,
            cardName = Constants.CardNames.THREE
        )
        assertThat(result).isInstanceOf(CardStates.Error::class.java)
    }

    @Test
    fun `allow hit with valid playerId & cardName, return hit`() = runBlockingTest {

        val card = Card(
            id = "id",
            name = Constants.CardNames.TWO,
            type = Constants.CardTypes.CLUBS
        )
        fakeUserRepositoryImpl.insertDefaultPlayers()
        fakeCardRepositoryImpl.insertCard(card)

        val userId = fakeUserRepositoryImpl.getUserId()
        val comUserId = fakeUserRepositoryImpl.getAndroidUserId()

        val participants = fakeUserRepositoryImpl.getPlayers().first()
        val gameId = createGameUseCase.createGame(gameName = "game", participants = participants)

        val gameCard = fakeGameCardRepositoryImpl
            .getGameCards(gameId = gameId)
            .first()
            .copy(playerId = userId)

        fakeGameCardRepositoryImpl.updateGameCard(gameCard)

        val userCard = fakeGameCardRepositoryImpl.getUnPlayedCardsForPlayer(
            gameId = gameId,
            playerId = userId)
            .first()
            .first()

        val cardName = userCard.name

        val expect = CardStates.Hit(
            playerId = userId,
            cardsToPick = 2
        )
        val result = cardsController.allowHit(
            playerId = comUserId,
            prevCardsToPick = 0,
            cardName = cardName
        )

        assertThat(result).isEqualTo(expect)
    }

    @Test
    fun `allow logged in user hit with two players, the cards to be picked are correct`() =
        runBlockingTest {
            val cardsToPick = 0

            val card = Card(
                id = "id",
                name = Constants.CardNames.TWO,
                type = Constants.CardTypes.CLUBS
            )
            fakeUserRepositoryImpl.insertDefaultPlayers()
            fakeCardRepositoryImpl.insertCard(card)

            val userId = fakeUserRepositoryImpl.getUserId()
            val comUserId = fakeUserRepositoryImpl.getAndroidUserId()

            val participants = fakeUserRepositoryImpl.getPlayers().first()
            val gameId = createGameUseCase.createGame(gameName = "game", participants = participants)

            val gameCard = fakeGameCardRepositoryImpl
                .getGameCards(gameId = gameId)
                .first()
                .copy(playerId = userId)

            fakeGameCardRepositoryImpl.updateGameCard(gameCard)

            val userCard = fakeGameCardRepositoryImpl.getUnPlayedCardsForPlayer(
                gameId = gameId,
                playerId = userId)
                .first()
                .first()

            val result = cardsController.allowHit(
                playerId = comUserId,
                prevCardsToPick = cardsToPick,
                cardName = userCard.name
            )
            val expect = CardStates.Hit(
                playerId = userId,
                cardsToPick = 2
            )
            assertThat(result).isEqualTo(expect)
        }

    @Test
    fun `allow logged in user hit with more two players, the cards to be picked are correct`() =
        runBlockingTest {
            val cardsToPick = 0

            val card = Card(
                id = "id",
                name = Constants.CardNames.TWO,
                type = Constants.CardTypes.CLUBS
            )
            fakeUserRepositoryImpl.insertDefaultPlayers()
            fakeUserRepositoryImpl.savePlayer(Player(uniqueId = "id"))

            fakeCardRepositoryImpl.insertCard(card)
            val userId = fakeUserRepositoryImpl.getUserId()
            val comUserId = fakeUserRepositoryImpl.getAndroidUserId()

            val participants = fakeUserRepositoryImpl.getPlayers().first()
            val gameId = createGameUseCase.createGame(gameName = "game", participants = participants)

            val gameCard = fakeGameCardRepositoryImpl
                .getGameCards(gameId = gameId)
                .first()
                .copy(playerId = userId)

            fakeGameCardRepositoryImpl.updateGameCard(gameCard)

            val userCard = fakeGameCardRepositoryImpl.getUnPlayedCardsForPlayer(
                gameId = gameId,
                playerId = userId)
                .first()
                .first()

            val cardName = userCard.name

            val result = cardsController.allowHit(
                playerId = comUserId,
                prevCardsToPick = cardsToPick,
                cardName = cardName
            )
            val expect = CardStates.Hit(
                playerId = userId,
                cardsToPick = 2
            )
            assertThat(result).isEqualTo(expect)
        }

    @Test
    fun `allow com hit with two players, the cards to be picked are correct`() = runBlockingTest {

        val cardsToPick = 0

        val card = Card(
            id = "id",
            name = Constants.CardNames.TWO,
            type = Constants.CardTypes.CLUBS
        )
        fakeUserRepositoryImpl.insertDefaultPlayers()
        fakeCardRepositoryImpl.insertCard(card)
        val userId = fakeUserRepositoryImpl.getUserId()

        val participants = fakeUserRepositoryImpl.getPlayers().first()
        val gameId = createGameUseCase.createGame(gameName = "game", participants = participants)

        val gameCard = fakeGameCardRepositoryImpl
            .getGameCards(gameId = gameId)
            .first()
            .copy(playerId = userId)

        fakeGameCardRepositoryImpl.updateGameCard(gameCard)

        val userCard = fakeGameCardRepositoryImpl.getUnPlayedCardsForPlayer(
            gameId = gameId,
            playerId = userId)
            .first()
            .first()

        val cardName = userCard.name

        val comUserId = fakeUserRepositoryImpl.getAndroidUserId()

        val result = cardsController.allowHit(
            playerId = comUserId,
            prevCardsToPick = cardsToPick,
            cardName = cardName
        )
        val expect = CardStates.Hit(
            playerId = userId,
            cardsToPick = 2
        )
        assertThat(result).isEqualTo(expect)
    }

    @Test
    fun `allow com hit with more than two players, the cards to be picked are correct`() =
        runBlockingTest {
            val cardsToPick = 0

            val card = Card(
                id = "id",
                name = Constants.CardNames.TWO,
                type = Constants.CardTypes.CLUBS)

            fakeUserRepositoryImpl.insertDefaultPlayers()
            fakeUserRepositoryImpl.savePlayer(Player(uniqueId = "id"))

            fakeCardRepositoryImpl.insertCard(card)

            val userId = fakeUserRepositoryImpl.getUserId()

            val participants = fakeUserRepositoryImpl.getPlayers().first()
            val gameId = createGameUseCase.createGame(gameName = "game", participants = participants)

            val gameCard = fakeGameCardRepositoryImpl
                .getGameCards(gameId = gameId)
                .first()
                .copy(playerId = userId)

            fakeGameCardRepositoryImpl.updateGameCard(gameCard)

            val userCard = fakeGameCardRepositoryImpl.getUnPlayedCardsForPlayer(
                gameId = gameId,
                playerId = userId)
                .first()
                .first()

            val comUserId = fakeUserRepositoryImpl.getAndroidUserId()

            val cardName = userCard.name

            val result = cardsController.allowHit(
                playerId = comUserId,
                prevCardsToPick = cardsToPick,
                cardName = cardName
            )
            val expect = CardStates.Hit(
                playerId = userId,
                cardsToPick = 2
            )

            assertThat(result).isEqualTo(expect)
        }

    @Test
    fun `request hit back with a null card, returns hit`() = runBlockingTest {
        val numOfCardsToPick = 2
        val numOfCardsToMakeAvailable = 4
        val game = Constants.TestData.game
        val playerId = Constants.TestData.playerId

        fakeGameRepositoryImpl.insertGame(game)
        //add enough cards to pick
        val cards = Constants.Cards.cards.take(numOfCardsToMakeAvailable)
        cards.forEach {
            fakeCardRepositoryImpl.insertCard(it)
        }

        val gameCards = cards
            .mapIndexed { index, card ->
                GameCard(
                    id = "$index",
                    gameId = game.id,
                    cardId = card.id,
                    playerId = playerId
                )
            }
        gameCards.forEach {
            fakeGameCardRepositoryImpl.insertGameCard(it)
        }

        val result = cardsController.requestHitBackOrPick(
            userCard = UserCard(),
            playerId = playerId,
            cardsToPick = numOfCardsToPick)
        assertThat(result).isInstanceOf(CardStates.Hit::class.java)
    }

    @Test
    fun `request hit back with a card not 2 or joker, returns hitback error`() = runBlockingTest {
        val numOfCardsToPick = 2

        val game = Constants.TestData.game
        val playerId = Constants.TestData.playerId

        fakeGameRepositoryImpl.insertGame(game)
        //add enough cards to pick
        val card = Constants.TestData.card1

        val gameCard = GameCard(
            id = "1",
            gameId = game.id,
            cardId = card.id,
            playerId = playerId
        )
        fakeGameCardRepositoryImpl.insertGameCard(gameCard)

        val userCard = GameCardWithCard(
            gameCard = gameCard,
            card = card
        ).toUserCard()

        val result = cardsController.requestHitBackOrPick(userCard, playerId, numOfCardsToPick)
        val expect = CardStates.Hit(
            playerId = userCard.playerId,
            cardsToPick = numOfCardsToPick,
            message = CardStatesDefaultMsg.HitError
        )

        assertThat(result).isEqualTo(expect)
    }

    @Test
    fun `request hit back with a card as 2, returns hit`() = runBlockingTest {
        val numOfCardsToPick = 2

        val card = Card(
            id = "id",
            name = Constants.CardNames.TWO,
            type = Constants.CardTypes.CLUBS
        )
        fakeUserRepositoryImpl.insertDefaultPlayers()
        fakeCardRepositoryImpl.insertCard(card)
        val userId = fakeUserRepositoryImpl.getUserId()

        val participants = fakeUserRepositoryImpl.getPlayers().first()
        val gameId = createGameUseCase.createGame(gameName = "game", participants = participants)

        val gameCard = fakeGameCardRepositoryImpl
            .getGameCards(gameId = gameId)
            .first()
            .copy(playerId = userId)

        fakeGameCardRepositoryImpl.updateGameCard(gameCard)

        val userCard = fakeGameCardRepositoryImpl.getUnPlayedCardsForPlayer(
            gameId = gameId,
            playerId = userId)
            .first()
            .first()

        val result = cardsController.requestHitBackOrPick(
            userCard = userCard,
            playerId = userId,
            cardsToPick = numOfCardsToPick)

        val expect = CardStates.Hit(
            playerId = Constants.Players.ANDROID_PLAYER.id,
            cardsToPick = 4
        )
        assertThat(result).isEqualTo(expect)
    }

    @Test
    fun `find previous player to play after jack for two players, return same id`() =
        runBlockingTest {

            fakeCardRepositoryImpl.insertCard(
                Constants.Cards.cards.random().copy(id = "id")
            )
            fakeUserRepositoryImpl.insertDefaultPlayers()
            val userId = fakeUserRepositoryImpl.getUserId()

            val participants = fakeUserRepositoryImpl.getPlayers().first()
            val gameId =
                createGameUseCase.createGame(gameName = "game - name", participants = participants)
            fakeGameRepositoryImpl.updateRecentGameId(gameId)

            val actual = cardsController.findPreviousPlayerToPlayAboveJackAndChangePlayingDirection(
                currPlayerId = userId,
                gameId = gameId
            )
            assertThat(actual).isEqualTo(userId)
        }

    @Test
    fun `find previous player to play after jack for more than two players, return diff id`() =
        runBlockingTest {
            fakeUserRepositoryImpl.insertDefaultPlayers()
            fakeUserRepositoryImpl.savePlayer(Player(uniqueId = "id"))
            val userId = fakeUserRepositoryImpl.getUserId()

            val participants = fakeUserRepositoryImpl.getPlayers().first()

            fakeCardRepositoryImpl.insertCard(
                Constants.Cards.cards.random()
            )

            val gameId = createGameUseCase.createGame("game", participants)

            val actual = cardsController.findPreviousPlayerToPlayAboveJackAndChangePlayingDirection(
                currPlayerId = userId,
                gameId = gameId
            )
            assertThat(actual).isNotEqualTo(userId)
        }

    @Test
    fun `find next player to play after seven for two players, return same id`() =
        runBlockingTest {
            fakeUserRepositoryImpl.insertDefaultPlayers()
            val userId = fakeUserRepositoryImpl.getUserId()

            val participants = fakeUserRepositoryImpl.getPlayers().first()

            fakeCardRepositoryImpl.insertCard(
                Constants.Cards.cards.random()
            )

            val gameId = createGameUseCase.createGame("game", participants)

            val actual = cardsController.findNextPlayerToPlayAboveSeven(
                currPlayerId = userId,
                gameId = gameId
            )

            assertThat(actual).isEqualTo(userId)
        }

    @Test
    fun `find next player to play after seven for more than two players, return diff id`() =
        runBlockingTest {
            fakeUserRepositoryImpl.insertDefaultPlayers()
            fakeUserRepositoryImpl.savePlayer(Player(uniqueId = "id"))
            val userId = fakeUserRepositoryImpl.getUserId()

            val participants = fakeUserRepositoryImpl.getPlayers().first()

            fakeCardRepositoryImpl.insertCard(
                Constants.Cards.cards.random()
            )

            val gameId = createGameUseCase.createGame("game", participants)

            val actual = cardsController.findNextPlayerToPlayAboveSeven(
                currPlayerId = userId,
                gameId = gameId
            )

            assertThat(actual).isNotEqualTo(userId)
        }

    @Test
    fun `find next player id to be hit by a two for more than two players, return diff id`() =
        runBlockingTest {
            fakeUserRepositoryImpl.savePlayer(Player(uniqueId = "id"))
            fakeUserRepositoryImpl.insertDefaultPlayers()

            val comUserId = fakeUserRepositoryImpl.getAndroidUserId()

            fakeCardRepositoryImpl.insertCard(
                Constants.Cards.cards.random().copy(id = "id")
            )

            val participants = fakeUserRepositoryImpl.getPlayers().first()

            val gameId = createGameUseCase.createGame(
                gameName = "game",
                participants = participants
            )
            val actual = cardsController.getIdOfTheNextPlayer(
                currentPlayer = comUserId,
                gameId = gameId
            )
            assertThat(actual).isNotEqualTo(comUserId)
        }

    @Test
    fun `find next player id to be hit by a two for two players, return diff id`() =
        runBlockingTest {
            fakeUserRepositoryImpl.insertDefaultPlayers()

            val comUserId = fakeUserRepositoryImpl.getAndroidUserId()

            fakeCardRepositoryImpl.insertCard(
                Constants.Cards.cards.random().copy(id = "id")
            )

            val participants = fakeUserRepositoryImpl.getPlayers().first()

            val gameId = createGameUseCase.createGame(
                gameName = "game",
                participants = participants
            )

            val actual = cardsController.getIdOfTheNextPlayer(
                currentPlayer = comUserId,
                gameId = gameId
            )

            assertThat(actual).isNotEqualTo(comUserId)
        }
}