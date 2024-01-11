package com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.domain.use_case

import com.learningis4fun.gameshub.crusade_cards.commons.domain.controller.MultiplayerCrusadeCardsController
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@ExperimentalStdlibApi
class PickCards @Inject constructor(
    private val multiplayerCrusadeCardsController: MultiplayerCrusadeCardsController
) {

    suspend fun pickCards(playerId: String, cardsToPick: Int, canPlay: Boolean): CardStates {
        return multiplayerCrusadeCardsController.requestPlayerPick(
            playerId = playerId,
            cardsToPick = cardsToPick,
            canPlay = canPlay
        )
    }
}