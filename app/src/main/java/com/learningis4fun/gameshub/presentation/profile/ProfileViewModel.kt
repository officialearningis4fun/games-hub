package com.learningis4fun.gameshub.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.model.User
import com.learningis4fun.gameshub.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.commons.domain.util.isProfileNameCountLimitReached
import com.learningis4fun.gameshub.data.data_source.remote.GoogleAuthStages
import com.learningis4fun.gameshub.data.data_source.remote.GoogleAuthUiClient
import com.learningis4fun.gameshub.presentation.play_crusade_cards.ProfileEvents
import com.learningis4fun.gameshub.util.getDataOrThrowException
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
    private val userRepository: UserRepository,
    private val googleAuthUiClient: GoogleAuthUiClient
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
        viewModelScope.launch {
            googleAuthUiClient.signedInUser.collectLatest { user ->
                onInit(
                    signedInUser = user
                )
            }
        }
        viewModelScope.launch {
            googleAuthUiClient.googleAuthStages.collectLatest { stage ->
                when (stage) {
                    GoogleAuthStages.Complete,
                    GoogleAuthStages.NotStarted -> {
                        profileStateFlow.value = profileStateFlow.value.copy(
                            isLoading = false
                        )
                    }

                    GoogleAuthStages.Started -> {
                        profileStateFlow.value = profileStateFlow.value.copy(
                            isLoading = true
                        )
                    }
                }
            }
        }

    }

    fun onInit(
        signedInUser: User? = null
    ) {
        viewModelScope.launch {

            val isSignedIn = signedInUser != null
            val email = signedInUser?.email

            val signedIn = if (email.isNullOrEmpty()) {
                if (isSignedIn) {
                    SignedIn.Anonymously
                } else {
                    SignedIn.NotSignedIn
                }
            } else {
                SignedIn.WithEmail(email = email)
            }
            profileStateFlow.value = profileStateFlow.value.copy(
                signedIn = signedIn,
                isLoading = false,
                signInError = null
            )
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

                    if (event.username.isProfileNameCountLimitReached()) {
                        it.copy(
                            isProfileNameCountLimitReached = true
                        )
                    } else {
                        it.copy(
                            user = it.user.copy(
                                profileName = event.username,
                                email = "${event.username}@gameshub.com"
                            ),
                            isProfileNameCountLimitReached = false
                        ).enableSaveButton(profileStateFlow.value.user)
                    }
                }
            }
        }
    }

    fun onSignInButtonClick(
        onSignIn: () -> Unit
    ) {
        when (profileStateFlow.value.signedIn) {
            SignedIn.Anonymously -> onSignIn()
            SignedIn.NotSignedIn -> onSignIn()
            is SignedIn.WithEmail -> {
                viewModelScope.launch {
                    profileStateFlow.value = profileStateFlow.value.copy(
                        isLoading = true
                    )
                    googleAuthUiClient.signOut()
                    profileStateFlow.value = profileStateFlow.value.copy(
                        signedIn = SignedIn.Anonymously,
                        isLoading = false,
                        signInError = null
                    )
                }
            }
        }
    }

    fun onSaveProfile(success: () -> Unit) {
        profileStateFlow.update { it.copy(isEnableSaveButton = false) }
        viewModelScope.launch {
            try {
                userRepository.updatePlayerLogin(profileStateFlow.value.user)
                    .getDataOrThrowException()
                success()
            } catch (e: Exception) {
                e.printStackTrace()
                profileStateFlow.update { it.copy(error = e.message) }
            }
        }
    }
}