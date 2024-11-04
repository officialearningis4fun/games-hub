package com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools

sealed interface CreateNewGameToolsEvents {
    data object IncreaseNumberOfCards : CreateNewGameToolsEvents
    data object DecreaseNumberOfCards : CreateNewGameToolsEvents
    data object IncreaseNumberOfParticipants : CreateNewGameToolsEvents
    data object DecreaseNumberOfParticipants : CreateNewGameToolsEvents
    data object IncreaseEliminationLimitForCrusade : CreateNewGameToolsEvents
    data object DecreaseEliminationLimitForCrusade : CreateNewGameToolsEvents
}