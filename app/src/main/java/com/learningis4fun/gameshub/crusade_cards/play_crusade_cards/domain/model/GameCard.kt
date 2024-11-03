package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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
    @ColumnInfo(name = "isPlayed")
    val isPlayed: Boolean = false,
    @ColumnInfo(name = "playTime")
    val playTime: Long = 0L,
    @ColumnInfo(name = "pickedTime")
    val pickedTime: Long = 0L
)