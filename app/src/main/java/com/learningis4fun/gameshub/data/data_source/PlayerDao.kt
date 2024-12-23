package com.learningis4fun.gameshub.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.data.data_source.relations.GamesOfPlayer
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayer(player: Player)

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