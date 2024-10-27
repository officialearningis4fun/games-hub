package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.presentation

sealed interface ProfileEvents {
    data class UpdateUserName(val username: String) : ProfileEvents
    data class UpdateUserAvatar(val avatar: Int) : ProfileEvents
}