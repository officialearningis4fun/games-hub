package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model

import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card

data class UserCard(
    @ColumnInfo(name = "gameCardId")
    val gameCardId: String = "",

    @ColumnInfo(name = "playerId")
    val playerId: String = "",

    @ColumnInfo(name = "cardId")
    val cardId: String = "",

    @ColumnInfo(name = "cardName")
    val name: String = "",

    @ColumnInfo(name = "cardType")
    val type: String = "",

    @ColumnInfo(name = "gameId")
    val gameId: String = "",

    @ColumnInfo(name = "isPlayed")
    val isPlayed: Boolean = false,

    @ColumnInfo(name = "cardImage")
    val image: String = "",

    @ColumnInfo(name = "playTime")
    val playTime : Long = 0L,

    @ColumnInfo(name = "pickedTime")
    val pickedTime : Long = 0L
){
    @Ignore
    val card = "${name.uppercase()} - ${type.uppercase()}"
    fun toCard(): Card = Card(
        id = cardId,
        name = name,
        type = type,
        image = image
    )
}
