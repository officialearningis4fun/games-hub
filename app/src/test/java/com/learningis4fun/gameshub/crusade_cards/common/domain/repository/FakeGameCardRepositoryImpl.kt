package com.learningis4fun.gameshub.crusade_cards.common.domain.repository

import com.learningis4fun.gameshub.app.MainViewModel
import com.learningis4fun.gameshub.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.data.data_source.relations.GameCardWithCard
import com.learningis4fun.gameshub.domain.model.GameCard
import com.learningis4fun.gameshub.domain.model.UserCard

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

@ExperimentalCoroutinesApi
class FakeGameCardRepositoryImpl(
    private val cardsRepositoryImpl: FakeCardRepositoryImpl
) : GameCardRepository {

    private val _gameCards: MutableList<GameCard> = ArrayList()

    override suspend fun insertGameCard(gameCard: GameCard): Response<Unit> {
        return try {
            _gameCards.add(gameCard)
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Unknown error occurred")
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

            Response.Success(true)
        } catch (exception: Exception) {
            Response.Error(
                message = exception.localizedMessage
                    ?: "Unknown error occurred while updating gamecard"
            )
        }
    }

    override suspend fun playGameCard(gameCard: GameCard): Response<Boolean> {
        val prevGameCard = _gameCards
            .find { it.id == gameCard.id } ?: return Response.Error(
            "Was unable to find the card to update"
        )
        val prevGameCardIndex = _gameCards.indexOf(prevGameCard)
        val card = cardsRepositoryImpl.getCards().find {
            it.id == gameCard.cardId
        } ?: return Response.Error("The card does not have a card created before")

        val playTime = System.currentTimeMillis()
        _gameCards[prevGameCardIndex] = gameCard.copy(playTime = playTime, isPlayed = true)

        cardsRepositoryImpl.updateCardType(card.type)

        return Response.Success(true)
    }

    override suspend fun deleteGameCard(gameCard: GameCard): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllGameCards(): Response<Unit> {
        return try {
            _gameCards.clear()
            Response.Success(Unit)
        } catch (exception: Exception) {
            Response.Error(
                exception.localizedMessage ?: "Unknown error occurred while deleting all cards"
            )
        }
    }

    override suspend fun deleteAllGameCardsWithoutPlayer(
        gameId: String,
        stageNumber: Int
    ): Response<Unit> {
        TODO("Not yet implemented")
    }

    override fun getNumOfCurrentGameCards(gameId: String, stageNumber: Int): Flow<Int> {
        return flowOf(
            _gameCards.size
        )
    }

    override suspend fun getGameCards(gameId: String, stageNumber: Int): List<GameCard> {
        return _gameCards
    }

    override fun numOfUnPlayedCardsForPlayer(
        gameId: String,
        playerId: String,
        stageNumber: Int
    ): Flow<Int> {
        return flowOf(
            _gameCards
                .filter {
                    playerId == it.playerId
                }
                .count { !it.isPlayed }
        )
    }

    override fun numOfUnPlayedCardsForPlayers(
        gameId: String,
        playerIds: List<String>,
        stageNumber: Int
    ): Flow<Int> {
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
        playerIds: List<String>, stageNumber: Int
    ): Flow<List<UserCard>> {
        TODO("Not yet implemented")
    }

    override fun getTopPlayedCard(
        gameId: String,
        playerIds: List<String>, stageNumber: Int
    ): Flow<GameCardWithCard?> {
        TODO("Not yet implemented")
    }

    override fun getUnPlayedCardsForPlayer(
        gameId: String,
        playerId: String,
        stageNumber: Int
    ): Flow<List<UserCard>> =
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
        numOfCardsToPick: Int, stageNumber: Int
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
            Response.Success(numOfCardsToPick)
        } catch (exception: Exception) {
            Response.Error(exception.message.toString())
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

            Response.Success(data = userCard)
        } catch (exception: Exception) {
            Response.Error(message = exception.message.toString())
        }
    }

    override suspend fun requestCardsFromSceneToBePicked(
        gameId: String,
        stageNumber: Int,
        numOfCardsToPick: Int,
        numOfCardsAlreadyAvailableToPick: Int
    ): Response<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun numOfCardsAvailableToPick(gameId: String, stageNumber: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getUnPlayedCardsForPlayerSorted(
        gameId: String,
        playerId: String,
        stageNumber: Int,
        sortBy: MainViewModel.UserCardsSortBy
    ): Flow<List<UserCard>> {
        TODO("Not yet implemented")
    }
}