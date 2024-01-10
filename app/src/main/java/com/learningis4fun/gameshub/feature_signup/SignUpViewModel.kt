package com.learningis4fun.gameshub.feature_signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.AuthStates
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val signUpScreenEventsChannel = Channel<SignUpScreenEvents>()
    val signUpScreenEvents = signUpScreenEventsChannel.receiveAsFlow()

    fun googleAuthForFirebase(account: GoogleSignInAccount) {
        viewModelScope.launch {
            userRepository.googleSignInWithFirebase(account)
        }
    }

    fun signUpScreenStates(): Flow<SignUpScreenStates> = flow {
        emit(SignUpScreenStates.Loading)
        val otherStates = userRepository.authStateListener().map { authStates ->
            when (authStates) {
                is AuthStates.NotSignedIn -> {
                    SignUpScreenStates.NotSignedIn("Have not signed in...")
                }
                is AuthStates.NotSignedUp -> {
                    SignUpScreenStates.NotSignedIn("Have not signed in...")
                }
                is AuthStates.SignedIn -> {
                    signUpScreenEventsChannel.send(SignUpScreenEvents.NavigateToStartScreen)
                    SignUpScreenStates.SignedIn
                }
            }
        }
        emitAll(otherStates)
    }

    sealed class SignUpScreenStates {
        data class Error(val message: String) : SignUpScreenStates()
        object Loading : SignUpScreenStates()
        data class NotSignedIn(val message: String) : SignUpScreenStates()
        object SignedIn : SignUpScreenStates()
    }

    sealed class SignUpScreenEvents {
        object NavigateToStartScreen : SignUpScreenEvents()
    }
}