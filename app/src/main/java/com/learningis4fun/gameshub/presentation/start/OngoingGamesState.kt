package com.learningis4fun.gameshub.presentation.start

import com.learningis4fun.gameshub.commons.domain.model.Card
import com.learningis4fun.gameshub.commons.domain.model.Game
import com.learningis4fun.gameshub.data.data_source.relations.GameWithPlayers

data class OngoingGamesState(
    val games: List<OnGoingGame> = emptyList(),
    val isLoading: Boolean = true
)

data class OnGoingGame(
    val threeCards: List<Card> = emptyList(),
    val gameWithPlayers: GameWithPlayers = GameWithPlayers(
        game = Game(),
        players = emptyList()
    )
)
