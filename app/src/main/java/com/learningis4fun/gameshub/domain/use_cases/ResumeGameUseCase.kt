package com.learningis4fun.gameshub.domain.use_cases

import com.learningis4fun.gameshub.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.commons.domain.util.GameState
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.commons.domain.util.toState
import com.learningis4fun.gameshub.util.getDataOrThrowException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ResumeGameUseCase @Inject constructor(
    val repository: DefaultCardRepository,
    private val gameRepository: GameRepository,
    private val preferencesManager: CrusadeCardsPreferencesManager
) {

    suspend fun resumeGame(currGameId: String): Response<Unit> {

        try {

            println("requested to resume game")

            if (currGameId.isEmpty()) {
                throw Exception("Game to be resumed was not found")
            }

            val currGame = gameRepository.getGame(currGameId).first()
                ?: throw Exception("Game to be resumed was not found")

            val prevSavedGameState = toState(currGame.gameState)

            return when (prevSavedGameState) {
                GameState.Completed -> {
                    return Response.Error(
                        "Unable to resume game because it was completed"
                    )
                }

                GameState.NotStarted -> {
                    gameRepository.updateGame(currGame.copy(gameState = GameState.Started.name))
                        .getDataOrThrowException()
                    preferencesManager.updateRecentGameId(currGame.id).getDataOrThrowException()
                    //_gameDuration.value = Pair(true, currGame.gameDuration)
                    Response.Success(Unit)
                }

                GameState.Paused -> {
                    gameRepository.updateGame(currGame.copy(gameState = GameState.Started.name))
                    //_gameDuration.value = Pair(true, currGame.gameDuration)
                    Response.Success(Unit)
                }

                GameState.Started -> {
                    Response.Success(Unit)
                }
            }

        } catch (exception: Exception) {
            exception.printStackTrace()
            return Response.Error(
                exception.localizedMessage ?: "Unknown error occurred while resuming game"
            )
        }

    }
}