package com.learningis4fun.gameshub.presentation.start.friends

import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.PlayerInfo

sealed interface FriendsStartEvents {
    data class DeleteGame(val gameId: String): FriendsStartEvents
    data object IncreaseNumberOfCards : FriendsStartEvents
    data object DecreaseNumberOfCards : FriendsStartEvents
    data class CheckParticipantClick(val playerInfo: PlayerInfo): FriendsStartEvents
    data object IncreaseEliminationLimitForCrusade : FriendsStartEvents
    data object DecreaseEliminationLimitForCrusade : FriendsStartEvents
    data object ClearSearchQuery: FriendsStartEvents
    data class UpdateSearchQuery(val query: String): FriendsStartEvents
    data class AddUserToFriends(val email: String): FriendsStartEvents
    data object CreateGame: FriendsStartEvents
}