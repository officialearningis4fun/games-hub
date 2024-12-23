package com.learningis4fun.gameshub.domain.use_cases

import com.learningis4fun.gameshub.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.util.getDataOrThrowException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ResetGameUseCase @Inject constructor(
    val repository: DefaultCardRepository,
    private val gameRepository: GameRepository,
    private val setupGameUseCase: SetupGameUseCase
) {

    /**
     * delete game
     * create a new game with the same gameId
     */
    suspend operator fun invoke(
        gameId: String
    ): Response<Unit> {

        return try {

            val game = gameRepository.getGame(gameId).first()
                ?: throw Exception("Game not found")
            val gameWithPlayers = gameRepository.getGameWithPlayers(gameId = gameId).first()

            gameRepository.deleteGame(gameId).getDataOrThrowException()

            setupGameUseCase(
                gameName = game.name,
                createdGameId = gameId,
                players = gameWithPlayers.players
            )

            Response.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Response.Error(e.message.toString())
        }
    }
}