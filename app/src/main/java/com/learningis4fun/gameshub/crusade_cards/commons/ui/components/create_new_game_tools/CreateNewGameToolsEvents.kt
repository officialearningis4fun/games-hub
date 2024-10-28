package com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools

import com.learningis4fun.gameshub.crusade_cards.feature_start.CardsStartEvents

sealed interface CreateNewGameToolsEvents {
    data object IncreaseNumberOfCards : CreateNewGameToolsEvents
    data object DecreaseNumberOfCards : CreateNewGameToolsEvents
    data object IncreaseNumberOfParticipants : CreateNewGameToolsEvents
    data object DecreaseNumberOfParticipants : CreateNewGameToolsEvents
}