package com.learningis4fun.gameshub.commons.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "gameUserCrossRef", primaryKeys = ["gameId", "uniqueId"],
    indices = [Index("uniqueId")]
)
data class GameUserCrossRef(
    @ColumnInfo(name = "gameId")
    val gameId: String,
    @ColumnInfo(name = "uniqueId")
    val userId: String
)
