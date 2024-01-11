package com.learningis4fun.gameshub.crusade_cards.common.domain.repository

import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.CardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeCardRepositoryImpl(
): CardRepository {

    private val _cards:MutableList<Card> = ArrayList()
    private var _currentCardType = ""
    private var _currentCardName = ""

    override suspend fun insertCard(card: Card): Response<String> {
        return try {
            _cards.add(card)
            Response.Success("", "Successfully updated")
        } catch (e: Exception) {
            Response.Error("", e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun updateCard(card: Card): Response<String> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteCard(card: Card): Response<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getCards(): List<Card> {
        return _cards
    }

    override suspend fun getCurrCardName(): String {
        return _currentCardName
    }

    override suspend fun getCurrentCardType(): String {
        return _currentCardType
    }

    fun updateCardType(type: String){
        _currentCardType = type
    }

    override fun getNumOfCards(): Flow<Int> {
        return flowOf(_cards.size)
    }
}