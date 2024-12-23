package com.learningis4fun.gameshub.commons.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.domain.util.GameState

// completedTime
@Entity(tableName = "game")
data class Game(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "gameId")
    val id: String = "",
    @ColumnInfo(name = "gameState")
    val gameState: String = GameState.NotStarted.name,
    @ColumnInfo(name = "gameName")
    val name: String = "",
    @ColumnInfo(name = "winnerId")
    val winnerId: String = "",
    @ColumnInfo(name = "gameDuration")
    val gameDuration: Long = 0L,
    @ColumnInfo(
        name = "playingDirection",
        defaultValue = Constants.CrusadePlayingDirection.CLOCKWISE
    )
    val playingDirection: String = Constants.CrusadePlayingDirection.CLOCKWISE,
    @ColumnInfo(name = "gameCompletedTime")
    val completedTime: Long = 0L,
    @ColumnInfo(name = "gameCompletedMessage")
    val completedGameMessage: String = ""
)
