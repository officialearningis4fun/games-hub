@file:OptIn(ExperimentalCoroutinesApi::class)

package com.learningis4fun.gameshub.data.data_source

import com.learningis4fun.gameshub.api.use_cases.GetLatestStageNumber
import com.learningis4fun.gameshub.commons.domain.controller.CrusadeComputerCardsController
import com.learningis4fun.gameshub.commons.domain.repository.CardRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameStageRepository
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.domain.model.UserCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CrusadeComputerCardsControllerImpl2 @Inject constructor(
    private val gameRepository: GameRepository,
    private val cardRepository: CardRepository,
    private val gameCardRepository: GameCardRepository,
    private val gameStageRepository: GameStageRepository,
    private val getLatestStageNumber: GetLatestStageNumber
) : CrusadeComputerCardsController {
    override suspend fun getCurrentGameId(): String {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentCardType(): String {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentCardName(): String {
        TODO("Not yet implemented")
    }

    override suspend fun jokerCardAction(gameId: String, playerId: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentStageNumber(gameId: String): Int {
        TODO("Not yet implemented")
    }

    override suspend fun cardForComputerToHit(
        playerId: String,
        gameId: String,
        cardsToPick: Int?
    ): UserCard? {
        TODO("Not yet implemented")
    }

    override suspend fun newCardType(playerId: String, gameId: String): String {
        TODO("Not yet implemented")
    }

    override fun cardTypeButJoker(cardsTypes: List<String>): String {
        TODO("Not yet implemented")
    }

    override suspend fun playCardForComputer(
        playerId: String,
        gameId: String
    ): Response<UserCard?> {

        try {
            val currentCardType = getCurrentCardType()
            val currentCardName = getCurrentCardName()

            val card = if (currentCardType.isEmpty()) {
                // playCardWithNoRestrictions ->  // can play
            } else {
                if (currentCardName.isEmpty()) {
                    // playCardWithCardTypeRestrictions(gameId, playerId, currentCardType)
                    if (canPlayWithTypeRestriction(
                            playerId,
                            gameId,
                            currentCardType
                        )
                    ) {
                        // playCardWithTypeRestrictions can play
                    } else {
                        //pick from dustbin
                    }
                } else {
                    if (canPlayWithCardNameTypeRestriction(
                            playerId = playerId,
                            gameId = gameId,
                            currentCardType = currentCardType,
                            currentCardName = currentCardName
                        )
                    ) {
                        //  playCardWithCardTypeAndNameRestrictions
                    } else {
                        //pick from dustbin
                    }

                }

            }
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.message.toString())
        }

        TODO("Not yet implemented")

    }


    private suspend fun playCardsWithNoCardRestrictions(
        playerId: String,
        gameId: String
    ): UserCard? {
        try {
            val gameWithPlayers =
                gameRepository.getGameWithLatestStagePlayers(gameId = gameId).first()
            val opponentsCards = gameWithPlayers.players.map { player ->
                gameCardRepository.getUnPlayedCardsForPlayer(
                    gameId = gameId,
                    playerId = player.uniqueId,
                    stageNumber = getCurrentStageNumber(gameId = gameId)
                ).first()
            }

            val userCards = gameCardRepository
                .getUnPlayedCardsForPlayer(
                    gameId = gameId,
                    playerId = playerId,
                    stageNumber = getCurrentStageNumber(gameId = gameId)
                ).first()

            when (opponentsCards.closeToWinning(numOfCardsLeft = userCards.size)) {
                true -> {}
                false -> {}
            }
        } catch (e: Exception) {

        }

        return null
    }


    private fun canPlayWithTypeRestriction(
        playerId: String,
        gameId: String,
        currentCardType: String
    ): Boolean {
        return false
    }

    private fun canPlayWithCardNameTypeRestriction(
        playerId: String,
        gameId: String,
        currentCardType: String,
        currentCardName: String
    ): Boolean {
        return false
    }
}