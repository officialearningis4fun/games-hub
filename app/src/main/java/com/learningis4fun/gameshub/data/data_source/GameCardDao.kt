package com.learningis4fun.gameshub.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.learningis4fun.gameshub.data.data_source.relations.GameCardWithCard
import com.learningis4fun.gameshub.domain.model.GameCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

@ExperimentalCoroutinesApi
@Dao
interface GameCardDao {

    @Insert
    suspend fun insertGameCard(gameCard: GameCard)

    @Update
    suspend fun updateGameCard(gameCard: GameCard)

    @Delete
    suspend fun deleteGameCard(gameCard: GameCard)

    @Query("DELETE FROM gameCard")
    suspend fun deleteAllGameCards()

    @Query("DELETE FROM gameCard WHERE gameId =:gameId")
    suspend fun deleteAllGameCards(gameId: String)

    @Query("DELETE FROM gameCard WHERE gameId =:gameId AND stageNumber =:stageNumber AND playerId =:playerId")
    suspend fun deleteAllGameCardsWithoutPlayer(
        gameId: String,
        stageNumber: Int,
        playerId: String = ""
    )

    @Transaction
    @Query("SELECT * FROM gameCard WHERE gameId LIKE :gameId AND playerId LIKE :playerId AND isPlayed == 1 AND stageNumber =:stageNumber ORDER BY playTime DESC")
    fun getPlayedCards(
        gameId: String,
        playerId: String,
        stageNumber: Int
    ): Flow<List<GameCardWithCard>>

    @Transaction
    @Query("SELECT * FROM gameCard WHERE gameId LIKE :gameId AND playerId IN(:playerIds) AND isPlayed == 1 AND stageNumber =:stageNumber ORDER BY playTime DESC LIMIT 3")
    fun getTopThreePlayedCards(
        gameId: String,
        stageNumber: Int,
        playerIds: List<String>
    ): Flow<List<GameCardWithCard>>

    @Transaction
    @Query("SELECT * FROM gameCard WHERE gameId LIKE :gameId AND playerId IN(:playerIds) AND isPlayed == 1 AND stageNumber =:stageNumber ORDER BY playTime DESC")
    fun getTopPlayedCard(
        gameId: String,
        stageNumber: Int,
        playerIds: List<String>
    ): Flow<GameCardWithCard?>

    @Transaction
    @Query("SELECT * FROM gameCard WHERE gameId LIKE :gameId AND playerId LIKE :playerId AND isPlayed == 1 AND stageNumber =:stageNumber")
    fun getPlayedCardsForPlayer(
        gameId: String,
        playerId: String,
        stageNumber: Int
    ): Flow<List<GameCardWithCard>>

    @Transaction
    @Query("SELECT * FROM gameCard WHERE gameId LIKE :gameId AND playerId LIKE :playerId AND isPlayed == 0 AND stageNumber =:stageNumber")
    fun getUnPlayedCardsForPlayer(
        gameId: String,
        playerId: String,
        stageNumber: Int
    ): Flow<List<GameCardWithCard>>

    // 1731443017760 7iooVf8Vu9T8NR2JcwgqLbgr0OC3dev2 1
    @Transaction
    @Query("SELECT * FROM gameCard, card WHERE gameId LIKE :gameId AND playerId LIKE :playerId AND isPlayed == 0 AND stageNumber =:stageNumber ORDER BY cardType")
    fun getUnPlayedCardsForPlayerSortedByCardType(
        gameId: String,
        playerId: String,
        stageNumber: Int
    ): Flow<List<GameCardWithCard>>

    @Transaction
    @Query("SELECT * FROM gameCard, card WHERE gameId LIKE :gameId AND playerId LIKE :playerId AND isPlayed == 0 AND stageNumber =:stageNumber ORDER BY cardName")
    fun getUnPlayedCardsForPlayerSortedByCardName(
        gameId: String,
        playerId: String,
        stageNumber: Int
    ): Flow<List<GameCardWithCard>>

    @Transaction
    @Query("SELECT * FROM gameCard WHERE gameId LIKE :gameId AND playerId LIKE :playerId AND isPlayed == 0 AND stageNumber =:stageNumber")
    fun getCardsInDustbin(
        gameId: String,
        playerId: String = "",
        stageNumber: Int
    ): Flow<List<GameCardWithCard>>

    @Transaction
    @Query("SELECT * FROM gameCard WHERE gameId LIKE :gameId AND playerId LIKE :playerId AND isPlayed == 0 AND stageNumber =:stageNumber")
    suspend fun getGameCards(
        gameId: String,
        playerId: String = "",
        stageNumber: Int
    ): List<GameCard>

    @Query("SELECT COUNT(gameCardId) FROM gameCard WHERE gameId LIKE :gameId AND stageNumber =:stageNumber")
    fun getNumOfCurrGameCards(gameId: String, stageNumber: Int): Flow<Int>

    @Transaction
    @Query("SELECT * FROM gameCard WHERE gameCardId LIKE :gameCardId")
    suspend fun getGameCardWithCard(gameCardId: String): GameCardWithCard

    @Query("SELECT COUNT(gameCardId) FROM gameCard WHERE gameId LIKE :gameId AND playerId LIKE :playerId AND isPlayed == 0 AND stageNumber =:stageNumber")
    fun numOfUnPlayedCardsForPlayer(gameId: String, playerId: String, stageNumber: Int): Flow<Int>

    @Query("SELECT COUNT(gameCardId) FROM gameCard WHERE gameId LIKE :gameId AND playerId IN (:playerIds) AND isPlayed == 0 AND stageNumber =:stageNumber")
    fun numOfUnPlayedCardsForPlayers(
        gameId: String,
        playerIds: List<String>,
        stageNumber: Int
    ): Flow<Int>


    @Query("SELECT * FROM gameCard WHERE gameId LIKE :gameId AND playerId IN (:playerIds) AND isPlayed == 1 AND stageNumber =:stageNumber")
    fun getPlayedCardsForPlayers(
        gameId: String,
        playerIds: List<String>,
        stageNumber: Int
    ): Flow<List<GameCard>>
}