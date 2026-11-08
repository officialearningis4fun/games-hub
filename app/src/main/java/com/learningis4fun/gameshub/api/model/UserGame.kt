package com.learningis4fun.gameshub.api.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class UserGame(
    val gameId: String,
    val owner: Boolean = false,
    val online: Boolean = false,
    val completed: Boolean = false
)
