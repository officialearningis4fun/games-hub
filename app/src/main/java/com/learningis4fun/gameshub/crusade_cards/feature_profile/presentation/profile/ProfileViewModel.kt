package com.learningis4fun.gameshub.crusade_cards.feature_profile.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util.getDataOrThrowException
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.presentation.ProfileEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val userProfileFlow = MutableStateFlow(Player())

    private val profileStateFlow = MutableStateFlow(ProfileState())
    val profileState = profileStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.getPlayerSignInDetails().collectLatest { player ->
                println("Logged user: $player")
                userProfileFlow.update { player }
                profileStateFlow.update {
                    it.copy(user = player)
                }
            }
        }

    }

    fun onEvent(event: ProfileEvents) {
        when (event) {

            is ProfileEvents.UpdateUserAvatar -> {
                profileStateFlow.update {
                    it.copy(
                        user = it.user.copy(
                            imageUrl = event.avatar.toString()
                        )
                    ).enableSaveButton(profileStateFlow.value.user)
                }
            }

            is ProfileEvents.UpdateUserName -> {
                profileStateFlow.update {
                    it.copy(
                        user = it.user.copy(
                            profileName = event.username,
                            email = "${event.username}@gameshub.com"
                        )
                    ).enableSaveButton(profileStateFlow.value.user)
                }
            }
        }
    }

    fun onSaveProfile(success: () -> Unit) {
        profileStateFlow.update { it.copy(isEnableSaveButton = false) }
        viewModelScope.launch {
            try {
                userRepository.updatePlayerLogin(profileStateFlow.value.user).getDataOrThrowException()
                success()
            } catch (e: Exception) {
                e.printStackTrace()
                profileStateFlow.update { it.copy(error = e.message) }
            }
        }
    }
}