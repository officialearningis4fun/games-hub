package com.learningis4fun.gameshub.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.learningis4fun.gameshub.api.model.dto.GameCardDto

@Entity(tableName = "gameCard")
data class GameCard(
    @ColumnInfo(name = "gameCardId")
    @PrimaryKey(autoGenerate = false)
    val id: String = "",
    @ColumnInfo(name = "gameId")
    val gameId: String = "",
    @ColumnInfo(name = "cardId")
    val cardId: String = "",
    @ColumnInfo(name = "playerId")
    val playerId: String = "",
    @ColumnInfo(name = "stageNumber", defaultValue = "1")
    val stageNumber: Int = 1,
    @ColumnInfo(name = "isPlayed")
    val isPlayed: Boolean = false,
    @ColumnInfo(name = "playTime")
    val playTime: Long = 0L,
    @ColumnInfo(name = "pickedTime")
    val pickedTime: Long = 0L
){
    fun toGameCardDto() = GameCardDto(
        gameId = gameId,
        cardId = cardId,
        playerId = playerId,
        stageNumber = stageNumber,
        played = isPlayed,
        playTime = playTime,
        pickedTime = pickedTime
    )
}