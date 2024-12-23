package com.learningis4fun.gameshub.commons.domain.model

sealed class AuthStates {
    data class NotSignedUp(val message: String = "Sign in"): AuthStates()
    data class NotSignedIn(val message: String = ""): AuthStates()
    data class SignedIn(val player: Player = Player()): AuthStates()
}