package com.learningis4fun.gameshub.crusade_cards.commons.domain.repository

import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameCardWithCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GamesOfPlayer
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.GameCard
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.GameUserCrossRef
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.UserCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

@ExperimentalCoroutinesApi
interface DefaultCardRepository {

    suspend fun insertGameUserCrossRef(crossRef: GameUserCrossRef): Response<Unit>

    suspend fun updateGameUserCrossRef(crossRef: GameUserCrossRef): Response<Boolean>

    suspend fun deleteGameUserCrossRef(crossRef: GameUserCrossRef): Response<Unit>

    suspend fun getRecentTopCard(): UserCard

    fun getGamesOfPlayer(playerId: String): Flow<GamesOfPlayer>

    fun getCardsInDustbin(gameId: String): Flow<List<GameCardWithCard>>

    suspend fun pickRandomCardFromDB(gameId: String): GameCard

    suspend fun getGameCard(cardName: String): UserCard?

    suspend fun changeCurrentCardType(type: String): Response<Unit>

    suspend fun pickCardsFromLibrary(
        playerId: String,
        gameId: String,
        numOfCardsToPick: Int
    ): Response<Int>

    suspend fun requestCardsFromSceneToBePicked(
        gameId: String,
        numOfCardsToPick: Int,
        numOfCardsAlreadyAvailableToPick: Int
    ): Response<Int>

    suspend fun numOfCardsAvailableToPick(gameId: String): Int

    suspend fun cardToBePlayedByComputer(gameId: String, playerId: String): UserCard?
    suspend fun jokerCardActionByComputer(): String
    suspend fun cardTypeForComputerToChangeTo(): String
    suspend fun cardForComputerToHitBack(gameId: String, playerId: String): UserCard?

    suspend fun availableCardsForComputer(gameId: String, playerId: String): List<UserCard>

}