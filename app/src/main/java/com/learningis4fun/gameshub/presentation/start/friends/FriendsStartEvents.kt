package com.learningis4fun.gameshub.presentation.start.friends

sealed interface FriendsStartEvents {
    data class DeleteGame(val gameId: String): FriendsStartEvents
}