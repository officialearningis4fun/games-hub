package com.learningis4fun.gameshub.presentation.start.friends

import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.PlayerInfo

data class CreateOnlineGameState(
    val numberOfCardsPerPlayer: Int = Constants.NumberOfCardsOptions.DEFAULT_CARDS_PER_PLAYER,
    val numberOfParticipants: Int = Constants.DEFAULT_PARTICIPANTS_IN_CRUSADE_GAME,
    val eliminationLimitForCrusade: Int = Constants.DEFAULT_CRUSADE_ELIMINATION_LIMIT,
    val createGameLoading: Boolean = false,
    val enableCreateGameButton: Boolean = true,
    val playerInfos: List<PlayerInfo> = emptyList(),
    val userInfo: PlayerInfo? = null,
    val error: String? = null
) {
    val availablePlayers = playerInfos
        .filter { it.includeInList }

    fun updateNumberOfParticipants(): CreateOnlineGameState {
        return CreateOnlineGameState(
            numberOfCardsPerPlayer = numberOfCardsPerPlayer,
            numberOfParticipants = availablePlayers.size,
            eliminationLimitForCrusade = eliminationLimitForCrusade,
            createGameLoading = createGameLoading,
            enableCreateGameButton = enableCreateGameButton,
            playerInfos = playerInfos,
            userInfo = userInfo
        )
    }
}
