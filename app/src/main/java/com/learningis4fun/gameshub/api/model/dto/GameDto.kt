package com.learningis4fun.gameshub.api.model.dto

import androidx.annotation.Keep
import com.learningis4fun.gameshub.commons.domain.model.ActiveGame
import com.learningis4fun.gameshub.commons.domain.model.Game
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.domain.util.GameState
import com.learningis4fun.gameshub.presentation.util.removeSAtEnd
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class GameDto(
    val gameState: String = GameState.NotStarted.name,
    val recentCardState: RecentCardStateDto = RecentCardStateDto(),
    val currentCardType: String = "",
    val currentCardName: String = "",
    val name: String = "",
    val winnerId: String = "",
    val gameDuration: Long = 0L,
    val playingDirection: String = Constants.CrusadePlayingDirection.CLOCKWISE,
    val completedTime: Long = 0L,
    val completedGameMessage: String = "",
    val playerIds: List<PlayerIdAndPositionDto> = emptyList(),
    val gameStages: List<GameStageDto> = emptyList()
){
    constructor () : this("")

    fun toActiveGame(players: List<Player>, gameId: String) = ActiveGame(
        gameState = gameState,
        recentCardState = recentCardState.toCardStateEntity(),
        currentCardName = currentCardName,
        currentCardType = currentCardType.removeSAtEnd(),
        name = name,
        winnerId = winnerId,
        gameDuration = gameDuration,
        playingDirection = playingDirection,
        completedTime = completedTime,
        completedGameMessage = completedGameMessage,
        players = players,
        gameStages = gameStages.map { it.toGameStage(gameId = gameId) }
    )

    fun toGame(gameId: String) = Game(
        id = gameId,
        gameState = gameState,
        name = name,
        winnerId = winnerId,
        gameDuration = gameDuration,
        playingDirection = playingDirection,
        completedTime = completedTime,
        completedGameMessage = completedGameMessage
    )
}
