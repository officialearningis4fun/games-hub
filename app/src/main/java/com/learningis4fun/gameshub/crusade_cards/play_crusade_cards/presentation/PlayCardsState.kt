package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.presentation

import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.GameCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.PlayerScreenInfo
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.UserCard

data class PlayCardsState(
    val playersScreenInfo: List<PlayerScreenInfo> = emptyList(),
    val playedCards: List<UserCard> = emptyList(),
    val dustbin: List<GameCard> = emptyList(),
    val userUnPlayedCards: List<UserCard> = emptyList(),
    val message: String = "",
    val allowOthersPlay: Boolean = false,
    val showCurrentCardType: Pair<Boolean, String> = Pair(false, ""),
    val showChooseCardTypeScreen: Boolean = false,
    val showDefineJokerActions: Boolean = false,
    val winner: Player? = null,
    val error: String? = null
) {

    private val imageOfCurrentCardType = when (showCurrentCardType.second) {
        Constants.CardTypes.DIAMOND -> {
            R.drawable.suit_diamond
        }

        Constants.CardTypes.HEART -> {
            R.drawable.suit_heart
        }

        Constants.CardTypes.SPADE -> {
            R.drawable.suit_spade
        }

        Constants.CardTypes.CLUBS -> {
            R.drawable.suit_club
        }

        else -> null

    }?.toString()

    val showCurrentCardTypeAndImage = Pair(showCurrentCardType.first, imageOfCurrentCardType)
}
