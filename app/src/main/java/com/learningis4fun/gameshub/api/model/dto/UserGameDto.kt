package com.learningis4fun.gameshub.api.model.dto

import com.learningis4fun.gameshub.api.model.UserGame
import kotlinx.serialization.Serializable

@Serializable
data class UserGameDto(
    val owner: Boolean = false,
    val online: Boolean = false,
    val completed: Boolean = false
){
    constructor() : this(false, false, false)

    fun toUserGame(gameId: String) = UserGame(
        gameId = gameId,
        owner = owner,
        online = online,
        completed = completed
    )
}
