package com.learningis4fun.gameshub.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.learningis4fun.gameshub.commons.domain.model.Card
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Query("SELECT * FROM card")
    suspend fun getCards(): List<Card>

    @Query("SELECT COUNT(cardId) FROM card")
    fun getNumOfCards(): Flow<Int>

    @Insert
    suspend fun insertCard(card: Card)

    @Update
    suspend fun updateCard(card: Card)

    @Delete
    suspend fun deleteCard(card: Card)
}