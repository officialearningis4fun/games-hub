package com.learningis4fun.gameshub.crusade_cards.feature_start.domain.model

data class OnGoingGame(
    val players: String,
    val timeElapsed: String,
    val lastPlayedCardsUrls: List<String>
)
