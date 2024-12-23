package com.learningis4fun.gameshub.commons.domain.model

import com.learningis4fun.gameshub.commons.domain.util.GameState
import kotlinx.serialization.Serializable

@Serializable
data class ActiveGame(
    val id: String = "",
    val gameState: String = GameState.NotStarted.name,
    val name: String = "",
    val gameDuration: Long = 0L,
    val cardStateJson: String = "",
    val recentCardType: String = "",
    val recentCardName: String = ""
)
