package com.learningis4fun.gameshub.domain.model

data class OnGoingGame(
    val players: String,
    val timeElapsed: String,
    val lastPlayedCardsUrls: List<String>
)
