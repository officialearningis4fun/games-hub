package com.learningis4fun.gameshub.crusade_cards.commons.domain.repository

import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import kotlinx.coroutines.flow.Flow

interface CardRepository {

    suspend fun insertCard(card: Card) : Response<String>

    suspend fun updateCard(card: Card): Response<String>

    suspend fun deleteCard(card: Card) : Response<String>

    suspend fun getCards() : List<Card>

    suspend fun getCurrCardName() : String

    suspend fun getCurrentCardType() : String

    fun getNumOfCards() : Flow<Int>
}