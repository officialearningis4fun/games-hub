package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util

import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response

fun <T> Response<T>.getDataOrThrowException(): T {
    return when (this) {
        is Response.Error -> throw Exception(this.message)
        is Response.Success -> if(this.data == null) {
            throw Exception("Data not found")
        } else data
    }
}