package com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.presentation

import androidx.lifecycle.ViewModel
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.use_case.PlayCardsUseCases

class PlayCardsViewModel (
    playCardsUseCases: PlayCardsUseCases
        ): ViewModel(){


    /**
     * GameDuration - from server
     * Available cards - intercepted from server
     * Players - intercepted from server
     * gameUserActions - screen states - intercepted from server
     */

    val gameDuration = playCardsUseCases.getGameDuration()
    val cardsOfUser = playCardsUseCases.getUserCards()
    val gamePlayerActions = playCardsUseCases.getGamePlayerAction()
    val players = playCardsUseCases.getPlayersScreen()

}