package com.learningis4fun.gameshub.data.data_source.remote.dto

import androidx.annotation.Keep
import com.learningis4fun.gameshub.commons.domain.model.Card
import com.learningis4fun.gameshub.commons.domain.model.CardOfGame

@Keep
data class CardOfGameDto(
    val id: String = "",
    val gameId: String = "",
    val playerId: String = "",
    val card: CardDto = CardDto(),
    val isPlayed: Boolean = false,
    val playTime: Long = 0L,
    val pickedTime: Long = 0L
) {
    fun toCard() = Card(card.id, card.name, card.type, card.image)
    fun toCardOfGame() = CardOfGame(id, gameId, playerId, toCard(), isPlayed, playTime, pickedTime)
}
