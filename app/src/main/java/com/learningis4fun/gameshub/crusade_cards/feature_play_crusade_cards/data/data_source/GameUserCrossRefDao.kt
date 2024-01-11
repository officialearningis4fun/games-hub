package com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.data.data_source

import androidx.room.*
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.GameUserCrossRef

@Dao
interface GameUserCrossRefDao {

    @Insert
    suspend fun insertGameUserCrossRef(crossRef: GameUserCrossRef)

    @Update
    suspend fun updateGameUserCrossRef(crossRef: GameUserCrossRef)

    @Delete
    suspend fun deleteGameUserCrossRef(crossRef: GameUserCrossRef)

}