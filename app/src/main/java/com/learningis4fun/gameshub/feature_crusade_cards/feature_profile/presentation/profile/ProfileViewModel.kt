package com.learningis4fun.gameshub.feature_crusade_cards.feature_profile.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.DefaultCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: DefaultCardRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val profileScreenEventsChannel = Channel<ProfileScreenEvents>()
    val profileScreenEvents = profileScreenEventsChannel.receiveAsFlow()

    fun userProfileInfo(): LiveData<Player> = userRepository.getPlayerSignInDetails().asLiveData()

    init {
        viewModelScope.launch {
            userRepository.authStateListener().collect()
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
        object PopBackStack : ProfileScreenEvents()
        object NavigateToSignUpScreen : ProfileScreenEvents()
    }
}