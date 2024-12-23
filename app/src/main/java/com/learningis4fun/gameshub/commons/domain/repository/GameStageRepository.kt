package com.learningis4fun.gameshub.commons.domain.repository

import com.learningis4fun.gameshub.commons.domain.model.GameStage
import com.learningis4fun.gameshub.commons.domain.util.Response

interface GameStageRepository {

    suspend fun saveGameStage(gameStage: GameStage): Response<Unit>

    suspend fun deleteGameStage(gameStage: GameStage): Response<Unit>

    suspend fun deleteGameStages(gameId: String): Response<Unit>

    suspend fun getGameStages(gameId: String): Response<List<GameStage>>
}