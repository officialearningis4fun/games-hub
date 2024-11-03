package com.learningis4fun.gameshub.crusade_cards.commons.data.repository

import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.local.GamesDatabase
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.GameStage
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameStageRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import javax.inject.Inject

class GameStageRepositoryImpl @Inject constructor(
    private val db: GamesDatabase
) : GameStageRepository {

    override suspend fun saveGameStage(gameStage: GameStage): Response<Unit> {
        return try {
            db.gameStageDao().saveGameStage(gameStageRef = gameStage.toGameStageRef())
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.message.toString())
        }
    }

    override suspend fun deleteGameStage(gameStage: GameStage): Response<Unit> {
        return try {
            db.gameStageDao().deleteGameStage(gameStageRef = gameStage.toGameStageRef())
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.message.toString())
        }
    }

    override suspend fun deleteGameStages(gameId: String): Response<Unit> {
        return try {
            db.gameStageDao().deleteGameStages(gameId = gameId)
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.message.toString())
        }
    }

    override suspend fun getGameStages(gameId: String): Response<List<GameStage>> {
        return try {
            val gameStages = db.gameStageDao()
                .getGameStages(gameId = gameId)
                .map { gameStageRef -> gameStageRef.toGameStage() }

            Response.Success(gameStages)
        } catch (e: Exception) {
            Response.Error(e.message.toString())
        }
    }
}