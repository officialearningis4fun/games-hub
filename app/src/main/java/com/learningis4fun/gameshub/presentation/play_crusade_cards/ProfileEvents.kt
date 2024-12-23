package com.learningis4fun.gameshub.presentation.play_crusade_cards

sealed interface ProfileEvents {
    data class UpdateUserName(val username: String) : ProfileEvents
    data class UpdateUserAvatar(val avatar: Int) : ProfileEvents
}