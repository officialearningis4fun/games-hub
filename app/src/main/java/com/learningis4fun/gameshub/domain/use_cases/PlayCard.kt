package com.learningis4fun.gameshub.domain.use_cases

import com.learningis4fun.gameshub.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.commons.domain.controller.MultiplayerCrusadeCardsController
import com.learningis4fun.gameshub.commons.domain.repository.CardRepository
import com.learningis4fun.gameshub.commons.domain.util.CardStates
import com.learningis4fun.gameshub.commons.domain.util.CardStatesDefaultMsg
import com.learningis4fun.gameshub.domain.model.UserCard
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