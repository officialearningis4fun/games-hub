package com.learningis4fun.gameshub.api.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val name : String? = "",
    val email: String = "",
    val photoUrl: String? = ""
){
    constructor() : this("", "", "")
}
