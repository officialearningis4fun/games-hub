package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case

import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameStageRepository
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util.getDataOrThrowException
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetLastGameStageWinnerOrFirstPlayer @Inject constructor(
    private val gameStageRepository: GameStageRepository,
    private val gameRepository: GameRepository
) {

    suspend operator fun invoke(
        gameId: String
    ): Player? {
        return try {

            val gameStages = gameStageRepository.getGameStages(gameId).getDataOrThrowException()

            val gameWithPlayers = gameRepository.getGameWithPlayers(gameId).first()

            val winnerId = gameWithPlayers.game.winnerId.ifEmpty { null }

            val firstPlayerId = gameStages
                .first()
                .playerIds
                .first()
                .playerId

            if (winnerId.isNullOrEmpty()) {
                gameWithPlayers.players.first { it.uniqueId == firstPlayerId }
            } else {
                gameWithPlayers.players.first { it.uniqueId == winnerId }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}