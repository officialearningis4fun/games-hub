package com.learningis4fun.gameshub.api.model

import kotlinx.serialization.Serializable

@Serializable
data class RecentCardStateDto(
    val cardStateName : String = "",
    val playerId: String = "",
    val gameCardId : String = "",
    val canPick: Boolean = false,
    val canPlay: Boolean = false,
    val cardsPicked: Int = 0,
    val cardsToPick: Int = 0,
    val message: String = ""
){
    constructor() : this("")
}
