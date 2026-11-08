package com.learningis4fun.gameshub.presentation.create_game.online

import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.PlayerInfo

sealed interface CreateOnlineGameEvents {
    data object IncreaseNumberOfCards : CreateOnlineGameEvents
    data object DecreaseNumberOfCards : CreateOnlineGameEvents
    data class CheckParticipantClick(val playerInfo: PlayerInfo): CreateOnlineGameEvents
    data object IncreaseEliminationLimitForCrusade : CreateOnlineGameEvents
    data object DecreaseEliminationLimitForCrusade : CreateOnlineGameEvents
    data object ClearSearchQuery: CreateOnlineGameEvents
    data class UpdateSearchQuery(val query: String): CreateOnlineGameEvents
    data class AddUserToFriends(val email: String): CreateOnlineGameEvents
    data object CreateGame: CreateOnlineGameEvents
}