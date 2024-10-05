package com.learningis4fun.gameshub.crusade_cards.commons.data.repository

import androidx.room.withTransaction
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.local.GamesDatabase
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.GameUserCrossRef
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toUserCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameCardWithCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GamesOfPlayer
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.GameCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.UserCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * db structure
 * game(id, name)
 * gameIdAndPlayerId(gameId, playerId)
 * permanent cards collection store -> at app first start -> user can change this in settings
 * Card(id, name, type, image)
 * cards per game
 * gameCard(gameId, cardId, playerId, isPlayed)
 * dao-s
 * game with cards by junction -> gameIdAndPlayerId
 * gameWithCards -> by player
 * gameWithCards -> by no player
 * gameWithCards -> by user with isPlayed true
 */

@ExperimentalCoroutinesApi
class DefaultCardRepositoryImpl @Inject constructor(
    private val db: GamesDatabase,
    val crusadeCardsPreferencesManager: CrusadeCardsPreferencesManager
) : DefaultCardRepository {

    override suspend fun insertGameUserCrossRef(crossRef: GameUserCrossRef): Response<Unit> {
        return try {
            db.gameUserCrossRef().insertGameUserCrossRef(crossRef)
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun updateGameUserCrossRef(crossRef: GameUserCrossRef): Response<Boolean> {
        return try {
            db.gameUserCrossRef().updateGameUserCrossRef(crossRef)
            Response.Success(true)
        } catch (exception: Exception) {
            Response.Error(exception.localizedMessage ?: "Unknown error occurred")
        }
    }


    override suspend fun deleteGameUserCrossRef(crossRef: GameUserCrossRef): Response<Unit> {
        return try {
            db.gameUserCrossRef().deleteGameUserCrossRef(crossRef)
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun getRecentTopCard(): UserCard {
        TODO("Not yet implemented")
    }

    override fun getGamesOfPlayer(playerId: String): Flow<GamesOfPlayer> {
        return db.playerDao().getGamesOfPlayer(playerId = playerId)
    }

    override suspend fun cardToBePlayedByComputer(gameId: String, playerId: String): UserCard? {
        val playerCards = db.gameCardDao().getUnPlayedCardsForPlayer(
            gameId, playerId
        ).first()

        return if (playerCards.isEmpty()) {
            null
        } else {
            playerCards.random().toUserCard()
        }

    }

    override suspend fun jokerCardActionByComputer(): String {
        val actions = listOf(
            Constants.CardJokerActions.changeType,
            Constants.CardJokerActions.hit
        )
        return actions.random()
    }

    override suspend fun cardTypeForComputerToChangeTo(): String {
        val types = listOf(
            Constants.CardTypes.CLUBS,
            Constants.CardTypes.HEART,
            Constants.CardTypes.DIAMOND,
            Constants.CardTypes.SPADE,
        )
        return types.random()
    }

    override suspend fun cardForComputerToHitBack(gameId: String, playerId: String): UserCard? {
        val cards = db.gameCardDao().getUnPlayedCardsForPlayer(
            gameId, playerId
        ).first()
        val cardsToHitBack = cards.filter { Constants.HITTING_CARD_NAMES.contains(it.card.name) }

        if (cardsToHitBack.isEmpty()) {
            return null
        }
        return cardsToHitBack.random().toUserCard()
    }

    override suspend fun changeCurrentCardType(type: String): Response<Unit> {
        return crusadeCardsPreferencesManager.updateRecentCardType(type)
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

    override fun getCardsInDustbin(gameId: String): Flow<List<GameCardWithCard>> {
        return db.gameCardDao().getCardsInDustbin(gameId = gameId)
    }

    override suspend fun pickRandomCardFromDB(gameId: String): GameCard {
        val cardsInDustbin = db.gameCardDao().getCardsInDustbin(gameId).first()
        return cardsInDustbin.random().gameCard
    }

    override suspend fun getGameCard(cardName: String): UserCard? {
        TODO("Not yet implemented")
    }

    override suspend fun numOfCardsAvailableToPick(gameId: String): Int {
        return db.gameCardDao().getCardsInDustbin(gameId = gameId).count()
    }

    override suspend fun availableCardsForComputer(
        gameId: String,
        playerId: String
    ): List<UserCard> {
        return db.gameCardDao().getUnPlayedCardsForPlayer(gameId, playerId).first()
            .map { it.toUserCard() }
    }

}