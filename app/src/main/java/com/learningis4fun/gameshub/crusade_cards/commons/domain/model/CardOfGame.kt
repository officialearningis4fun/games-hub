package com.learningis4fun.gameshub.crusade_cards.commons.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CardOfGame(
    val id: String = "",
    val gameId: String = "",
    val playerId: String = "",
    val card: Card = Card(),
    val isPlayed: Boolean = false,
    val playTime: Long = 0L,
    val pickedTime: Long = 0L
)
