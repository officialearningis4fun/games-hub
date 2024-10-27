package com.learningis4fun.gameshub.crusade_cards.feature_game_winner.presentation

import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants

data class PlayedGameWinnerState(
    val loading: Boolean = true,
    val gameDuration: String = "",
    val winnerImageUrl: String = "",
    val otherPlayers: List<Player> = emptyList(),
    val numberOfCardsPerPlayer: Int = Constants.NumberOfCardsOptions.DEFAULT_CARDS_PER_PLAYER,
    val winnerNames: String = "",
    val error: String? = null
)
