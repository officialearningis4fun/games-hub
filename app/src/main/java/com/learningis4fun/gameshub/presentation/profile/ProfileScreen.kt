package com.learningis4fun.gameshub.presentation.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.ui.routes.ViewProfile
import com.learningis4fun.gameshub.commons.ui.theme.GamesHubAppTheme
import com.learningis4fun.gameshub.util.ScaleTransitionDirection
import com.learningis4fun.gameshub.util.scaleIntoContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
fun NavGraphBuilder.profileScreenComposable(
    onBackClick: () -> Unit,
    onEditProfile: () -> Unit
) {
    composable<ViewProfile>(
        enterTransition = {
            scaleIntoContainer()
        },
        popEnterTransition = {
            scaleIntoContainer(direction = ScaleTransitionDirection.INWARDS)
        }
    ) {

        val profileViewModel: ProfileViewModel = hiltViewModel()
        val userProfileInfo by profileViewModel.profileState.collectAsStateWithLifecycle()

        ProfileScreen(
            onBackClick = onBackClick,
            onEditProfile = onEditProfile,
            profileState = userProfileInfo
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    profileState: ProfileState
) {

    val userProfileInfo = profileState.user

    val userImage = userProfileInfo.imageUrl.toIntOrNull() ?: R.drawable.avatar_man1

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(
                    elevation = 5.dp,
                    spotColor = Color.LightGray
                ),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    Text(text = stringResource(id = R.string.screen_profile_title))
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                },
                actions = {}
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.CenterEnd
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier
                        .clickable { onEditProfile() }
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Image(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .aspectRatio(1f),
                        contentScale = ContentScale.Crop,
                        painter = painterResource(id = userImage),
                        contentDescription = null
                    )

                    Column(
                        modifier = Modifier,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            modifier = Modifier,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            text = userProfileInfo.profileName,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            modifier = Modifier,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            text = userProfileInfo.email,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                }

                HorizontalDivider()

                Text(
                    modifier = Modifier
                        .padding(vertical = 8.dp),
                    text = "The name and profile image will identify you to others",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelSmall
                )

            }

        }
    }
}

@Preview
@Composable
private fun ProfileScreenLightPrev() {
    GamesHubAppTheme(
        darkTheme = false
    ) {
        ProfileScreen(
            profileState = ProfileState(
                user = Player(
                    profileName = "Hlompho",
                    email = "hlomphoshadrakmohapi@gmail.com",
                    imageUrl = ""
                )
            )
        )
    }
}

@Preview
@Composable
private fun ProfileScreenDarkPrev() {
    GamesHubAppTheme(
        darkTheme = true
    ) {
        ProfileScreen(
            profileState = ProfileState(
                user = Player(
                    profileName = "Hlompho",
                    email = "hlomphoshadrakmohapi@gmail.com",
                    imageUrl = R.drawable.avatar_boy2.toString()
                )
            )
        )
    }
}