package com.learningis4fun.gameshub.crusade_cards.commons.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.learningis4fun.gameshub.MainViewModel
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.local.GamesDatabase
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardsErrorMessages
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameCardWithCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.GameCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.UserCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util.GamesHubLogTags
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

    override suspend fun deleteAllGameCardsWithoutPlayer(
        gameId: String,
        stageNumber: Int
    ): Response<Unit> {
        return try {
            db.gameCardDao().deleteAllGameCardsWithoutPlayer(gameId, stageNumber)
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.message.toString())
        }
    }

    override fun getNumOfCurrentGameCards(gameId: String, stageNumber: Int): Flow<Int> {
        return db.gameCardDao().getNumOfCurrGameCards(gameId = gameId, stageNumber)
    }

    override suspend fun getGameCards(gameId: String, stageNumber: Int): List<GameCard> {
        return db.gameCardDao().getGameCards(gameId = gameId, stageNumber = stageNumber)
    }

    override fun numOfUnPlayedCardsForPlayer(
        gameId: String,
        playerId: String,
        stageNumber: Int
    ): Flow<Int> {
        return db.gameCardDao().numOfUnPlayedCardsForPlayer(gameId, playerId, stageNumber).map {
            it
        }
    }

    override fun numOfUnPlayedCardsForPlayers(
        gameId: String,
        playerIds: List<String>,
        stageNumber: Int
    ): Flow<Int> {
        return db.gameCardDao().numOfUnPlayedCardsForPlayers(gameId, playerIds, stageNumber)
    }

    override fun getThreeTopPlayedCards(
        gameId: String,
        playerIds: List<String>,
        stageNumber: Int
    ): Flow<List<UserCard>> {
        return db.gameCardDao().getTopThreePlayedCards(
            gameId = gameId,
            playerIds = playerIds,
            stageNumber = stageNumber
        ).map {
            Log.d(
                GamesHubLogTags.GAME_CARD_REPOSITORY,
                "params: gameId:$gameId; playerIds:$playerIds; stageNumber:$stageNumber"
            )
            Log.d("GameCardRepository", it.toString())
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
        playerIds: List<String>,
        stageNumber: Int
    ): Flow<GameCardWithCard?> {
        return db.gameCardDao()
            .getTopPlayedCard(gameId = gameId, playerIds = playerIds, stageNumber = stageNumber)
    }

    override fun getUnPlayedCardsForPlayer(
        gameId: String,
        playerId: String,
        stageNumber: Int
    ): Flow<List<UserCard>> {
        return db.gameCardDao().getUnPlayedCardsForPlayer(gameId, playerId, stageNumber)
            .map {
                Log.d(GamesHubLogTags.GAME_CARD_REPOSITORY, "userCards: $it")
                it.map { gameCardWithCard ->
                    gameCardWithCard.toUserCard()
                }
            }
    }

    override suspend fun pickCardsFromLibrary(
        playerId: String,
        gameId: String,
        numOfCardsToPick: Int,
        stageNumber: Int
    ): Response<Int> {
        return try {
            val cardsInDustbin =
                db.gameCardDao().getCardsInDustbin(gameId = gameId, stageNumber = stageNumber)
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
                    numOfCardsAlreadyAvailableToPick = numOfCardsInDustbin,
                    stageNumber = stageNumber
                )

                when (response) {
                    is Response.Error -> {
                        throw Exception(response.message)
                    }

                    is Response.Success -> {

                        val cardsInDustbinAfterRequesting =
                            db.gameCardDao().getCardsInDustbin(gameId, stageNumber = stageNumber)
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
        stageNumber: Int,
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
                playerIds = playersIds,
                stageNumber = stageNumber
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
                throw Exception(CardsErrorMessages.NOT_ENOUGH_CARDS_TO_BE_PICKED)
            }
        } catch (exception: Exception) {
            Response.Error(
                exception.localizedMessage
                    ?: "Unknown error occurred while requesting cards to be picked"
            )
        }
    }

    override suspend fun numOfCardsAvailableToPick(gameId: String, stageNumber: Int): Int {
        return db.gameCardDao().getCardsInDustbin(gameId = gameId, stageNumber = stageNumber)
            .count()
    }

    override fun getUnPlayedCardsForPlayerSorted(
        gameId: String,
        playerId: String,
        stageNumber: Int,
        sortBy: MainViewModel.UserCardsSortBy
    ): Flow<List<UserCard>> {

        return when (sortBy) {
            MainViewModel.UserCardsSortBy.CardName -> {
                db.gameCardDao().getUnPlayedCardsForPlayerSortedByCardName(
                    gameId = gameId,
                    playerId = playerId,
                    stageNumber = stageNumber
                )
            }

            MainViewModel.UserCardsSortBy.CardType -> {
                db.gameCardDao().getUnPlayedCardsForPlayerSortedByCardType(
                    gameId = gameId,
                    playerId = playerId,
                    stageNumber = stageNumber
                )
            }

            MainViewModel.UserCardsSortBy.TimePicked -> {
                db.gameCardDao().getUnPlayedCardsForPlayerSortedByCardType(
                    gameId = gameId,
                    playerId = playerId,
                    stageNumber = stageNumber
                )
            }
        }.map {
            it.map { gameCardWithCard ->
                gameCardWithCard.toUserCard()
            }
        }
    }

}