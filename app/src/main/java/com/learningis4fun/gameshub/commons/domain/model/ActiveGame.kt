package com.learningis4fun.gameshub.commons.domain.model

import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.domain.util.GameState
import com.learningis4fun.gameshub.domain.model.CardStateEntity
import kotlinx.serialization.Serializable

@Serializable
data class ActiveGame(
    val gameState: String = GameState.NotStarted.name,
    val recentCardState: CardStateEntity,
    val currentCardType: String = "",
    val currentCardName: String = "",
    val name: String = "",
    val winnerId: String = "",
    val gameDuration: Long = 0L,
    val playingDirection: String = Constants.CrusadePlayingDirection.CLOCKWISE,
    val completedTime: Long = 0L,
    val completedGameMessage: String = "",
    val players: List<Player> = emptyList(),
    val gameStages: List<GameStage> = emptyList()
){
    fun toGame(gameId: String) = Game(
        id = gameId,
        gameState = gameState,
        name = name,
        winnerId = winnerId,
        gameDuration = gameDuration,
        playingDirection = playingDirection,
        completedTime = completedTime,
        completedGameMessage = completedGameMessage
    )
}
