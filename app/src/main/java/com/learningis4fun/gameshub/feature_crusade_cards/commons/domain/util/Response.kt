package com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util

sealed class Response<T>(val data:T, val message:String) {
    class Success<T>(data: T, message: String) : Response<T>(data, message)
    class Error<T>(data: T, message: String): Response<T>(data, message)
}