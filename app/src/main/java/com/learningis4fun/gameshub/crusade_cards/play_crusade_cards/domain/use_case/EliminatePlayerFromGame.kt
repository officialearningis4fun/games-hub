package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case

import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.numberRepresentation
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
        eliminationLimitForCrusade: Int = Constants.DEFAULT_CRUSADE_ELIMINATION_LIMIT
    ): Response<Boolean> {
        return try {

            val totalNumberRepresentationOfUnPlayedCards = gameCardRepository
                .getUnPlayedCardsForPlayer(
                    gameId = game.id,
                    playerId = player.uniqueId
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