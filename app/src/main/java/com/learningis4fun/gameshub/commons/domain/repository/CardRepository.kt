package com.learningis4fun.gameshub.commons.domain.repository

import com.learningis4fun.gameshub.commons.domain.model.Card
import com.learningis4fun.gameshub.commons.domain.util.Response
import kotlinx.coroutines.flow.Flow

interface CardRepository {

    suspend fun insertCard(card: Card): Response<Unit>

    suspend fun updateCard(card: Card): Response<Unit>

    suspend fun deleteCard(card: Card): Response<Unit>

    suspend fun getCards(): List<Card>

    suspend fun getCurrCardName(): String

    suspend fun getCurrentCardType(): String

    fun getNumOfCards(): Flow<Int>
}