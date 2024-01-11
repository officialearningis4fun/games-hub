package com.learningis4fun.gameshub.crusade_cards.common.domain.repository

import com.learningis4fun.gameshub.MainViewModel
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toUserCard
import com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.data.data_source.relations.GameCardWithCard
import com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.domain.model.GameCard
import com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.domain.model.UserCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

@ExperimentalCoroutinesApi
class FakeGameCardRepositoryImpl(
    private val cardsRepositoryImpl: FakeCardRepositoryImpl
) : GameCardRepository {

    private val _gameCards: MutableList<GameCard> = ArrayList()

    override suspend fun insertGameCard(gameCard: GameCard): Response<String> {
        return try {
            _gameCards.add(gameCard)
            Response.Success("", "Successfully updated")
        } catch (e: Exception) {
            Response.Error("", e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun updateGameCard(gameCard: GameCard): Response<Boolean> {
        return try {
            val prevGameCard = _gameCards
                .find { it.id == gameCard.id }
                ?: throw Exception("Was unable to find the card to update")

            val prevGameCardIndex = _gameCards.indexOf(prevGameCard)
            _gameCards[prevGameCardIndex] = gameCard
            val card = cardsRepositoryImpl.getCards().find {
                it.id == gameCard.cardId
            } ?: throw Exception("The card does not have a card created before")

            val userCard = UserCard(
                gameCardId = gameCard.id,
                playerId = gameCard.playerId,
                cardId = gameCard.cardId,
                name = card.name,
                gameId = gameCard.gameId,
                type = card.type,
                isPlayed = gameCard.isPlayed,
                image = card.image,
                playTime = 0L
            )

            Response.Success(true, "Successfully updated")
        } catch (exception: Exception) {
            Response.Error(
                data = true,
                message = exception.localizedMessage
                    ?: "Unknown error occurred while updating gamecard"
            )
        }
    }

    override suspend fun playGameCard(gameCard: GameCard): Response<Boolean> {
        val prevGameCard = _gameCards
            .find { it.id == gameCard.id } ?: return Response.Error(
            false,
            "Was unable to find the card to update"
        )
        val prevGameCardIndex = _gameCards.indexOf(prevGameCard)
        val card = cardsRepositoryImpl.getCards().find {
            it.id == gameCard.cardId
        } ?: return Response.Error(false, "The card does not have a card created before")

        val playTime = System.currentTimeMillis()
        _gameCards[prevGameCardIndex] = gameCard.copy(playTime = playTime, isPlayed = true)

        cardsRepositoryImpl.updateCardType(card.type)

        return Response.Success(true, "Successfully played")
    }

    override suspend fun deleteGameCard(gameCard: GameCard): Response<String> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllGameCards(): Response<String> {
        return try {
            _gameCards.clear()
            Response.Success("", message = "Success")
        } catch (exception: Exception) {
            Response.Error(
                "",
                exception.localizedMessage ?: "Unknown error occurred while deleting all cards"
            )
        }
    }

    override fun getNumOfCurrentGameCards(gameId: String): Flow<Int> {
        return flowOf(
            _gameCards.size
        )
    }

    override suspend fun getGameCards(gameId: String): List<GameCard> {
        return _gameCards
    }

    override fun numOfUnPlayedCardsForPlayer(gameId: String, playerId: String): Flow<Int> {
        return flowOf(
            _gameCards
                .filter {
                    playerId == it.playerId
                }
                .count { !it.isPlayed }
        )
    }

    override fun numOfUnPlayedCardsForPlayers(gameId: String, playerIds: List<String>): Flow<Int> {
        return flowOf(
            _gameCards
                .filter {
                    playerIds.contains(it.playerId)
                }
                .count { !it.isPlayed }
        )
    }

    override fun getThreeTopPlayedCards(
        gameId: String,
        playerIds: List<String>
    ): Flow<List<UserCard>> {
        TODO("Not yet implemented")
    }

    override fun getTopPlayedCard(
        gameId: String,
        playerIds: List<String>
    ): Flow<GameCardWithCard?> {
        TODO("Not yet implemented")
    }

    override fun getUnPlayedCardsForPlayer(gameId: String, playerId: String): Flow<List<UserCard>> =
        flow {
            if (gameId.isEmpty() || playerId.isEmpty()) {
                emit(emptyList())
            }
            val cards = cardsRepositoryImpl.getCards()
            val gameCards = _gameCards
                .filter { it.playerId == playerId && it.gameId == gameId && !it.isPlayed }
                .map { gameCard ->
                    val card = cards.find { it.id == gameCard.cardId } ?: return@flow
                    GameCardWithCard(gameCard = gameCard, card = card).toUserCard()
                }
            emit(gameCards)
        }

    override suspend fun pickCardsFromLibrary(
        playerId: String,
        gameId: String,
        numOfCardsToPick: Int
    ): Response<Int> {
        return try {
            val availableCards = _gameCards.count {
                !it.isPlayed
            }
            if (numOfCardsToPick <= availableCards) {
                val cardsToPickFrom = _gameCards.filter { !it.isPlayed }
                val pickedCards = cardsToPickFrom
                    .take(numOfCardsToPick)
                    .map { it.copy(playerId = playerId, pickedTime = System.currentTimeMillis()) }

                pickedCards.forEach {
                    updateGameCard(it)
                }
            }
            Response.Success(numOfCardsToPick, "Successfully picked")
        } catch (exception: Exception) {
            Response.Error(0, exception.localizedMessage.toString())
        }
    }

    override suspend fun getGameCardWithCard(gameCardId: String): Response<UserCard> {
        return try {
            val gameCard = _gameCards.firstOrNull { it.id == gameCardId }
                ?: throw Exception("gameCard is not available")
            val card =
                cardsRepositoryImpl.getCards().find { it.id == gameCard.cardId } ?: throw Exception(
                    "card is not availabled"
                )
            val userCard = GameCardWithCard(
                card = card,
                gameCard = gameCard
            ).toUserCard()

            Response.Success(data = userCard, message = "Successful")
        } catch (exception: Exception) {
            Response.Error(data = UserCard(), message = exception.localizedMessage.toString())
        }
    }

    override suspend fun requestCardsFromSceneToBePicked(
        gameId: String,
        numOfCardsToPick: Int,
        numOfCardsAlreadyAvailableToPick: Int
    ): Response<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun numOfCardsAvailableToPick(gameId: String): Int {
        TODO("Not yet implemented")
    }

    override fun getUnPlayedCardsForPlayerSorted(
        gameId: String,
        playerId: String,
        sortBy: MainViewModel.UserCardsSortBy
    ): Flow<List<UserCard>> {
        TODO("Not yet implemented")
    }
}