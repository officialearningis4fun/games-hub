package com.learningis4fun.gameshub.crusade_cards.commons.data.repository

import androidx.room.withTransaction
import com.learningis4fun.gameshub.MainViewModel
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.local.GamesDatabase
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toUserCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameCardWithCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.GameCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.UserCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
class GameCardRepositoryImpl @Inject constructor(
    private val db: GamesDatabase
) : GameCardRepository {


    override suspend fun insertGameCard(gameCard: GameCard): Response<Unit> {
        return try {
            db.gameCardDao().insertGameCard(gameCard)
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun updateGameCard(gameCard: GameCard): Response<Boolean> {
        return try {
            db.gameCardDao().updateGameCard(gameCard)
            Response.Success(true)
        } catch (exception: Exception) {
            Response.Error(exception.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun playGameCard(gameCard: GameCard): Response<Boolean> {
        val playTime = System.currentTimeMillis()
        return try {
            db.gameCardDao().updateGameCard(gameCard.copy(playTime = playTime, isPlayed = true))
            Response.Success(true)
        } catch (exception: Exception) {
            Response.Error(exception.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun deleteGameCard(gameCard: GameCard): Response<Unit> {
        return try {
            db.gameCardDao().deleteGameCard(gameCard)
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun deleteAllGameCards(): Response<Unit> {
        return try {
            db.gameCardDao().deleteAllGameCards()
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override fun getNumOfCurrentGameCards(gameId: String): Flow<Int> {
        return db.gameCardDao().getNumOfCurrGameCards(gameId = gameId)
    }

    override suspend fun getGameCards(gameId: String): List<GameCard> {
        return db.gameCardDao().getGameCards(gameId = gameId)
    }

    override fun numOfUnPlayedCardsForPlayer(gameId: String, playerId: String): Flow<Int> {
        return db.gameCardDao().numOfUnPlayedCardsForPlayer(gameId, playerId).map {
            it
        }
    }

    override fun numOfUnPlayedCardsForPlayers(gameId: String, playerIds: List<String>): Flow<Int> {
        return db.gameCardDao().numOfUnPlayedCardsForPlayers(gameId, playerIds)
    }

    override fun getThreeTopPlayedCards(
        gameId: String,
        playerIds: List<String>
    ): Flow<List<UserCard>> {
        return db.gameCardDao().getTopThreePlayedCards(
            gameId = gameId,
            playerIds = playerIds
        ).map {
            it.map { gameCardWithCard ->
                gameCardWithCard.toUserCard()
            }
        }
    }

    override suspend fun getGameCardWithCard(gameCardId: String): Response<UserCard> {
        return try {
            val gameCard = db.gameCardDao().getGameCardWithCard(gameCardId = gameCardId)
            if (gameCard == GameCardWithCard(GameCard(), Card())) {
                throw Exception("Unable to retrieve game card")
            }
            Response.Success(gameCard.toUserCard())
        } catch (exception: Exception) {
            Response.Error(
                exception.localizedMessage ?: "Unknown error retrieving game card"
            )
        }
    }

    override fun getTopPlayedCard(
        gameId: String,
        playerIds: List<String>
    ): Flow<GameCardWithCard?> {
        return db.gameCardDao().getTopPlayedCard(gameId, playerIds)
    }

    override fun getUnPlayedCardsForPlayer(gameId: String, playerId: String): Flow<List<UserCard>> {
        return db.gameCardDao().getUnPlayedCardsForPlayer(gameId, playerId)
            .map {
                it.map { gameCardWithCard ->
                    gameCardWithCard.toUserCard()
                }
            }
    }

    override suspend fun pickCardsFromLibrary(
        playerId: String,
        gameId: String,
        numOfCardsToPick: Int
    ): Response<Int> {
        return try {
            val cardsInDustbin = db.gameCardDao().getCardsInDustbin(gameId)
                .first()

            val numOfCardsInDustbin = cardsInDustbin.size

            if (numOfCardsInDustbin >= numOfCardsToPick) {
                // can safely pick
                pickingCardsFromDustbin(
                    playerId = playerId,
                    cardsToPick = cardsInDustbin
                        .shuffled()
                        .take(numOfCardsToPick)
                        .map {
                            it.gameCard
                        })
            } else {
                // request other cards first
                val response = requestCardsFromSceneToBePicked(
                    gameId = gameId,
                    numOfCardsToPick = numOfCardsToPick,
                    numOfCardsAlreadyAvailableToPick = numOfCardsInDustbin
                )

                when (response) {
                    is Response.Error -> {
                        throw Exception(response.message)
                    }

                    is Response.Success -> {

                        val cardsInDustbinAfterRequesting =
                            db.gameCardDao().getCardsInDustbin(gameId)
                                .first()
                                .shuffled()
                                .take(numOfCardsToPick)
                                .map {
                                    it.gameCard
                                }

                        pickingCardsFromDustbin(
                            playerId = playerId,
                            cardsToPick = cardsInDustbinAfterRequesting
                        )
                    }
                }
            }

        } catch (exception: Exception) {
            Response.Error(
                exception.localizedMessage ?: "Unknown error occurred while picking cards"
            )
        }
    }

    private suspend fun pickingCardsFromDustbin(
        playerId: String,
        cardsToPick: List<GameCard>
    ): Response<Int> {
        return try {
            db.withTransaction {
                val pickTime = System.currentTimeMillis()

                cardsToPick.forEach {
                    db.gameCardDao().updateGameCard(
                        it.copy(
                            playerId = playerId,
                            pickedTime = pickTime
                        )
                    )
                }
            }
            Response.Success(data = cardsToPick.size)
        } catch (exception: Exception) {
            Response.Error(
                message = exception.localizedMessage ?: "Unknown error occurred while picking cards"
            )
        }
    }

    override suspend fun requestCardsFromSceneToBePicked(
        gameId: String,
        numOfCardsToPick: Int,
        numOfCardsAlreadyAvailableToPick: Int
    ): Response<Int> {

        return try {
            val playersIds = db.gameDao().getGameWithPlayers(gameId).first()
                .players
                .map {
                    it.uniqueId
                }

            val cardsInDustbin = db.gameCardDao().getPlayedCardsForPlayers(
                gameId = gameId,
                playerIds = playersIds
            )
                .first()
                .sortedByDescending { it.playTime }
            val sizeOfCardsInDustbin = cardsInDustbin.size

            val numOfTotalCardsAvailableForPicking =
                sizeOfCardsInDustbin - 1 + numOfCardsAlreadyAvailableToPick

            if (numOfTotalCardsAvailableForPicking >= numOfCardsToPick) {

                val cardsToRequest = cardsInDustbin
                    .slice(1..cardsInDustbin.lastIndex)

                // request cards
                db.withTransaction {
                    cardsToRequest.forEach {
                        db.gameCardDao().updateGameCard(
                            it.copy(
                                isPlayed = false,
                                playerId = "",
                                playTime = 0L
                            )
                        )
                    }
                }
                Response.Success(cardsToRequest.size)

            } else {
                throw Exception("Not enough cards to be picked")
            }
        } catch (exception: Exception) {
            Response.Error(
                exception.localizedMessage
                    ?: "Unknown error occurred while requesting cards to be picked"
            )
        }
    }

    override suspend fun numOfCardsAvailableToPick(gameId: String): Int {
        return db.gameCardDao().getCardsInDustbin(gameId = gameId).count()
    }

    override fun getUnPlayedCardsForPlayerSorted(
        gameId: String,
        playerId: String,
        sortBy: MainViewModel.UserCardsSortBy
    ): Flow<List<UserCard>> {
        return db.gameCardDao().getUnPlayedCardsForPlayerSorted(gameId, playerId, sortBy)
            .map {
                it.map { gameCardWithCard ->
                    gameCardWithCard.toUserCard()
                }
            }
    }

}