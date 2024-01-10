package com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "card")
data class Card(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "cardId")
    val id: String = "",
    @ColumnInfo(name = "cardName")
    val name: String = "",
    @ColumnInfo(name = "cardType")
    val type: String = "",
    @ColumnInfo(name = "cardImage")
    val image: String = ""
)
