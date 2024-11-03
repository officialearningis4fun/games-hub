package com.learningis4fun.gameshub.crusade_cards.commons.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class GameStage(
    val gameId: String = "",
    val stageNumber: Int = 1,
    val playerIds: List<String> = emptyList()
) {
    fun toGameStageRef() = GameStageRef(
        gameId = gameId,
        stageNumber = stageNumber,
        playerIds = Json.encodeToString(playerIds)
    )

    fun nextStage(playerIds: List<String>): GameStage = GameStage(
        gameId = gameId,
        stageNumber = stageNumber + 1,
        playerIds = playerIds
    )
}
