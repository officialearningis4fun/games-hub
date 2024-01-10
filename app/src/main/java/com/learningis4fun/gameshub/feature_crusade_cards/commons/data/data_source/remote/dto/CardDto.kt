package com.learningis4fun.gameshub.feature_crusade_cards.commons.data.data_source.remote.dto

import androidx.annotation.Keep
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Card

@Keep data class CardDto(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val image: String = ""
){
    fun toCard() = Card(id, name, type, image)
}
