package com.learningis4fun.gameshub.api.model

import kotlinx.serialization.Serializable

@Serializable
data class UserGame(
    val gameId: String,
    val owner: Boolean = false,
    val online: Boolean = false,
    val completed: Boolean = false
)
