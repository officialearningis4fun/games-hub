package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case

import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.GameCard
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.DefaultCardRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@ExperimentalCoroutinesApi
class AssignCardsUseCase @Inject constructor(
    val repository: DefaultCardRepository,
    private val gameRepository: GameRepository,
    private val gameCardRepository: GameCardRepository
) {

    suspend fun assignCards(
        gameId: String,
        howManyCardsPerPlayer: Int = Constants.DEFAULT_CARDS_PER_USER
    ): Boolean {
        return try {

            val playersIds: List<String> =
                gameRepository.getGameWithPlayers(gameId = gameId).first().players.map { it.uniqueId }
            val cardsToAssign = gameCardRepository.getGameCards(gameId)
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