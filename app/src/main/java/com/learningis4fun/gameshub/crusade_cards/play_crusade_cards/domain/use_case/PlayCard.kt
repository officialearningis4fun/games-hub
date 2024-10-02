package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case

import com.learningis4fun.gameshub.crusade_cards.commons.domain.controller.MultiplayerCrusadeCardsController
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.CardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.UserCard
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStatesDefaultMsg
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
class PlayCard @ExperimentalStdlibApi constructor(
    private val multiplayerCrusadeCardsController: MultiplayerCrusadeCardsController,
    private val cardRepository: CardRepository,
    private val crusadeCardsPreferencesManager: CrusadeCardsPreferencesManager
) {

    suspend fun playCard(
        card: UserCard?,
        playerId: String,
        canPick: Boolean
    ): CardStates {
        card?.let {
            return multiplayerCrusadeCardsController.requestUserPlay(
                card = card,
                canPick = canPick
            )
        } ?: return CardStates.Play(
            playerId = playerId,
            canPick = canPick,
            message = CardStatesDefaultMsg.PlayError
        )
    }
}