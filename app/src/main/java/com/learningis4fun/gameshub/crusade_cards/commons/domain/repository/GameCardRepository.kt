package com.learningis4fun.gameshub.crusade_cards.commons.domain.repository

import com.learningis4fun.gameshub.MainViewModel
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameCardWithCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.GameCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.UserCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

@ExperimentalCoroutinesApi
interface GameCardRepository {

    suspend fun insertGameCard(gameCard: GameCard): Response<Unit>

    suspend fun updateGameCard(gameCard: GameCard): Response<Boolean>

    suspend fun playGameCard(gameCard: GameCard): Response<Boolean>

    suspend fun deleteGameCard(gameCard: GameCard): Response<Unit>

    suspend fun deleteAllGameCards(): Response<Unit>

    suspend fun deleteAllGameCardsWithoutPlayer(gameId: String, stageNumber: Int): Response<Unit>

    fun getNumOfCurrentGameCards(gameId: String, stageNumber: Int): Flow<Int>

    suspend fun getGameCards(gameId: String, stageNumber: Int): List<GameCard>

    fun numOfUnPlayedCardsForPlayer(gameId: String, playerId: String, stageNumber: Int): Flow<Int>

    fun numOfUnPlayedCardsForPlayers(gameId: String, playerIds: List<String>, stageNumber: Int): Flow<Int>

    fun getThreeTopPlayedCards(gameId: String, playerIds: List<String>, stageNumber: Int): Flow<List<UserCard>>

    fun getTopPlayedCard(gameId: String, playerIds: List<String>, stageNumber: Int): Flow<GameCardWithCard?>

    fun getUnPlayedCardsForPlayer(gameId: String, playerId: String, stageNumber: Int): Flow<List<UserCard>>

    suspend fun pickCardsFromLibrary(playerId: String, gameId: String, numOfCardsToPick: Int, stageNumber: Int): Response<Int>

    suspend fun getGameCardWithCard(gameCardId: String): Response<UserCard>

    suspend fun requestCardsFromSceneToBePicked(
        gameId: String,
        stageNumber: Int,
        numOfCardsToPick: Int,
        numOfCardsAlreadyAvailableToPick: Int
    ): Response<Int>

    suspend fun numOfCardsAvailableToPick(gameId: String, stageNumber: Int): Int

    fun getUnPlayedCardsForPlayerSorted(
        gameId: String,
        playerId: String,
        stageNumber: Int,
        sortBy: MainViewModel.UserCardsSortBy
    ): Flow<List<UserCard>>

}