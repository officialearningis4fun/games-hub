package com.learningis4fun.gameshub.crusade_cards.commons.domain.util

sealed class Response<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Response<T>(data)
    class Error<T>(message: String) : Response<T>(null, message)
}