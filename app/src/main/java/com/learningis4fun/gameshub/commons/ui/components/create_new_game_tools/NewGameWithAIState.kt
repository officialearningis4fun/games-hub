package com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools

import com.learningis4fun.gameshub.commons.domain.model.Player

data class NewGameWithAIState(
    val players: List<Player> = emptyList(),
    val requiredUseOfInternet: Boolean = true,
    val enableCreateGameButton: Boolean = false,
)
