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

    @ColumnInfo(name = "stageNumber", defaultValue = "1")
    val stageNumber: Int = 1,

    @ColumnInfo(name = "isPlayed")
    val isPlayed: Boolean = false,

    @ColumnInfo(name = "cardImage")
    val image: String = "",

    @ColumnInfo(name = "playTime")
    val playTime: Long = 0L,

    @ColumnInfo(name = "pickedTime")
    val pickedTime: Long = 0L
) {
    @Ignore
    val card = "${name.uppercase()} - ${type.uppercase()}"
    fun toCard(): Card = Card(
        id = cardId,
        name = name,
        type = type,
        image = image
    )

    fun toGameCard() = GameCard(
        id = gameCardId,
        gameId = gameId,
        cardId = cardId,
        playerId = playerId,
        stageNumber = stageNumber,
        isPlayed = isPlayed,
        playTime = playTime,
        pickedTime = pickedTime
    )
}
