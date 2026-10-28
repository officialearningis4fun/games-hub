package com.learningis4fun.gameshub.data.repository

import com.learningis4fun.gameshub.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.data.data_source.local.GamesDatabase
import com.learningis4fun.gameshub.commons.domain.model.Card
import com.learningis4fun.gameshub.commons.domain.repository.CardRepository
import com.learningis4fun.gameshub.commons.domain.util.Response
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@ExperimentalCoroutinesApi
class CardRepositoryImpl @Inject constructor(
    private val db: GamesDatabase,
    private val crusadeCardsPreferencesManager: CrusadeCardsPreferencesManager,
) : CardRepository {

    override suspend fun insertCard(card: Card): Response<Unit> {
        return try {
            db.cardDao().insertCard(card)
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun updateCard(card: Card): Response<Unit> {
        return try {
            db.cardDao().updateCard(card)
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun deleteCard(card: Card): Response<Unit> {
        return try {
            db.cardDao().deleteCard(card)
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun getCards(): List<Card> {
        return db.cardDao().getCards()
    }

    override suspend fun getCurrCardName(): String {
        val gameId = crusadeCardsPreferencesManager.recentGameId.first()
        if (gameId.isEmpty()) {
            return ""
        }

        val currentStageNumber = db.gameStageDao()
            .getGameStages(gameId)
            .first()
            .stageNumber

        val playersOfGameIds =
            db.gameDao().getGameWithPlayers(gameId = gameId).first()?.players?.map { it.uniqueId } ?: emptyList()
        val topPlayedCard = db.gameCardDao().getTopPlayedCard(
            gameId = gameId,
            playerIds = playersOfGameIds,
            stageNumber = currentStageNumber
        ).first()
        return topPlayedCard?.card?.name ?: ""
    }

    override suspend fun getCurrentCardType(): String {
        return crusadeCardsPreferencesManager.recentCardType.first()
    }

    override fun getNumOfCards(): Flow<Int> {
        return db.cardDao().getNumOfCards()
    }

}