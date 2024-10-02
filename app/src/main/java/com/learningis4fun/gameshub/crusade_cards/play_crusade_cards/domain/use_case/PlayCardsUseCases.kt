package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case

data class PlayCardsUseCases(
    val getGameDuration: GetGameDuration,
    val getUserCards: GetUserCards,
    val getGamePlayerAction: GetGamePlayerAction,
    val getPlayersScreen: GetPlayersScreen
)
