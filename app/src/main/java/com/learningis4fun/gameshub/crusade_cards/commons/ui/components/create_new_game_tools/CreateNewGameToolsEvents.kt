package com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools

import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player

sealed interface CreateNewGameToolsEvents {
    data object IncreaseNumberOfCards : CreateNewGameToolsEvents
    data object DecreaseNumberOfCards : CreateNewGameToolsEvents
    data class RemoveParticipant(val player: Player) : CreateNewGameToolsEvents
    data object AddParticipant : CreateNewGameToolsEvents
    data object IncreaseEliminationLimitForCrusade : CreateNewGameToolsEvents
    data object DecreaseEliminationLimitForCrusade : CreateNewGameToolsEvents
}