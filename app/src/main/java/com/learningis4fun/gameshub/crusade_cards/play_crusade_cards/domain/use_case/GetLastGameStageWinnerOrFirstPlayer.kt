package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case

import android.util.Log
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.GameStage
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameStageRepository
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameWithPlayers
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util.GamesHubLogTags
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util.getDataOrThrowException
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetLastGameStageWinnerOrFirstPlayer @Inject constructor(
    private val gameStageRepository: GameStageRepository,
    private val gameRepository: GameRepository
) {

    suspend operator fun invoke(
        gameId: String
    ): Player? {
        return try {

            val gameStages = gameStageRepository.getGameStages(gameId).getDataOrThrowException()

            val gameWithPlayers = gameRepository.getGameWithPlayers(gameId).first()

            val winnerId = gameWithPlayers.game.winnerId.ifEmpty { null }

            val latestGameStage = gameStages
                .first()

            val previousGameStage =
                if (gameStages.size > 1) gameStages.take(2).last() else gameStages.first()

            when (val gameStagePlayer = gameStagePlayer(
                winnerId = winnerId,
                latestGameStage = latestGameStage
            )) {
                GameStagePlayerToStart.NoWinner -> {
                    // get first player
                    val firstPlayerId = gameStages
                        .first()
                        .playerIds
                        .first()
                        .playerId

                    gameWithPlayers.players.first { it.uniqueId == firstPlayerId }
                }

                is GameStagePlayerToStart.WinnerNotEliminated -> {
                    gameWithPlayers.players.first { it.uniqueId == gameStagePlayer.winnerId }
                }

                is GameStagePlayerToStart.WinnerEliminated -> {
                    nextGameStagePlayerAfterEliminatedPlayer(
                        gameWithPlayers = gameWithPlayers,
                        latestGameStage = latestGameStage,
                        prevGameStage = previousGameStage,
                        eliminatedWinnerId = gameStagePlayer.winnerId
                    )
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(
                GamesHubLogTags.GET_LATEST_GAME_STAGE_PLAYER,
                "err: $e"
            )
            null
        }
    }
}


private fun isWinnerEliminated(
    winnerId: String,
    latestGameStage: GameStage
): Boolean {
    return latestGameStage.playerIds.any { it.playerId == winnerId }.not()
}

private fun gameStagePlayer(
    winnerId: String?,
    latestGameStage: GameStage
): GameStagePlayerToStart {
    return try {
        if (winnerId.isNullOrEmpty()) throw Exception("No winner")
        if (isWinnerEliminated(
                winnerId,
                latestGameStage
            )
        ) {
            GameStagePlayerToStart.WinnerEliminated(winnerId)
        } else {
            GameStagePlayerToStart.WinnerNotEliminated(winnerId)
        }
    } catch (e: Exception) {
        GameStagePlayerToStart.NoWinner
    }
}


private fun nextGameStagePlayerAfterEliminatedPlayer(
    gameWithPlayers: GameWithPlayers,
    latestGameStage: GameStage,
    prevGameStage: GameStage,
    eliminatedWinnerId: String
): Player? {

    val eliminatedWinnerIdAndPosition =
        prevGameStage.playerIds.first { it.playerId == eliminatedWinnerId }

    val activePlayersIdsAndPositions = latestGameStage.playerIds

    val prevGameStagePlayers = prevGameStage.playerIds

    // from the prev players - get index of eliminated player

    val indexOfEliminatedPlayer = prevGameStagePlayers.indexOf(eliminatedWinnerIdAndPosition)

    // check if there are other active players after eliminated
    // then take the first
    // if there are no players after
    // take the first player starting with the first in the list

    val player = if (
        indexOfEliminatedPlayer < prevGameStagePlayers.lastIndex
    ) {
        prevGameStagePlayers
            .subList(indexOfEliminatedPlayer + 1, prevGameStagePlayers.size)
            .firstOrNull { it in activePlayersIdsAndPositions }
            ?: prevGameStagePlayers.firstOrNull { it in activePlayersIdsAndPositions }
            ?: activePlayersIdsAndPositions.first()
    } else {
        prevGameStagePlayers.firstOrNull { it in activePlayersIdsAndPositions }
            ?: activePlayersIdsAndPositions.first()
    }

    return gameWithPlayers.players.first { it.uniqueId == player.playerId }

}

sealed interface GameStagePlayerToStart {
    data object NoWinner : GameStagePlayerToStart
    data class WinnerEliminated(val winnerId: String) : GameStagePlayerToStart
    data class WinnerNotEliminated(val winnerId: String) : GameStagePlayerToStart
}