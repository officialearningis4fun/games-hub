package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source

import androidx.room.*
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
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