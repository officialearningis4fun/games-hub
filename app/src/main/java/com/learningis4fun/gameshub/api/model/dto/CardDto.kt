package com.learningis4fun.gameshub.api.model.dto

import com.learningis4fun.gameshub.commons.domain.model.Card
import kotlinx.serialization.Serializable

@Serializable
data class CardDto(
val id: String = "",
val name: String = "",
val type: String = "",
val image: String = ""
){
    constructor() : this("", "", "", "")

    fun toCard() = Card(
        id = id,
        name = name.lowercase(),
        type = type.lowercase(),
        image = image
    )
}
