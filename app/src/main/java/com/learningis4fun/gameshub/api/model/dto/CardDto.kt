package com.learningis4fun.gameshub.api.model.dto

import androidx.annotation.Keep
import com.learningis4fun.gameshub.commons.domain.model.Card
import com.learningis4fun.gameshub.presentation.util.removeSAtEnd
import kotlinx.serialization.Serializable

@Serializable
@Keep
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
        type = type.lowercase().removeSAtEnd(),
        image = image
    )
}
