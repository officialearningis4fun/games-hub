package com.learningis4fun.gameshub.crusade_cards.commons.domain.controller

import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.UserCard
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
interface CrusadeComputerCardsController {

    suspend fun getCurrentGameId(): String

    suspend fun getCurrentCardType(): String

    suspend fun getCurrentCardName(): String

    suspend fun getCurrentStageNumber(gameId: String): Int

    suspend fun cardForComputerToHit(playerId: String): UserCard?

    suspend fun newCardType(
        playerId: String
    ): String

    fun cardTypeButJoker(cardsTypes: List<String> = emptyList()): String

    suspend fun playCardForComputer(
        playerId: String,
        gameId: String
    ): UserCard?

}