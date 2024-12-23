package com.learningis4fun.gameshub.data.data_source.remote.dto

import androidx.annotation.Keep
import com.learningis4fun.gameshub.commons.domain.model.Card

@Keep
data class CardDto(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val image: String = ""
) {
    fun toCard() = Card(id, name, type, image)
}
