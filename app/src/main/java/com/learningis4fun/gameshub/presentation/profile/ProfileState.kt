package com.learningis4fun.gameshub.presentation.profile

import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.util.Constants

data class ProfileState(
    val avatars: List<Int> = defaultAvatars,
    val user: Player = Player(),
    val isEnableSaveButton: Boolean = false,
    val isProfileNameCountLimitReached: Boolean = false,
    val profileNameCountLimit: String = Constants.PROFILE_NAME_COUNT_LIMIT.toString(),
    val error: String? = null,
    val isLoading: Boolean = true,
    val signedIn: SignedIn = SignedIn.NotSignedIn,
    val signInError: String? = null,
) {

    fun enableSaveButton(newUser: Player): ProfileState {
        val enable = newUser.imageUrl != user.imageUrl || newUser.profileName != user.profileName
        return this.copy(
            isEnableSaveButton = enable
        )
    }
}

sealed interface SignedIn{
    object Anonymously: SignedIn
    data class WithEmail(val email: String): SignedIn
    object NotSignedIn: SignedIn
}


val defaultAvatars = listOf(
    R.drawable.avatar_boy1,
    R.drawable.avatar_boy2,
    R.drawable.avatar_male3,
    R.drawable.avatar_man1,

    R.drawable.avatar_female3,
    R.drawable.avatar_girl1,
    R.drawable.avatar_girl2,
    R.drawable.avatar_person1
)
