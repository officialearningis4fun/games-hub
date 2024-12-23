package com.learningis4fun.gameshub.domain.use_cases

import com.learningis4fun.gameshub.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.domain.model.GameCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@ExperimentalCoroutinesApi
class AssignCardsUseCase @Inject constructor(
    val repository: DefaultCardRepository,
    private val gameRepository: GameRepository,
    private val gameCardRepository: GameCardRepository,
    private val getLatestStageNumber: GetLatestStageNumber
) {

    suspend fun assignCards(
        gameId: String,
        howManyCardsPerPlayer: Int = Constants.DEFAULT_CARDS_PER_USER
    ): Boolean {
        return try {

            val playersIds: List<String> =
                gameRepository.getGameWithLatestStagePlayers(gameId = gameId)
                    .first().players.map { it.uniqueId }

            val stageNumber = getLatestStageNumber(gameId)

            val cardsToAssign = gameCardRepository.getGameCards(gameId, stageNumber)
                .shuffled()
                .take(howManyCardsPerPlayer * playersIds.size)

            val playersCards: MutableList<GameCard> = ArrayList(cardsToAssign)

            var maxIndex = playersCards.size - 1
            var minIndex = playersCards.size - howManyCardsPerPlayer
            playersIds.forEach { id ->
                val pickTime = System.currentTimeMillis()

                for (count in maxIndex downTo minIndex) {

                    val gameCard = playersCards[count]
                    playersCards.remove(gameCard)
                    gameCardRepository.updateGameCard(
                        gameCard.copy(
                            playerId = id,
                            pickedTime = pickTime
                        )
                    )
                }
                maxIndex = minIndex - 1
                minIndex -= howManyCardsPerPlayer
            }

            true
        } catch (e: Exception) {
            false
        }
    }
}