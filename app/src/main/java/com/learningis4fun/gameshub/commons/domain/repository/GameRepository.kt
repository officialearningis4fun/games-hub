package com.learningis4fun.gameshub.commons.domain.repository

import com.learningis4fun.gameshub.commons.domain.model.Game
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.data.data_source.relations.GameWithPlayers
import kotlinx.coroutines.flow.Flow

interface GameRepository {

    suspend fun insertGame(game: Game): Response<Unit>

    suspend fun updateGame(game: Game): Response<Unit>

    suspend fun deleteGame(gameId: String): Response<Unit>

    fun getNonCompletedGame(): Flow<Game>

    fun getCompletedGames(): Flow<List<Game>>

    suspend fun getNumOfCompletedGames(): Int

    fun getGame(gameId: String): Flow<Game?>

    suspend fun saveGameCardState(stateEntity: com.learningis4fun.gameshub.domain.model.CardStateEntity)

    fun getGameWithLatestStagePlayers(gameId: String): Flow<GameWithPlayers>

    fun getGameWithLatestStagePlayersSorted(gameId: String): Flow<GameWithPlayers>

    fun getGameWithPlayers(gameId: String): Flow<GameWithPlayers>

    fun getCompletedGamesWithPlayers(): Flow<List<GameWithPlayers>>

    fun getCurrentGameId(): Flow<String>

    suspend fun updateRecentGameId(gameId: String): Response<Unit>
}