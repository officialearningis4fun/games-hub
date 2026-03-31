package com.learningis4fun.gameshub.commons.domain.controller

import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.domain.model.UserCard
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
interface CrusadeComputerCardsController {

    suspend fun getCurrentGameId(): String

    suspend fun getCurrentCardType(): String

    suspend fun getCurrentCardName(): String

    suspend fun jokerCardAction(
        gameId: String,
        playerId: String
    ): String

    suspend fun getCurrentStageNumber(gameId: String): Int

    suspend fun cardForComputerToHit(playerId: String, gameId: String, cardsToPick: Int?): UserCard?

    suspend fun newCardType(
        playerId: String,
        gameId: String
    ): String

    fun cardTypeButJoker(cardsTypes: List<String> = emptyList()): String

    suspend fun playCardForComputer(
        playerId: String,
        gameId: String
    ): Response<UserCard?>

}