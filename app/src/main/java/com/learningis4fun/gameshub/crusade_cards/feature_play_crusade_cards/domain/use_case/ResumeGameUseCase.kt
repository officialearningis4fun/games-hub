package com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.domain.use_case

import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.GameState
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toState
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.DefaultCardRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ResumeGameUseCase @Inject constructor(
    val repository: DefaultCardRepository,
    private val gameRepository: GameRepository,
    private val preferencesManager: CrusadeCardsPreferencesManager
) {

    suspend fun resumeGame(currGameId: String): Response<String> {

        try {

            if (currGameId.isEmpty()) {
                return Response.Error("", "Unable to resume the game, Game to be resumed was created")
            }

            val currGame = gameRepository.getGame(currGameId).first()
                ?: throw Exception("Game to be resumed was not found")

            val prevSavedGameState = toState(currGame.gameState)

            return when (prevSavedGameState) {
                GameState.Completed -> {
                    return Response.Error(
                        "",
                        "Unable to resume game because it was completed"
                    )
                }
                GameState.NotStarted -> {
                    gameRepository.updateGame(currGame.copy(gameState = GameState.Started.name))
                    preferencesManager.updateRecentGameId(currGame.id)
                    //_gameDuration.value = Pair(true, currGame.gameDuration)
                    Response.Success("", "Successfully started the game")
                }
                GameState.Paused -> {
                    gameRepository.updateGame(currGame.copy(gameState = GameState.Started.name))
                    //_gameDuration.value = Pair(true, currGame.gameDuration)
                    Response.Success("", "Successfully resumed the game")
                }
                GameState.Started -> {
                    Response.Success("", "The game was already started")
                }
            }

        } catch (exception: Exception) {
            return Response.Error(
                "",
                exception.localizedMessage ?: "Unknown error occurred while resuming game"
            )
        }

    }
}