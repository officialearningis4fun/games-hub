package com.learningis4fun.gameshub.crusade_cards.commons.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.serialization.json.Json

@Entity(tableName = "gameStage", primaryKeys = ["gameId", "stageNumber"])
data class GameStageRef(
    @ColumnInfo(name = "gameId")
    val gameId: String = "",
    @ColumnInfo(name = "stageNumber")
    val stageNumber: Int = 1,
    @ColumnInfo(name = "playerIds")
    val playerIds: String = "[]"
) {
    fun toGameStage() = GameStage(
        gameId = gameId,
        stageNumber = stageNumber,
        playerIds = Json.decodeFromString(playerIds)
    )
}
