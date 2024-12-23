package com.learningis4fun.gameshub.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import com.learningis4fun.gameshub.commons.domain.model.GameUserCrossRef

@Dao
interface GameUserCrossRefDao {

    @Insert
    suspend fun insertGameUserCrossRef(crossRef: GameUserCrossRef)

    @Update
    suspend fun updateGameUserCrossRef(crossRef: GameUserCrossRef)

    @Delete
    suspend fun deleteGameUserCrossRef(crossRef: GameUserCrossRef)

}