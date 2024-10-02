package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source

import androidx.room.*
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GamesOfPlayer
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player)

    @Update
    suspend fun updatePlayer(player: Player)

    @Delete
    suspend fun deletePlayer(player: Player)

    @Query("SELECT * FROM player")
    fun getPlayers(): Flow<List<Player>>

    @Query("SELECT * FROM player WHERE uniqueId LIKE :playerId")
    fun getPlayer(playerId: String): Flow<Player?>

    @Transaction
    @Query("SELECT * FROM player WHERE uniqueId LIKE :playerId")
    fun getGamesOfPlayer(playerId: String): Flow<GamesOfPlayer>
}