package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model

data class CardStateEntity(
    val cardStateName : String = "",
    val playerId: String = "",
    val gameCardId : String = "",
    val canPick: Boolean = false,
    val canPlay: Boolean = false,
    val cardsPicked: Int = 0,
    val cardsToPick: Int = 0,
    val message: String = ""
)