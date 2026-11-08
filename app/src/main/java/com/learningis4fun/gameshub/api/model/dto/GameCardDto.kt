package com.learningis4fun.gameshub.api.model.dto

import androidx.annotation.Keep
import com.learningis4fun.gameshub.domain.model.GameCard
import com.learningis4fun.gameshub.domain.model.UserCard
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class GameCardDto(
    val playerId: String = "",
    val cardId: String = "",
    val name: String = "",
    val type: String = "",
    val gameId: String = "",
    val stageNumber: Int = 1,
    val played: Boolean = false,
    val image: String = "",
    val playTime: Long = 0L,
    val pickedTime: Long = 0L
){
    constructor () : this("")
    fun toUserCard(gameCardId: String) = UserCard(
        gameCardId = gameCardId,
        playerId = playerId,
        cardId = cardId,
        name = name.lowercase(),
        type = type.lowercase(),
        gameId = gameId,
        stageNumber = stageNumber,
        isPlayed = played,
        image = image,
        playTime = playTime,
        pickedTime = pickedTime
    )

    fun toGameCard(gameCardId: String) = GameCard(
        id = gameCardId,
        gameId = gameId,
        cardId = cardId,
        playerId = playerId,
        stageNumber = stageNumber,
        isPlayed = played,
        playTime = playTime,
        pickedTime = pickedTime
    )

}
