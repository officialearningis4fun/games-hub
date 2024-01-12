package com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.data.data_source

import androidx.room.*
import com.learningis4fun.gameshub.MainViewModel
import com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.data.data_source.relations.GameCardWithCard
import com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.domain.model.GameCard
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

    @Transaction
    @Query("SELECT * FROM gameCard WHERE gameId LIKE :gameId AND playerId LIKE :playerId AND isPlayed == 1 ORDER BY playTime DESC")
    fun getPlayedCards(gameId: String, playerId: String): Flow<List<GameCardWithCard>>

    @Transaction
    @Query("SELECT * FROM gameCard WHERE gameId LIKE :gameId AND playerId IN(:playerIds) AND isPlayed == 1 ORDER BY playTime DESC LIMIT 3")
    fun getTopThreePlayedCards(
        gameId: String,
        playerIds: List<String>
    ): Flow<List<GameCardWithCard>>

    @Transaction
    @Query("SELECT * FROM gameCard WHERE gameId LIKE :gameId AND playerId IN(:playerIds) AND isPlayed == 1 ORDER BY playTime DESC")
    fun getTopPlayedCard(gameId: String, playerIds: List<String>): Flow<GameCardWithCard?>

    @Transaction
    @Query("SELECT * FROM gameCard WHERE gameId LIKE :gameId AND playerId LIKE :playerId AND isPlayed == 1")
    fun getPlayedCardsForPlayer(gameId: String, playerId: String): Flow<List<GameCardWithCard>>

    @Transaction
    @Query("SELECT * FROM gameCard WHERE gameId LIKE :gameId AND playerId LIKE :playerId AND isPlayed == 0")
    fun getUnPlayedCardsForPlayer(gameId: String, playerId: String): Flow<List<GameCardWithCard>>

    fun getUnPlayedCardsForPlayerSorted(
        gameId: String,
        playerId: String,
        sortBy: MainViewModel.UserCardsSortBy
    ): Flow<List<GameCardWithCard>> {
        return when (sortBy) {
            MainViewModel.UserCardsSortBy.CardName -> {
                getUnPlayedCardsForPlayerSortedByCardName(gameId, playerId)
            }
            MainViewModel.UserCardsSortBy.CardType -> {
                getUnPlayedCardsForPlayerSortedByCardType(gameId, playerId)
            }
            MainViewModel.UserCardsSortBy.TimePicked -> {
                getUnPlayedCardsForPlayerSortedByCardType(gameId, playerId)
            }
        }
    }

    @Transaction
    @Query("SELECT * FROM gameCard, card WHERE gameId LIKE :gameId AND playerId LIKE :playerId AND isPlayed == 0 ORDER BY cardType")
    fun getUnPlayedCardsForPlayerSortedByCardType(
        gameId: String,
        playerId: String
    ): Flow<List<GameCardWithCard>>

    @Transaction
    @Query("SELECT * FROM gameCard, card WHERE gameId LIKE :gameId AND playerId LIKE :playerId AND isPlayed == 0 ORDER BY cardName")
    fun getUnPlayedCardsForPlayerSortedByCardName(
        gameId: String,
        playerId: String
    ): Flow<List<GameCardWithCard>>

    @Transaction
    @Query("SELECT * FROM gameCard WHERE gameId LIKE :gameId AND playerId LIKE :playerId AND isPlayed == 0")
    fun getCardsInDustbin(gameId: String, playerId: String = ""): Flow<List<GameCardWithCard>>

    @Transaction
    @Query("SELECT * FROM gameCard WHERE gameId LIKE :gameId AND playerId LIKE :playerId AND isPlayed == 0")
    suspend fun getGameCards(gameId: String, playerId: String = ""): List<GameCard>

    @Query("SELECT COUNT(gameCardId) FROM gameCard WHERE gameId LIKE :gameId")
    fun getNumOfCurrGameCards(gameId: String): Flow<Int>

    @Transaction
    @Query("SELECT * FROM gameCard WHERE gameCardId LIKE :gameCardId")
    suspend fun getGameCardWithCard(gameCardId: String): GameCardWithCard

    @Query("SELECT COUNT(gameCardId) FROM gameCard WHERE gameId LIKE :gameId AND playerId LIKE :playerId AND isPlayed == 0")
    fun numOfUnPlayedCardsForPlayer(gameId: String, playerId: String): Flow<Int>

    @Query("SELECT COUNT(gameCardId) FROM gameCard WHERE gameId LIKE :gameId AND playerId IN (:playerIds) AND isPlayed == 0")
    fun numOfUnPlayedCardsForPlayers(gameId: String, playerIds: List<String>): Flow<Int>


    @Query("SELECT * FROM gameCard WHERE gameId LIKE :gameId AND playerId IN (:playerIds) AND isPlayed == 1")
    fun getPlayedCardsForPlayers(gameId: String, playerIds: List<String>): Flow<List<GameCard>>
}