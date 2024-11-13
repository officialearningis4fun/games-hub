package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case

import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameStageRepository
import javax.inject.Inject

class GetLatestStageNumber @Inject constructor(
    private val gameStageRepository: GameStageRepository
) {
    suspend operator fun invoke(gameId: String): Int {
        return gameStageRepository.getGameStages(gameId).data?.firstOrNull()?.stageNumber ?: 1
    }
}