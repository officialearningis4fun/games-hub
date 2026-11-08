package com.learningis4fun.gameshub.api.model.dto

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class FriendDto(
    val userId: String,
    val request: RequestDto = RequestDto()
){
    constructor() : this("")
}

@Serializable
@Keep
data class RequestDto(
    val accepted: Boolean = false,
    val send: Boolean = false
){

    constructor() : this(false, false)

}