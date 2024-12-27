package com.learningis4fun.gameshub.presentation.game_winner

import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.commons.domain.model.Card
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.domain.model.PlayerScreenInfo
import com.learningis4fun.gameshub.domain.model.UserCard
import com.learningis4fun.gameshub.presentation.start.OngoingGamesOrNewGameState

sealed interface PlayedGameWinnerOrNextStage {
    data object Loading : PlayedGameWinnerOrNextStage

    data class PlayedGameNextStageState(
        val loading: Boolean = true,
        val gameDuration: String = "",
        val winnerImageUrl: String = "",
        val eliminatedPlayers: List<Player> = emptyList(),
        val nextStagePlayers: List<Player> = emptyList(),
        val unPlayedCards: List<UserCard> = emptyList(),
        val winnerNames: String = "",
        val error: String? = null
    ) : PlayedGameWinnerOrNextStage {

        fun unPlayedCardsOfPlayer(playerId: String): List<Card> {
            return unPlayedCards.filter { it.playerId == playerId }
                .map { it.toCard() }
        }

        fun eliminatedPlayersScreenInfo(): List<PlayerScreenInfo> =
            eliminatedPlayers.map { player ->
                PlayerScreenInfo(
                    playerId = player.uniqueId,
                    playerName = player.profileName,
                    imageUrl = player.imageUrl,
                    defaultAvatar = R.drawable.avatar_man1,
                    numOfCardsLeft = unPlayedCardsOfPlayer(playerId = player.uniqueId).size.toString()
                )
            }

        fun nextStagePlayersScreenInfo(): List<PlayerScreenInfo> = nextStagePlayers.map { player ->
            val numOfLeftCards = unPlayedCardsOfPlayer(playerId = player.uniqueId).size
            player.toPlayerScreenInfo().copy(
                numOfCardsLeft = if (numOfLeftCards == 0) "" else numOfLeftCards.toString()
            )
        }

        fun isPlayerHaveCards(playerId: String, hasCards: () -> Unit = {}) {
            if (unPlayedCards.count { it.playerId == playerId } > 0) {
                hasCards()
            }
        }

    }

    data class PlayedGameWinnerState(
        val loading: Boolean = true,
        val gameDuration: String = "",
        val winnerImageUrl: String = "",
        val otherPlayers: List<Player> = emptyList(),
        val unPlayedCards: List<UserCard> = emptyList(),
        val numberOfCardsPerPlayer: Int = Constants.NumberOfCardsOptions.DEFAULT_CARDS_PER_PLAYER,
        val winnerNames: String = "",
        val error: String? = null,
    ) : PlayedGameWinnerOrNextStage {

        fun unPlayedCardsOfPlayer(playerId: String): List<Card> {
            return unPlayedCards.filter { it.playerId == playerId }
                .map { it.toCard() }
        }

        fun otherPlayersScreenInfo(): List<PlayerScreenInfo> = otherPlayers.map { player ->
            val numOfLeftCards = unPlayedCardsOfPlayer(playerId = player.uniqueId).size
            player.toPlayerScreenInfo().copy(
                numOfCardsLeft = if (numOfLeftCards == 0) "" else numOfLeftCards.toString()
            )
        }

        fun isPlayerHaveCards(playerId: String, hasCards: () -> Unit = {}) {
            if (unPlayedCards.count { it.playerId == playerId } > 0) {
                hasCards()
            }
        }
    }
}

