package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case

import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.GameState
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@ExperimentalCoroutinesApi
class PauseGameUseCase @Inject constructor(
    private val gameRepository: GameRepository,
    private val preferencesManager: CrusadeCardsPreferencesManager
) {

    /**
     * stop the counting time
     *
     * change game state to paused
     *
     * card state should already be saved
     */
    suspend fun pauseGame(currGameId: String, gameDuration: Long): Response<String> {
        try {

            if (currGameId.isEmpty()) {
                return Response.Error("", "Unable to pause the game")
            }
            val game = gameRepository.getGame(gameId = currGameId).first()?.copy(
                gameState = GameState.Paused.name,
                gameDuration = gameDuration
            ) ?: throw Exception("The game was not found")

            gameRepository.updateGame(game)
            return Response.Success(game.gameState, "Successfully paused the game")
        } catch (exception: Exception) {
            return Response.Error(
                "",
                exception.localizedMessage ?: "Unknown error occurred while pausing game"
            )
        }
    }
}