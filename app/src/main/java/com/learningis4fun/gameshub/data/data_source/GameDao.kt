package com.learningis4fun.gameshub.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.learningis4fun.gameshub.commons.domain.model.Game
import com.learningis4fun.gameshub.commons.domain.util.GameState
import com.learningis4fun.gameshub.data.data_source.relations.GameWithPlayers
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Insert
    suspend fun insertGame(game: Game)

    @Update
    suspend fun updateGame(game: Game)

    @Delete
    suspend fun deleteGame(game: Game)

    @Query("SELECT * FROM game WHERE gameId LIKE :gameId")
    suspend fun getGame(gameId: String): Game

    @Query("SELECT * FROM game WHERE gameId LIKE :gameId")
    fun getCurrGame(gameId: String): Flow<Game?>

    @Query("SELECT COUNT(gameId) FROM game WHERE gameState LIKE :gameState")
    suspend fun getNumOfCompletedGames(gameState: String = GameState.Completed.name): Int


    @Query("SELECT * FROM game WHERE gameState LIKE :gameState ORDER BY gameCompletedTime DESC")
    fun getCompletedGames(gameState: String = GameState.Completed.name): Flow<List<Game>>

    @Query("SELECT * FROM game WHERE gameState IN(:gameStates)")
    fun getNonCompletedGame(
        gameStates: List<String> = listOf(
            GameState.Started.name,
            GameState.Paused.name,
            GameState.NotStarted.name
        )
    ): Flow<Game>

    @Query("SELECT * FROM game WHERE gameState IN(:gameStates)")
    fun getNonCompletedGames(
        gameStates: List<String> = listOf(
            GameState.Started.name,
            GameState.Paused.name,
            GameState.NotStarted.name
        )
    ): Flow<List<GameWithPlayers>>

    @Transaction
    @Query("SELECT * FROM game WHERE gameId LIKE :gameId")
    fun getGameWithPlayers(gameId: String): Flow<GameWithPlayers>

    @Transaction
    @Query("SELECT * FROM game WHERE gameState LIKE :gameState ORDER BY gameId DESC ")
    fun getCompletedGamesWithPlayers(gameState: String = GameState.Completed.name): Flow<List<GameWithPlayers>>

}