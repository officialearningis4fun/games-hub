package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model

import androidx.annotation.DrawableRes

data class PlayerScreenInfo(
    val playerId: String,
    val playerName: String,
    val imageUrl: String? = null,
    @DrawableRes val defaultAvatar: Int,
    val numOfCardsLeft: String,
    val isCurrentlyPlaying: Boolean = false
)
