package com.learningis4fun.gameshub.presentation.start

sealed interface CardsStartEvents {
    data class DeleteGame(val gameId: String): CardsStartEvents
}