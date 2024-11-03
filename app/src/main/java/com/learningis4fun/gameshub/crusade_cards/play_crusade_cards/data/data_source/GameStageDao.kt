package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.GameStageRef

@Dao
interface GameStageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameStage(gameStageRef: GameStageRef)

    @Query("SELECT * FROM gameStage WHERE gameId =:gameId ORDER BY stageNumber DESC")
    suspend fun getGameStages(gameId: String): List<GameStageRef>

    @Delete
    suspend fun deleteGameStage(gameStageRef: GameStageRef)

    @Query("DELETE FROM gameStage WHERE gameId=:gameId")
    suspend fun deleteGameStages(gameId: String)
}