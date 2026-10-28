package com.learningis4fun.gameshub.api.model

import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.domain.util.GameState
import kotlinx.serialization.Serializable

@Serializable
data class GameDto(
    val gameState: String = GameState.NotStarted.name,
    val recentCardState: RecentCardStateDto = RecentCardStateDto(),
    val name: String = "",
    val winnerId: String = "",
    val gameDuration: Long = 0L,
    val playingDirection: String = Constants.CrusadePlayingDirection.CLOCKWISE,
    val completedTime: Long = 0L,
    val completedGameMessage: String = "",
    val playerIds: List<PlayerIdAndPositionDto> = emptyList(),
    val gameStage: GameStageDto = GameStageDto()
){
    constructor () : this("")
}
