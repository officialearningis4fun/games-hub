package com.learningis4fun.gameshub.api

import android.util.Log
import com.learningis4fun.gameshub.commons.domain.model.GameStage
import com.learningis4fun.gameshub.util.GamesHubLogTags
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class GetLastGameStageWinnerIdOrFirstPlayerId @Inject constructor(
) {

    operator fun invoke(
        gameId: String,
        gameStages: List<GameStage>,
        winnerId: String?
    ): String? {
        return try {

            val playerIds = gameStages.flatMap { it.playerIds }.distinct()

            val latestGameStage = gameStages
                .maxByOrNull { it.stageNumber }!!

            val previousGameStage =
                if (gameStages.size > 1) {
                    runCatching {
                        gameStages.find { it.stageNumber == latestGameStage.stageNumber - 1 }!!
                    }.onFailure {
                        Log.d(GamesHubLogTags.GET_LATEST_GAME_STAGE_PLAYER, "err: $it")
                    }.getOrNull()
                } else gameStages.first()

            return when (val gameStagePlayer = gameStagePlayer(
                winnerId = winnerId,
                latestGameStage = latestGameStage
            )) {
                GameStagePlayerToStart.NoWinner -> {
                    // get first player
                    val firstPlayerId = gameStages
                        .maxByOrNull { it.stageNumber }!!
                        .playerIds
                        .first()
                        .playerId

                    playerIds.first { it.playerId == firstPlayerId }.playerId
                }

                is GameStagePlayerToStart.WinnerNotEliminated -> {
                    playerIds.first { it.playerId == gameStagePlayer.winnerId }.playerId
                }

                is GameStagePlayerToStart.WinnerEliminated -> {
                    nextGameStagePlayerAfterEliminatedPlayer(
                        playerIds = playerIds.map { it.playerId },
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
        if (e is CancellationException) throw e
        e.printStackTrace()
        GameStagePlayerToStart.NoWinner
    }
}


private fun nextGameStagePlayerAfterEliminatedPlayer(
    playerIds: List<String>,
    latestGameStage: GameStage,
    prevGameStage: GameStage?,
    eliminatedWinnerId: String
): String {

    val eliminatedWinnerIdAndPosition =
        prevGameStage?.playerIds?.first { it.playerId == eliminatedWinnerId }

    val activePlayersIdsAndPositions = latestGameStage.playerIds

    val prevGameStagePlayers = prevGameStage?.playerIds

    // from the prev players - get index of eliminated player

    val indexOfEliminatedPlayer = prevGameStagePlayers?.indexOf(eliminatedWinnerIdAndPosition)

    // check if there are other active players after eliminated
    // then take the first
    // if there are no players after
    // take the first player starting with the first in the list

    val player = indexOfEliminatedPlayer?.let {
        if (
            it < prevGameStagePlayers.lastIndex
        ) {
            prevGameStagePlayers
                .subList(indexOfEliminatedPlayer + 1, prevGameStagePlayers.size)
                .firstOrNull { playerIdAndPosition -> playerIdAndPosition in activePlayersIdsAndPositions }
                ?: prevGameStagePlayers.firstOrNull { playerIdAndPosition -> playerIdAndPosition in activePlayersIdsAndPositions }
                ?: activePlayersIdsAndPositions.first()
        } else {
            prevGameStagePlayers.firstOrNull { playerIdAndPosition -> playerIdAndPosition in activePlayersIdsAndPositions }
                ?: activePlayersIdsAndPositions.first()
        }
    }

    return playerIds.first { it == player?.playerId }

}

sealed interface GameStagePlayerToStart {
    data object NoWinner : GameStagePlayerToStart
    data class WinnerEliminated(val winnerId: String) : GameStagePlayerToStart
    data class WinnerNotEliminated(val winnerId: String) : GameStagePlayerToStart
}