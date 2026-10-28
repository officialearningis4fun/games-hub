package com.learningis4fun.gameshub.api.model

import kotlinx.serialization.Serializable

@Serializable
data class UserGameDto(
    val owner: Boolean = false,
    val online: Boolean = false
){
    constructor() : this(false, false)
}
