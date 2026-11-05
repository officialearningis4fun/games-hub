package com.learningis4fun.gameshub.api.model.dto

import com.learningis4fun.gameshub.commons.domain.model.GameStage
import com.learningis4fun.gameshub.commons.domain.model.PlayerIdAndPosition
import kotlinx.serialization.Serializable

@Serializable
data class GameStageDto(
    val stageNumber: Int = 1,
    val playerIds: List<PlayerIdAndPositionDto> = emptyList()
){
    constructor() : this(1, emptyList())
    fun toGameStage(
        gameId: String,
    ) = GameStage(
        gameId = gameId,
        stageNumber = stageNumber,
        playerIds = playerIds.map { PlayerIdAndPosition(
            playerId = it.playerId,
            position = it.position
        ) }
    )
}

@Serializable
data class PlayerIdAndPositionDto(
    val playerId: String = "",
    val position: Int = 0
){
    constructor() : this("")
}