package com.learningis4fun.gameshub.crusade_cards.feature_game_winner.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toTimeFormatted
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.WinnerScreenRoute
import com.learningis4fun.gameshub.crusade_cards.commons.ui.theme.GamesHubAppTheme
import com.learningis4fun.gameshub.util.ScaleTransitionDirection
import com.learningis4fun.gameshub.util.scaleIntoContainer

fun NavGraphBuilder.playedGameWinnerComposable(
    onStartGame: () -> Unit,
    onCloseClick: () -> Unit
) {
    composable<WinnerScreenRoute>(
        enterTransition = {
            scaleIntoContainer()
        },
        popEnterTransition = {
            scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS)
        }
    ) {

        val viewModel: PlayedGameWinnerViewModel = hiltViewModel()

        val playedGameWinnerState by viewModel.playedGameWinnerState.collectAsStateWithLifecycle()

        println("viewModel hash: ${viewModel.hashCode()}")

        PlayedGameWinner(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            playedGameWinnerState = playedGameWinnerState,
            onStartGame = onStartGame,
            onCloseClick = onCloseClick
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayedGameWinner(
    modifier: Modifier = Modifier,
    playedGameWinnerState: PlayedGameWinnerState,
    onStartGame: () -> Unit = {},
    onCloseClick: () -> Unit = {}
) {

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    Text(text = stringResource(id = R.string.screen_winner_title))
                },
                actions = {
                    IconButton(onClick = onCloseClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (playedGameWinnerState.loading) {
                CircularProgressIndicator()
            } else {
                WinnerSection(
                    modifier = Modifier,
                    winnerImageUrl = playedGameWinnerState.winnerImageUrl,
                    winnerNames = playedGameWinnerState.winnerNames,
                    duration = playedGameWinnerState.gameDuration,
                    otherPlayers = playedGameWinnerState.otherPlayers, onStartGame = onStartGame,
                )
            }
        }
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WinnerSection(
    modifier: Modifier,
    winnerImageUrl: String,
    winnerNames: String,
    duration: String,
    otherPlayers: List<Player>,
    onStartGame: () -> Unit,
) {
    val winnerImage by remember {
        mutableIntStateOf(
            winnerImageUrl.toIntOrNull() ?: R.drawable.game_test_profile
        )
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            painter = painterResource(id = winnerImage),
            contentDescription = null
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {

            Text(
                text = winnerNames,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Black
                )
            )

            Text(
                text = " won within ",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = duration,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Black
                )
            )

            Text(
                text = " against",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            otherPlayers.forEach {
                com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.PlayerItem(
                    modifier = Modifier.size(42.dp),
                    player = it
                )
            }
        }

        Button(
            modifier = Modifier.padding(8.dp),
            onClick = onStartGame,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                painterResource(id = R.drawable.ic_outline_phone_android),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "PLAY ANOTHER GAME")
        }
    }
}

@Preview
@Composable
private fun PlayedGameWinnerLoadingLightPrev() {
    MaterialTheme {
        PlayedGameWinner(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            playedGameWinnerState = PlayedGameWinnerState()
        )
    }
}

@Preview
@Composable
private fun PlayedGameWinnerLoadingDarkPrev() {
    MaterialTheme {
        PlayedGameWinner(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            playedGameWinnerState = PlayedGameWinnerState()
        )
    }
}

@Preview
@Composable
private fun PlayedGameWinnerPrev() {
    GamesHubAppTheme(darkTheme = false) {
        PlayedGameWinner(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            playedGameWinnerState = PlayedGameWinnerState(
                loading = false,
                gameDuration = (80000000000L).toTimeFormatted(),
                otherPlayers = listOf(
                    Player(
                        "id",
                        profileName = "Hlompho",
                        imageUrl = R.drawable.avatar_boy1.toString()
                    ),
                    Player(
                        "id2",
                        profileName = "Android",
                        imageUrl = R.drawable.ai_platform.toString()
                    ),
                ),
                winnerNames = "Hlompho",
                winnerImageUrl = R.drawable.avatar_boy1.toString()
            )
        )
    }
}

@Preview
@Composable
private fun PlayedGameWinnerDarkPrev() {
    GamesHubAppTheme(darkTheme = true) {
        PlayedGameWinner(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            playedGameWinnerState = PlayedGameWinnerState(
                loading = false,
                gameDuration = (80000000000L).toTimeFormatted(),
                otherPlayers = listOf(
                    Player(
                        "id",
                        profileName = "Hlompho",
                        imageUrl = R.drawable.avatar_boy1.toString()
                    ),
                    Player(
                        "id2",
                        profileName = "Android",
                        imageUrl = R.drawable.ai_platform.toString()
                    ),
                ),
                winnerNames = "Hlompho",
                winnerImageUrl = R.drawable.avatar_boy1.toString()
            )
        )
    }
}