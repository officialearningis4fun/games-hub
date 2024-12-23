package com.learningis4fun.gameshub.domain.use_cases

import com.learningis4fun.gameshub.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.commons.domain.util.GameState
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.commons.domain.util.toState
import com.learningis4fun.gameshub.util.getDataOrThrowException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@ExperimentalCoroutinesApi
class PauseGameUseCase @Inject constructor(
    private val gameRepository: GameRepository
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
                return Response.Error("Unable to pause the game with no ID")
            }
            val game = gameRepository.getGame(gameId = currGameId).first()
                ?: throw Exception("The game was not found")

            when (toState(game.gameState)) {
                GameState.Completed -> throw Exception("Unable to pause completed game")
                GameState.NotStarted -> throw Exception("Unable to pause unstarted game")
                GameState.Paused,
                GameState.Started -> {
                    gameRepository.updateGame(
                        game.copy(
                            gameState = GameState.Paused.name,
                            gameDuration = gameDuration
                        )
                    ).getDataOrThrowException()
                }
            }
            return Response.Success(game.gameState)
        } catch (exception: Exception) {
            return Response.Error(
                exception.localizedMessage ?: "Unknown error occurred while pausing game"
            )
        }
    }
}