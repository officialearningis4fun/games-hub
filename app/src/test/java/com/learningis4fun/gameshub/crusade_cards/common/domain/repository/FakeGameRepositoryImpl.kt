package com.learningis4fun.gameshub.crusade_cards.common.domain.repository

import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameWithPlayers
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.CardStateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class FakeGameRepositoryImpl(
    private val userRepositoryImpl: FakeUserRepositoryImpl,
    private val defaultRepositoryImpl: FakeDefaultRepositoryImpl
) : GameRepository {

    private var _currentGame = Game()

    override suspend fun insertGame(game: Game): Response<String> {
        return try {
            _currentGame = game
            Response.Success("", "Successfully updated")
        } catch (e: Exception) {
            Response.Error("", e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun updateGame(game: Game): Response<String> {
        return try {
            _currentGame = game
            Response.Success("", "Successfully updated")
        } catch (exception: Exception) {
            Response.Error(
                "",
                exception.localizedMessage ?: "Unknown error occurred while updading game"
            )
        }
    }

    override suspend fun deleteGame(game: Game): Response<String> {
        TODO("Not yet implemented")
    }

    override fun getNonCompletedGame(): Flow<Game> {
        TODO("Not yet implemented")
    }

    override fun getCompletedGames(): Flow<List<Game>> {
        TODO("Not yet implemented")
    }

    override fun getGame(gameId: String): Flow<Game?> {
        return if (_currentGame.id == gameId) {
            flowOf(_currentGame)
        } else {
            flowOf(null)
        }
    }

    override suspend fun saveGameCardState(stateEntity: CardStateEntity) {
        TODO("Not yet implemented")
    }

    override fun getGameWithPlayers(gameId: String): Flow<GameWithPlayers> {
        val gameUserIds = defaultRepositoryImpl._gameUserCrossRefs.filter { it.gameId == gameId }.map { it.userId }

        val gameWithUsers = flow {
            val game = getGame(gameId).first() ?: return@flow

            val users = userRepositoryImpl.getPlayers().first().filter {
                gameUserIds.contains(it.uniqueId)
            }
            emit(
                GameWithPlayers(
                    game = game,
                    players = users
                )
            )
        }
        return gameWithUsers
    }

    override fun getCurrentGameId(): Flow<String> {
        return flowOf(_currentGame.id)

    }

    override suspend fun updateRecentGameId(gameId: String): Response<String> {
        return try {
            _currentGame = _currentGame.copy(id = gameId)
            Response.Success("", "Successfully")
        } catch (exception: Exception) {
            Response.Error("",
                message = exception.localizedMessage
                    ?: "Unknown error occurred while updating current game id"
            )
        }
    }
}