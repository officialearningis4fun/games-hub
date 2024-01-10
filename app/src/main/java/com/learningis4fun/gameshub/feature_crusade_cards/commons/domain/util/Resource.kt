package com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Loading<T>(data: T? = null, message: String? = null) : Resource<T>(data = data, message = message)
    class Success<T>(data: T, message: String? = null) : Resource<T>(data, message)
    class Error<T>(data: T? = null, message: String) : Resource<T>(data, message)
}
