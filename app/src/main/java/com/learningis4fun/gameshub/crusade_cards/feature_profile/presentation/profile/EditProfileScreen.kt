package com.learningis4fun.gameshub.crusade_cards.feature_profile.presentation.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.EditProfileRoute
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.Heading
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.presentation.ProfileEvents
import com.learningis4fun.gameshub.util.ScaleTransitionDirection
import com.learningis4fun.gameshub.util.scaleIntoContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi


@OptIn(ExperimentalCoroutinesApi::class)
fun NavGraphBuilder.editProfileScreenComposable(
    onBackClick: () -> Unit
) {
    composable<EditProfileRoute>(
        enterTransition = {
            scaleIntoContainer()
        },
        popEnterTransition = {
            scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS)
        }
    ) {

        val profileViewModel: ProfileViewModel = hiltViewModel()
        val profileState by profileViewModel.profileState.collectAsStateWithLifecycle()

        EditProfileScreen(
            modifier = Modifier.fillMaxSize(),
            profileState = profileState,
            onBackClick = onBackClick,
            onSaveProfileClick = {
                profileViewModel.onSaveProfile {
                    onBackClick()
                }
            },
            updateUserAvatar = {
                profileViewModel.onEvent(ProfileEvents.UpdateUserAvatar(it))
            },
            updateUsername = {
                profileViewModel.onEvent(ProfileEvents.UpdateUserName(it))

            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileScreen(
    modifier: Modifier = Modifier,
    profileState: ProfileState,
    onBackClick: () -> Unit = {},
    updateUsername: (String) -> Unit = {},
    updateUserAvatar: (Int) -> Unit = {},
    onSaveProfileClick: () -> Unit = {}
) {
    val backgroundColor = Color(0xffff8800)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    titleContentColor = Color.White
                ),
                title = {
                    Text(text = stringResource(id = R.string.screen_profile_choose_avatar))
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    TextButton(
                        modifier = Modifier,
                        enabled = profileState.isEnableSaveButton,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White,
                            disabledContentColor = Color.LightGray
                        ),
                        onClick = { onSaveProfileClick() }
                    ) {
                        Text(text = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.CenterEnd
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Heading(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    text = "Choose avatar",
                    textColor = MaterialTheme.colorScheme.onSurface
                )

                ChooseProfileAvatarSection(
                    modifier = Modifier.fillMaxWidth(),
                    avatars = profileState.avatars,
                    selectedAvatar = profileState.user.imageUrl.toIntOrNull(),
                    updateUserAvatar = { avatar ->
                        updateUserAvatar(
                            avatar
                        )
                    }
                )

                Heading(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    text = "Change username",
                    textColor = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    value = profileState.user.profileName,
                    onValueChange = {
                        updateUsername(it.trim())
                    }
                )

            }

        }
    }
}


@Composable
private fun ChooseProfileAvatarSection(
    modifier: Modifier,
    avatars: List<Int>,
    selectedAvatar: Int?,
    updateUserAvatar: (Int) -> Unit
) {
    val backgroundColor = Color(0xffff8800)

    LazyVerticalGrid(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 16.dp),
        columns = GridCells.Fixed(4),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(avatars) { avatar ->
            Box {
                Image(
                    modifier = Modifier
                        .clip(CircleShape)
                        .aspectRatio(1f)
                        .border(
                            width = 1.dp,
                            shape = CircleShape,
                            color = Color.LightGray.copy(alpha = 0.8f)
                        )
                        .clickable(interactionSource = null, indication = null, onClick = {
                            updateUserAvatar(avatar)
                        }),
                    painter = painterResource(id = avatar),
                    contentDescription = "boy1"
                )

                if (avatar == selectedAvatar) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier
                                .fillMaxSize(),
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "checkmark",
                            tint = backgroundColor
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ChooseProfileAvatarScreenPrev() {
    EditProfileScreen(
        profileState = ProfileState(
            avatars = defaultAvatars,
            user = Player(
                imageUrl = R.drawable.avatar_man1.toString()
            )
        )
    )
}