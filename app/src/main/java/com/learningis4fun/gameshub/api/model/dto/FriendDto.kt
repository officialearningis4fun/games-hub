package com.learningis4fun.gameshub.api.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class FriendDto(
    val userId: String,
    val request: RequestDto = RequestDto()
){
    constructor() : this("")
}

@Serializable
data class RequestDto(
    val accepted: Boolean = false,
    val send: Boolean = false
){

    constructor() : this(false, false)

}