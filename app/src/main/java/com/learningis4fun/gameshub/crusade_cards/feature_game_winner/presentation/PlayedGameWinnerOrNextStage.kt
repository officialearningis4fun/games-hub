package com.learningis4fun.gameshub.crusade_cards.feature_game_winner.presentation

import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants

sealed interface PlayedGameWinnerOrNextStage {
    data object Loading : PlayedGameWinnerOrNextStage

    data class PlayedGameNextStageState(
        val loading: Boolean = true,
        val gameDuration: String = "",
        val winnerImageUrl: String = "",
        val eliminatedPlayers: List<Player> = emptyList(),
        val nextStagePlayers: List<Player> = emptyList(),
        val winnerNames: String = "",
        val error: String? = null
    ) : PlayedGameWinnerOrNextStage

    data class PlayedGameWinnerState(
        val loading: Boolean = true,
        val gameDuration: String = "",
        val winnerImageUrl: String = "",
        val otherPlayers: List<Player> = emptyList(),
        val numberOfCardsPerPlayer: Int = Constants.NumberOfCardsOptions.DEFAULT_CARDS_PER_PLAYER,
        val winnerNames: String = "",
        val error: String? = null
    ) : PlayedGameWinnerOrNextStage
}