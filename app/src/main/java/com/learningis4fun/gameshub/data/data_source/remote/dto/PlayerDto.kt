package com.learningis4fun.gameshub.data.data_source.remote.dto

import androidx.annotation.Keep
import com.learningis4fun.gameshub.commons.domain.model.Player

@Keep
data class PlayerDto(
    val uniqueId: String = "",
    val email: String = "",
    val phone: String = "",
    val imageUrl: String = "",
    val profileName: String = ""
) {

    fun toPlayer() = Player(uniqueId, email, phone, imageUrl, profileName)

}
