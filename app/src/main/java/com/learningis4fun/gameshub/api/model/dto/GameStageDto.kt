package com.learningis4fun.gameshub.api.model.dto

import androidx.annotation.Keep
import com.learningis4fun.gameshub.commons.domain.model.GameStage
import com.learningis4fun.gameshub.commons.domain.model.PlayerIdAndPosition
import kotlinx.serialization.Serializable

@Serializable
@Keep
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
@Keep
data class PlayerIdAndPositionDto(
    val playerId: String = "",
    val position: Int = 0
){
    constructor() : this("")
}