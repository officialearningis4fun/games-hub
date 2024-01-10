package com.learningis4fun.gameshub

import com.learningis4fun.gameshub.feature_crusade_cards.common.domain.repository.FakeCardRepositoryImpl
import com.learningis4fun.gameshub.feature_crusade_cards.common.domain.repository.FakeGameCardRepositoryImpl
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.GameUserCrossRef
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.User
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.*
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.data.data_source.relations.GameCardWithCard
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.data.data_source.relations.GamesOfPlayer
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.GameCard
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.UserCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeRepository(
    private val gameCardRepositoryImpl: FakeGameCardRepositoryImpl,
    private val cardRepositoryImpl: FakeCardRepositoryImpl
) : DefaultCardRepository {

    private val _gameCards: MutableList<GameCard> = ArrayList()
    private val _cards: MutableList<Card> = ArrayList()
    private val _players: MutableList<User> = ArrayList()

    private val _gamesUserCrossRef: MutableList<GameUserCrossRef> = ArrayList()

    private var _currentCardType = ""
    private var _currentCardState: CardStates = CardStates.Start
    private var _currentGame = Game()
    private var _currentGameId = ""

    /**
     * For creating a game with a card and gameCard
     */
    fun createGameAndCard(
        playerId: String = "",
        cardName: String = Constants.CardNames.THREE,
        cardType: String = Constants.CardTypes.CLUBS
    ): UserCard {
        val game = Game(id = "gameId", name = "game")
        val card = Card(id = "cardId", name = cardName, type = cardType)
        val gameCard = GameCard(
            id = "gameCardId",
            gameId = game.id,
            cardId = card.id,
            playerId = playerId,
            isPlayed = false,
            playTime = 0L
        )
        val userCard = UserCard(
            gameCardId = gameCard.id,
            playerId = playerId,
            cardId = card.id,
            name = card.name,
            gameId = game.id,
            type = card.type,
            isPlayed = gameCard.isPlayed,
            image = card.image,
            playTime = gameCard.playTime
        )

        _currentGame = game
        _currentGameId = game.id
        _cards.add(card)
        _gameCards.add(gameCard)

        return userCard
    }

    /**
     * For creating a game with a cards per specified numOfCards and gameCards
     */
    fun createGameAndCards(
        optionalCards: List<Card>
    ) {
        val game = Game(id = "gameId", name = "game")

        val playersIds = listOf(
            Constants.Players.ANDROID_PLAYER.id,
            Constants.Players.GUEST_PLAYER.id,
        )

        _gamesUserCrossRef.addAll(
            playersIds.map {
                GameUserCrossRef(
                    gameId = game.id,
                    userId = it
                )
            }
        )

        _cards.addAll(optionalCards.mapIndexed { index, card ->
            card.copy(id = "$index")
        })

        _gameCards.addAll(
            optionalCards.mapIndexed { index, card ->
                GameCard(
                    id = "gameCardId$index",
                    gameId = game.id,
                    cardId = card.id,
                    playerId = "",
                    isPlayed = false,
                    playTime = 0L
                )
            }
        )

        _currentGame = game
        _currentGameId = game.id
    }


    /**
     * Assign a playerid and mark played
     */
    fun playCard(gameCardId: String, playerId: String): Boolean {
        val cardPlayed = _gameCards.find { it.id == gameCardId } ?: return false
        val indexOfCard = _gameCards.indexOf(cardPlayed)
        _gameCards[indexOfCard] = cardPlayed.copy(playerId = playerId, isPlayed = true)
        return true
    }

    override suspend fun insertGameUserCrossRef(crossRef: GameUserCrossRef): Response<String> {
        return try {
            _gamesUserCrossRef.add(crossRef)
            Response.Success("", "Successfully updated")
        } catch (e: Exception) {
            Response.Error("", e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun updateGameUserCrossRef(crossRef: GameUserCrossRef): Response<Boolean> {
        TODO("Not yet implemented")
    }

    override fun getCardsInDustbin(gameId: String): Flow<List<GameCardWithCard>> {
        val gameCards = _gameCards.filter { it.gameId == gameId }
        val gameCardsWithCards = gameCards.map { gameCard ->
            val card = _cards.find { it.id == gameCard.cardId }
            card?.let {
                GameCardWithCard(
                    gameCard = gameCard,
                    card = card
                )
            }
        }
        return flowOf(gameCardsWithCards.filterNotNull())
    }

    override suspend fun deleteGameUserCrossRef(crossRef: GameUserCrossRef): Response<String> {
        return try {
            _gamesUserCrossRef.remove(crossRef)
            Response.Success("", "Successfully deleted")
        } catch (exception: Exception) {
            Response.Error(
                "",
                exception.localizedMessage
                    ?: "Unknown error occurred while removing playrts from game"
            )
        }
    }


    override fun getGamesOfPlayer(playerId: String): Flow<GamesOfPlayer> {
        TODO("Not yet implemented")
    }

    override suspend fun getRecentTopCard(): UserCard {
        TODO("Not Yet Implemented")
    }

    override suspend fun changeCurrentCardType(type: String): Response<String> {
        _currentCardType = type

        return Response.Success(type, "Successfully changed type")
    }

    override suspend fun pickCardsFromLibrary(
        playerId: String,
        gameId: String,
        numOfCardsToPick: Int
    ): Response<Int> {
        val cardsAvailableForPick: MutableList<GameCard> =
            _gameCards.filter { !it.isPlayed && it.playerId.isEmpty() && it.gameId == gameId }
                .toMutableList()

        if (cardsAvailableForPick.size < numOfCardsToPick) {
            return Response.Error(
                data = cardsAvailableForPick.size,
                message = "Not enough cards in the dustbin, wanted to pick $numOfCardsToPick but found ${cardsAvailableForPick.size}"
            )
        }
        cardsAvailableForPick.shuffle()
        val pickedCards = cardsAvailableForPick.take(numOfCardsToPick)

        val updateResults: MutableList<Response<Boolean>> = ArrayList()
        pickedCards.forEach { gameCard ->
            updateResults.add(
                gameCardRepositoryImpl.updateGameCard(
                    gameCard = gameCard.copy(
                        playerId = playerId
                    )
                )
            )
        }

        return if (updateResults.any { false }) {
            val updatedResults = updateResults.count { it.data == true }
            Response.Error(
                data = 0,
                message = "Did not update all gameCards collected, updated $updatedResults of ${updateResults.size}"
            )
        } else {
            Response.Success(data = pickedCards.size, message = "Successfully picked cards")
        }
    }

    override suspend fun requestCardsFromSceneToBePicked(
        gameId: String,
        numOfCardsToPick: Int,
        numOfCardsAlreadyAvailableToPick: Int
    ): Response<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun pickRandomCardFromDB(gameId: String): GameCard {
        TODO("Not yet implemented")
    }

    override suspend fun getGameCard(cardName: String): UserCard? {
        TODO("Not yet implemented")
    }

    override suspend fun numOfCardsAvailableToPick(gameId: String): Int {
        TODO("Not yet implemented")
    }

    override suspend fun cardToBePlayedByComputer(gameId: String, playerId: String): UserCard? {
        return try {
            val currType = cardRepositoryImpl.getCurrentCardType()
            val currName = cardRepositoryImpl.getCurrCardName()

            val userCards = _gameCards
                .filter { it.playerId == playerId && !it.isPlayed }
                .map {
                    val card = _cards.find { card ->
                        card.id == it.cardId
                    } ?: return@map null
                    UserCard(
                        gameCardId = it.id,
                        playerId = it.playerId,
                        cardId = it.cardId,
                        name = card.name,
                        gameId = it.gameId,
                        type = card.type,
                        isPlayed = it.isPlayed,
                        image = card.image,
                        playTime = it.playTime
                    )
                }

            if (currType.isEmpty()) {
                userCards.randomOrNull()
            } else {
                userCards.firstOrNull { userCard ->
                    userCard?.name == currName ||
                            userCard?.type == currType ||
                            userCard?.name == Constants.CardNames.EIGHT ||
                            userCard?.name == Constants.CardNames.JOKER
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun jokerCardActionByComputer(): String {
        return listOf(
            Constants.CardJokerActions.changeType,
            Constants.CardJokerActions.hit
        ).random()
    }

    override suspend fun cardTypeForComputerToChangeTo(): String {
        return listOf(
            Constants.CardTypes.CLUBS,
            Constants.CardTypes.DIAMOND,
            Constants.CardTypes.HEART,
            Constants.CardTypes.SPADE
        )
            .filter { it != _currentCardType }
            .random()
    }

    override suspend fun cardForComputerToHitBack(gameId: String, playerId: String): UserCard? {
        val twoOrJoker =
            _cards.filter { it.name == Constants.CardNames.TWO || it.name == Constants.CardNames.JOKER }
        if (twoOrJoker.isEmpty()) {
            return null
        }

        val comGameCard = _gameCards
            .firstOrNull { it.playerId == playerId && !it.isPlayed && twoOrJoker.any { card -> card.id == it.cardId } }
            ?: return null

        val card = twoOrJoker.find { it.id == comGameCard.cardId } ?: return null

        return UserCard(
            gameCardId = comGameCard.id,
            playerId = playerId,
            cardId = card.id,
            name = card.name,
            gameId = comGameCard.gameId,
            type = card.type,
            image = card.image
        )
    }

    override suspend fun availableCardsForComputer(
        gameId: String,
        playerId: String
    ): List<UserCard> {
        TODO("Not yet implemented")
    }
    
}