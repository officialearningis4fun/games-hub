package com.learningis4fun.gameshub.api

import com.learningis4fun.gameshub.commons.domain.model.ActiveGame
import com.learningis4fun.gameshub.commons.domain.model.Card
import com.learningis4fun.gameshub.commons.domain.model.Game
import com.learningis4fun.gameshub.commons.domain.model.GameStage
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.domain.model.CardStateEntity
import com.learningis4fun.gameshub.domain.model.CompletedGame
import com.learningis4fun.gameshub.domain.model.GameCard
import com.learningis4fun.gameshub.domain.model.UserCard
import com.learningis4fun.gameshub.presentation.start.OnGoingGame
import kotlinx.coroutines.flow.Flow

interface CrusadeGameService {

    suspend fun playCard(userCard: UserCard): Response<Unit>
    suspend fun pickCards(
        playerId: String,
        gameId: String,
        stageNumber: Int,
        numOfCardsToPick: Int,
    ): Response<Int>

    suspend fun changeCardType(playerId: String, gameId: String, type: String): Response<Unit>
    suspend fun updateGamePlayingDirection(gameId: String, direction: String): Response<Unit>

    suspend fun getLatestStagePlayerIdsSorted(gameId: String): Response<List<String>>

    fun getUnPlayedCardsForUser(
        playerId: String,
        stageNumber: Int,
        gameId: String
    ): Flow<List<UserCard>>

    suspend fun getUnPlayedCardsForPlayers(
        gameId: String,
        playerIds: List<String>,
        gameStages: List<GameStage>,
    ): List<UserCard>

    suspend fun deletedUnPlayedCards(
        gameId: String,
        stageNumber: Int,
    ): Response<Unit>

    fun getNumOfUnPlayedCardsForUser(
        playerId: String,
        stageNumber: Int,
        gameId: String
    ): Flow<Int>

    suspend fun getTopPlayedCard(
        gameId: String,
        playerIds: List<String>,
        stageNumber: Int
    ): Response<UserCard>

    suspend fun getPlayedCards(
        stageNumber: Int,
        gameId: String
    ): Response<List<GameCard>>

    suspend fun getCard(
        gameId: String,
        gameCardId: String
    ): Response<Card>

    suspend fun getUserCard(
        gameId: String,
        gameCardId: String
    ): Response<UserCard>

    suspend fun updateCardState(
        gameId: String,
        cardStateEntity: CardStateEntity
    ): Response<Unit>

    fun getTopThreePlayedCards(
        gameId: String,
        playerIds: List<String>,
        stageNumber: Int
    ): Flow<List<UserCard>>

    fun recentGame(gameId: String): Flow<ActiveGame?>

    suspend fun createGameCards(
        gameId: String,
        stageNumber: Int,
        playerIds: List<String>
    ): Response<Unit>

    suspend fun createGame(
        opponentIds: List<String>
    ): Response<Unit>

    suspend fun deleteGame(gameId: String): Response<Unit>

    suspend fun endGame(
        currGameId: String,
        stageNumber: Int,
        duration: Long,
        winnerId: String,
        playerIds: List<String>
    ): Response<Unit>

    suspend fun pauseGame(
        gameId: String,
        duration: Long,
    ): Response<Unit>

    suspend fun resumeGame(
        gameId: String
    ): Response<Unit>

    suspend fun resetGame(
        gameId: String
    ): Response<Unit>

    suspend fun getGame(
        gameId: String
    ): Response<Game>

    fun getCompletedGames(): Flow<List<CompletedGame>>

    fun getUnCompletedGames(): Flow<List<OnGoingGame>>

    fun getFriends(): Flow<List<Player>>

    suspend fun addUserToFriends(email: String): Response<Unit>

    suspend fun saveAccount(): Response<Unit>
}