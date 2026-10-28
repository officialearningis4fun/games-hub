package com.learningis4fun.gameshub.api.model

import kotlinx.serialization.Serializable

@Serializable
data class GameStageDto(
    val stageNumber: Int = 1,
    val playerIds: List<PlayerIdAndPositionDto> = emptyList()
){
    constructor() : this(1, emptyList())
}

@Serializable
data class PlayerIdAndPositionDto(
    val playerId: String = "",
    val position: Int = 0
){
    constructor() : this("")
}