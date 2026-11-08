package com.learningis4fun.gameshub.api.model.dto

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class UserDto(
    val name : String? = "",
    val email: String = "",
    val photoUrl: String? = ""
){
    constructor() : this("", "", "")
}
