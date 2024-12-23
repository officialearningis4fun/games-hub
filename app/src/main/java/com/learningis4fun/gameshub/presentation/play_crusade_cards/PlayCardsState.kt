package com.learningis4fun.gameshub.presentation.play_crusade_cards

import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.domain.model.GameCard
import com.learningis4fun.gameshub.domain.model.PlayerScreenInfo
import com.learningis4fun.gameshub.domain.model.UserCard

data class PlayCardsState(
    val playersScreenInfo: List<PlayerScreenInfo> = emptyList(),
    val playedCards: List<UserCard> = emptyList(),
    val animatingCard: UserCard? = null,
    val dustbin: List<GameCard> = emptyList(),
    val userUnPlayedCards: Pair<List<UserCard>, UserCard?> = Pair(emptyList(), null),
    val message: String = "",
    val allowOthersPlay: Boolean = false,
    val showCurrentCardType: Pair<Boolean, String> = Pair(false, ""),
    val showChooseCardTypeScreen: Boolean = false,
    val showDefineJokerActions: Boolean = false,
    val wonGame: WonGame = WonGame(),
    val error: String? = null,
    val playCardsErrorState: PlayCardsErrorState? = null,
    val playingDirection: String = "",
    val warning: String? = null,
    val isLoading: Boolean = true
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

data class WonGame(
    val winner: Player = Player(),
    val gameId: String = ""
)

data class PlayCardsErrorState(
    val errorMessage: String,
    val dismissible: Boolean,
    val errorButtonText: String = "",
    val errorAction: PlayCardsErrorAction = PlayCardsErrorAction.Nothing
)

sealed interface PlayCardsErrorAction {
    data object Nothing : PlayCardsErrorAction
    data object ResetGame : PlayCardsErrorAction
    data object RetryCardState : PlayCardsErrorAction
    data object RetryResumeGame : PlayCardsErrorAction
}
