package com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.use_case

import com.learningis4fun.gameshub.feature_crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.GameState
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.CardStateEntity
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.DefaultCardRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@ExperimentalCoroutinesApi
class EndGameUseCase @Inject constructor(
    private val preferenceManager: CrusadeCardsPreferencesManager,
    private val repository: DefaultCardRepository,
    private val gameRepository: GameRepository,
    private val gameCardRepository: GameCardRepository
) {


    /**
     * stop the counting time
     *
     * change game state to completed
     *
     * set the winMessage to have user who won
     *
     * delete all gameCards, set cardState to start
     *
     * delete gameUserCrossRef with gameId
     *
     * change playCardState to navigate to winScreen (popping off the playCardsScreen)
     */
    suspend fun endGame(currGameId: String, duration: Long, winnerId: String) {
        val gameCompletedTime = System.currentTimeMillis()


        val game = gameRepository.getGame(currGameId).first()
            ?.copy(
                gameState = GameState.Completed.name,
                winnerId = winnerId,
                gameDuration = duration,
                completedTime = gameCompletedTime
            )

        game?.let {
            gameRepository.updateGame(game)
            repository.changeCurrentCardType("")
            gameRepository.updateRecentGameId("")
            gameCardRepository.deleteAllGameCards()
            preferenceManager.updateRecentCardState(CardStateEntity(cardStateName = CardStates.Start.name))
        }

    }
}