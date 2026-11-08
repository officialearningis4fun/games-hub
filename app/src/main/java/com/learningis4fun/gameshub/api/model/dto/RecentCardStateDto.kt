package com.learningis4fun.gameshub.api.model.dto

import androidx.annotation.Keep
import com.learningis4fun.gameshub.commons.domain.util.CardStates
import com.learningis4fun.gameshub.domain.model.CardStateEntity
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class RecentCardStateDto(
    val cardStateName : String = CardStates.Start.name,
    val playerId: String = "",
    val gameCardId : String = "",
    val canPick: Boolean = false,
    val canPlay: Boolean = false,
    val cardsPicked: Int = 0,
    val cardsToPick: Int = 0,
    val message: String = ""
){
    constructor() : this("")

    fun toCardStateEntity() = CardStateEntity(
        cardStateName = cardStateName,
        playerId = playerId,
        gameCardId = gameCardId,
        canPick = canPick,
        canPlay = canPlay,
        cardsPicked = cardsPicked,
        cardsToPick = cardsToPick,
        message = message
    )
}
