package com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools

import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants

data class CreateNewGameToolsState(
    val numberOfCardsPerPlayer: Int = Constants.NumberOfCardsOptions.DEFAULT_CARDS_PER_PLAYER,
    val numberOfParticipants: Int = Constants.DEFAULT_PARTICIPANTS_IN_CRUSADE_GAME,
    val eliminationLimitForCrusade: Int = Constants.DEFAULT_CRUSADE_ELIMINATION_LIMIT,
    val playersInfo: List<PlayerInfo> = emptyList(),
    val createGameLoading: Boolean = false
) {

    val availablePlayers = playersInfo
        .filter { it.includeInList }

    fun updateNumberOfParticipants(): CreateNewGameToolsState {
        return CreateNewGameToolsState(
            numberOfCardsPerPlayer = numberOfCardsPerPlayer,
            numberOfParticipants = availablePlayers.size,
            eliminationLimitForCrusade = eliminationLimitForCrusade,
            playersInfo = playersInfo
        )
    }
}
