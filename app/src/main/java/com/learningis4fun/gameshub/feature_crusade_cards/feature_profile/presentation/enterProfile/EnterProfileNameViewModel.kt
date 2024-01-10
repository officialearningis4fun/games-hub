package com.learningis4fun.gameshub.feature_crusade_cards.feature_profile.presentation.enterProfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.learningis4fun.gameshub.feature_crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
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
class EnterProfileNameViewModel @Inject constructor(
    private val repository: DefaultCardRepository,
    private val preference: CrusadeCardsPreferencesManager,
    private val userRepository: UserRepository
) : ViewModel() {

    var oldProfileUsername = MutableStateFlow("")
    var profileUsername = MutableStateFlow("")

    suspend fun getOldProfileName(): String {
        val userId = preference.userId.first()

        return try {

            if (userId.isNullOrBlank()) {
                throw Exception("User is not found")
            }

            val player = userRepository.getPlayer(userId).first()
                ?: throw Exception("User object was not found")

            oldProfileUsername.value = player.profileName

            player.profileName
        } catch (exception: Exception) {
            ""
        }
    }

    private val signUpScreenEventsChannel = Channel<SignUpScreenEvents>()
    val signUpScreenEvents = signUpScreenEventsChannel.receiveAsFlow()

    fun enableSaveProfileBtn(): LiveData<Boolean> = combine(
        profileUsername,
        oldProfileUsername
    )
    { currUserName, oldUserName ->
        Pair(
            currUserName,
            oldUserName
        )
    }.map { (currUserName, oldUserName) ->
        currUserName.isNotBlank() && currUserName != oldUserName
    }.asLiveData()

    fun onNameSaveBtnClick() = viewModelScope.launch {
        val player = userRepository.getPlayerSignInDetails().first()

        userRepository.updatePlayer(player = player)
        signUpScreenEventsChannel.send(SignUpScreenEvents.PopBackStack)
    }

    fun onNameChanged(currName: String) = viewModelScope.launch {
        profileUsername.value = currName
    }

    sealed class SignUpScreenEvents {
        object PopBackStack : SignUpScreenEvents()
    }
}