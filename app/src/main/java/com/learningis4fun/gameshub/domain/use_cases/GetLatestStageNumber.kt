package com.learningis4fun.gameshub.domain.use_cases

import com.learningis4fun.gameshub.commons.domain.repository.GameStageRepository
import javax.inject.Inject

class GetLatestStageNumber @Inject constructor(
    private val gameStageRepository: GameStageRepository
) {
    suspend operator fun invoke(gameId: String): Int {
        return gameStageRepository.getGameStages(gameId).data?.firstOrNull()?.stageNumber ?: 1
    }
}