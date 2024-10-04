package com.learningis4fun.gameshub.crusade_cards.feature_profile.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val profileScreenEventsChannel = Channel<ProfileScreenEvents>()
    val profileScreenEvents = profileScreenEventsChannel.receiveAsFlow()
    private val userProfileFlow = MutableStateFlow(Player())
    val userProfileInfo = userProfileFlow.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.authStateListener().collect()
        }

        viewModelScope.launch {
            userRepository.getPlayerSignInDetails().collectLatest { player ->
                println("Logged user: $player")
                userProfileFlow.update { player }
            }
        }
    }

    fun onBackNavigationClick() = viewModelScope.launch {
        profileScreenEventsChannel.send(ProfileScreenEvents.PopBackStack)
    }

    fun onEditProfileNameBtnClick() = viewModelScope.launch {
        profileScreenEventsChannel.send(ProfileScreenEvents.NavigateToSignUpScreen)
    }

    fun signOut() {
        userRepository.signUserOut()
    }

    sealed class ProfileScreenEvents {
        data object PopBackStack : ProfileScreenEvents()
        data object NavigateToSignUpScreen : ProfileScreenEvents()
    }
}