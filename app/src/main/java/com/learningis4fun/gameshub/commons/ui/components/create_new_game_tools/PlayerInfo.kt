package com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools

import com.learningis4fun.gameshub.commons.domain.model.Player

data class PlayerInfo(
    val player: Player,
    val canRemove: Boolean,
    val includeInList: Boolean
)
