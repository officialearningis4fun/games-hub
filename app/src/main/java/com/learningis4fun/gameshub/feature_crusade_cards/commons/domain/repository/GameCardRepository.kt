package com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository

import com.learningis4fun.gameshub.MainViewModel
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.data.data_source.relations.GameCardWithCard
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.GameCard
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.UserCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

@ExperimentalCoroutinesApi
interface GameCardRepository {

    suspend fun insertGameCard(gameCard: GameCard): Response<String>

    suspend fun updateGameCard(gameCard: GameCard): Response<Boolean>

    suspend fun playGameCard(gameCard: GameCard): Response<Boolean>

    suspend fun deleteGameCard(gameCard: GameCard): Response<String>

    suspend fun deleteAllGameCards(): Response<String>

    fun getNumOfCurrentGameCards(gameId: String): Flow<Int>

    suspend fun getGameCards(gameId: String): List<GameCard>

    fun numOfUnPlayedCardsForPlayer(gameId: String, playerId: String): Flow<Int>

    fun numOfUnPlayedCardsForPlayers(gameId: String, playerIds: List<String>): Flow<Int>

    fun getThreeTopPlayedCards(gameId: String, playerIds: List<String>): Flow<List<UserCard>>

    fun getTopPlayedCard(gameId: String, playerIds: List<String>): Flow<GameCardWithCard?>

    fun getUnPlayedCardsForPlayer(gameId: String, playerId: String): Flow<List<UserCard>>

    suspend fun pickCardsFromLibrary(playerId: String, gameId: String, numOfCardsToPick: Int): Response<Int>

    suspend fun getGameCardWithCard(gameCardId: String): Response<UserCard>

    suspend fun requestCardsFromSceneToBePicked(
        gameId: String,
        numOfCardsToPick: Int,
        numOfCardsAlreadyAvailableToPick: Int
    ): Response<Int>

    suspend fun numOfCardsAvailableToPick(gameId: String): Int

    fun getUnPlayedCardsForPlayerSorted(
        gameId: String,
        playerId: String,
        sortBy: MainViewModel.UserCardsSortBy
    ): Flow<List<UserCard>>

}