package com.learningis4fun.gameshub.domain.use_cases

import com.learningis4fun.gameshub.commons.domain.model.Game
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.commons.domain.util.numberRepresentation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class EliminatePlayerFromGame @OptIn(ExperimentalCoroutinesApi::class)
@Inject constructor(
    private val gameCardRepository: GameCardRepository
) {

    suspend operator fun invoke(
        player: Player,
        game: Game,
        stageNumber: Int,
        eliminationLimitForCrusade: Int = Constants.DEFAULT_CRUSADE_ELIMINATION_LIMIT
    ): Response<Boolean> {
        return try {

            val totalNumberRepresentationOfUnPlayedCards = gameCardRepository
                .getUnPlayedCardsForPlayer(
                    gameId = game.id,
                    playerId = player.uniqueId,
                    stageNumber = stageNumber
                ).first()
                .sumOf { it.toCard().numberRepresentation() }

            val eliminate =
                totalNumberRepresentationOfUnPlayedCards > eliminationLimitForCrusade

            println("player: ${player.uniqueId}: eliminate: $eliminate")

            Response.Success(
                eliminate
            )

        } catch (e: Exception) {
            Response.Error(e.message.toString())
        }
    }
}