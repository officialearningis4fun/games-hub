package com.learningis4fun.gameshub.crusade_cards.feature_start

sealed interface CardsStartEvents {
    data class DeleteGame(val gameId: String): CardsStartEvents
}