package com.learningis4fun.gameshub.crusade_cards.common.domain.repository

import com.learningis4fun.gameshub.commons.domain.model.Game
import com.learningis4fun.gameshub.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.data.data_source.relations.GameWithPlayers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class FakeGameRepositoryImpl(
    private val userRepositoryImpl: FakeUserRepositoryImpl,
    private val defaultRepositoryImpl: FakeDefaultRepositoryImpl
) : GameRepository {

    private var _currentGame = Game()

    override suspend fun insertGame(game: Game): Response<Unit> {
        return try {
            _currentGame = game
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun updateGame(game: Game): Response<Unit> {
        return try {
            _currentGame = game
            Response.Success(Unit)
        } catch (exception: Exception) {
            Response.Error(
                exception.localizedMessage ?: "Unknown error occurred while updading game"
            )
        }
    }

    override suspend fun deleteGame(gameId: String): Response<Unit> {
        TODO("Not yet implemented")
    }

    suspend fun deleteGame(gameId: Game): Response<Unit> {
        TODO("Not yet implemented")
    }

    override fun getNonCompletedGame(): Flow<Game> {
        TODO("Not yet implemented")
    }

    override fun getCompletedGames(): Flow<List<Game>> {
        TODO("Not yet implemented")
    }

    override suspend fun getNumOfCompletedGames(): Int {
        TODO("Not yet implemented")
    }

    override fun getGame(gameId: String): Flow<Game?> {
        return if (_currentGame.id == gameId) {
            flowOf(_currentGame)
        } else {
            flowOf(null)
        }
    }

    override suspend fun saveGameCardState(stateEntity: com.learningis4fun.gameshub.domain.model.CardStateEntity) {
        TODO("Not yet implemented")
    }

    override fun getGameWithLatestStagePlayers(gameId: String): Flow<GameWithPlayers> {
        val gameUserIds = defaultRepositoryImpl._gameUserCrossRefs.filter { it.gameId == gameId }
            .map { it.userId }

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

    override fun getGameWithLatestStagePlayersSorted(gameId: String): Flow<GameWithPlayers> {
        TODO("Not yet implemented")
    }

    override fun getGameWithPlayers(gameId: String): Flow<GameWithPlayers> {
        TODO("Not yet implemented")
    }

    override fun getCompletedGamesWithPlayers(): Flow<List<GameWithPlayers>> {
        TODO("Not yet implemented")
    }

    override fun getCurrentGameId(): Flow<String> {
        return flowOf(_currentGame.id)

    }

    override suspend fun updateRecentGameId(gameId: String): Response<Unit> {
        return try {
            _currentGame = _currentGame.copy(id = gameId)
            Response.Success(Unit)
        } catch (exception: Exception) {
            Response.Error(
                message = exception.localizedMessage
                    ?: "Unknown error occurred while updating current game id"
            )
        }
    }
}