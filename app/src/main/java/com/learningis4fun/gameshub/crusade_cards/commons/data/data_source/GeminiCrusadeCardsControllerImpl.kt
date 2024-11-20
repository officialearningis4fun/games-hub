@file:OptIn(ExperimentalCoroutinesApi::class)

package com.learningis4fun.gameshub.crusade_cards.commons.data.data_source

import com.learningis4fun.gameshub.crusade_cards.commons.domain.controller.CrusadeComputerCardsController
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.CardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameStageRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.UserCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util.getDataOrThrowException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GeminiCrusadeCardsControllerImpl @Inject constructor(
    private val gameRepository: GameRepository,
    private val cardRepository: CardRepository,
    private val gameCardRepository: GameCardRepository,
    private val gameStageRepository: GameStageRepository
) : CrusadeComputerCardsController {

    override suspend fun getCurrentGameId() = gameRepository.getCurrentGameId().first()

    override suspend fun getCurrentCardType() = cardRepository.getCurrentCardType()

    override suspend fun getCurrentCardName() = cardRepository.getCurrCardName()

    override suspend fun getCurrentStageNumber(gameId: String): Int =
        gameStageRepository.getGameStages(gameId).getDataOrThrowException().first().stageNumber

    private suspend fun playCardWithNoRestrictions(
        gameId: String,
        playerId: String
    ): Response<UserCard?> {

        return try {
            if (gameId.isEmpty()) {
                throw Exception("Game is not ready, create a game")
            }

            if (playerId.isEmpty()) {
                throw Exception("Player is not ready, make sure the player is created")
            }


            val cards =
                gameCardRepository.getUnPlayedCardsForPlayer(
                    gameId = gameId,
                    playerId = playerId,
                    getCurrentStageNumber(gameId)
                )
                    .first()
                    .filterNot {
                        it.name == Constants.CardNames.EIGHT
                    }

            val cardTypeWithMostMultiplicity = cards
                .groupBy { it.type }
                .map {
                    Pair(it.key, it.value.size)
                }
                .maxByOrNull {
                    it.second
                }

            val whatTypeToPlay = cardTypeWithMostMultiplicity?.first
            val cardToPlay = cards.firstOrNull {
                it.type == whatTypeToPlay
            }
            val userCard =
                cardToPlay ?: cards.randomOrNull() ?: throw Exception("User card not found")

            Response.Success(data = userCard)
        } catch (exception: Exception) {
            Response.Error(message = exception.message.toString())
        }
    }

    override suspend fun cardForComputerToHit(playerId: String): UserCard? {
        val gameCards = gameCardRepository.getUnPlayedCardsForPlayer(
            gameId = getCurrentGameId(),
            playerId = playerId,
            stageNumber = getCurrentStageNumber(gameId = getCurrentGameId())
        ).firstOrNull()
        return gameCards?.find { it.name == Constants.CardNames.TWO || it.name == Constants.CardNames.JOKER }
    }

    override suspend fun newCardType(
        playerId: String
    ): String {
        val cards = gameCardRepository.getUnPlayedCardsForPlayer(
            gameId = getCurrentGameId(),
            playerId = playerId,
            stageNumber = getCurrentStageNumber(gameId = getCurrentGameId())
        ).first()

        val typeWithMultiplicity = cards.groupBy { it.type }
            .map { Pair(it.key, it.value.size) }
            .maxByOrNull { it.second }

        val cardsButJokers = cards
            .filter { it.type != Constants.CardTypes.BLACK_JOKER && it.type != Constants.CardTypes.RED_JOKER }
            .map { it.type }

        return if (typeWithMultiplicity?.first == Constants.CardTypes.BLACK_JOKER || typeWithMultiplicity?.first == Constants.CardTypes.RED_JOKER) {
            cardTypeButJoker(
                cardsTypes = cardsButJokers
            )
        } else {
            typeWithMultiplicity?.first ?: cardTypeButJoker(
                cardsTypes = cardsButJokers
            )
        }
    }

    override fun cardTypeButJoker(cardsTypes: List<String>): String {
        val defaultCardTypes = listOf(
            Constants.CardTypes.CLUBS,
            Constants.CardTypes.SPADE,
            Constants.CardTypes.DIAMOND,
            Constants.CardTypes.HEART,
        )

        return cardsTypes.randomOrNull() ?: defaultCardTypes.random()
    }

    override suspend fun playCardForComputer(
        playerId: String,
        gameId: String
    ): UserCard? {

        val currentCardType = getCurrentCardType()
        val currentCardName = getCurrentCardName()

        return if (currentCardType.isEmpty()) {
            playCardWithNoRestrictions(gameId, playerId).data
        } else {

            if (currentCardName.isEmpty()) {
                playCardWithCardTypeRestrictions(gameId, playerId, currentCardType)
            } else {
                playCardWithCardTypeAndNameRestrictions(
                    gameId,
                    playerId,
                    currentCardType,
                    currentCardName
                )
            }

        }

    }

    suspend fun playCardWithCardTypeAndNameRestrictions(
        gameId: String,
        playerId: String,
        currentCardType: String,
        currentCardName: String
    ): UserCard? {
        val cards =
            gameCardRepository.getUnPlayedCardsForPlayer(
                gameId = gameId,
                playerId = playerId,
                getCurrentStageNumber(gameId)
            )
                .first()

        val cardTypesWithMultiplicity = cards
            .groupBy { it.type }
            .map {
                Pair(it.key, it.value.size)
            }

        val availableCardTypesOfSameCardName = cards
            .filter { it.name == currentCardName }
            .map { it.type }

        val possibleCardTypeToSwitchTo = cardTypesWithMultiplicity
            .filter {
                it.first == currentCardType
                        || availableCardTypesOfSameCardName.any { type ->
                    type == it.first
                }
            }
            .maxByOrNull {
                it.second
            }?.first

        return cards
            .filter {
                it.type == possibleCardTypeToSwitchTo
            }
            .randomOrNull()

    }

    suspend fun playCardWithCardTypeRestrictions(
        gameId: String,
        playerId: String,
        currentCardType: String
    ): UserCard? {
        val cards =
            gameCardRepository.getUnPlayedCardsForPlayer(
                gameId = gameId,
                playerId = playerId,
                getCurrentStageNumber(gameId)
            )
                .first()

        val cardTypeWithMultiplicity = cards
            .groupBy { it.type }

        val cardsWithCurrentType = cards
            .filter { it.type == currentCardType }

        if (cardsWithCurrentType.isEmpty()) {
            return null
        }

        val otherCardsWithSameNameAsCardToBePlayed = cards
            .filterNot { it.type == currentCardType }
            .filter { userCard -> cardsWithCurrentType.map { it.name }.contains(userCard.name) }

        if (otherCardsWithSameNameAsCardToBePlayed.isEmpty()) {
            return null
        }

        // amongst this card names - which has it's type as the most

        val otherCardsWithSameNameAsCardToBePlayedSortedByMultiplicity =
            otherCardsWithSameNameAsCardToBePlayed
                .maxByOrNull {
                    cardTypeWithMultiplicity[it.type]?.size ?: 0
                }

        val nameOfCardToPlay = otherCardsWithSameNameAsCardToBePlayedSortedByMultiplicity?.name

        return cardsWithCurrentType.find { it.name == nameOfCardToPlay }
            ?: cardsWithCurrentType.randomOrNull()
    }

}