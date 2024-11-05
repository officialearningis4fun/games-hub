package com.learningis4fun.gameshub.crusade_cards.feature_game_winner.presentation

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toUIDuration
import com.learningis4fun.gameshub.crusade_cards.commons.ui.theme.GamesHubAppTheme

@Composable
fun PlayedGameNextStage(
    modifier: Modifier = Modifier,
    playedGameNextStageState: PlayedGameWinnerOrNextStage.PlayedGameNextStageState,
    onStartGame: () -> Unit = {},
    onEvent: (PlayedGameWinnerEvents) -> Unit = {},
) {

    NextStageSection(
        modifier = modifier,
        winnerNames = playedGameNextStageState.winnerNames,
        duration = playedGameNextStageState.gameDuration,
        nextStagePlayers = playedGameNextStageState.nextStagePlayers,
        eliminatedPlayers = playedGameNextStageState.eliminatedPlayers,
        onStartGame = onStartGame,
        onEvent = onEvent,
    )

}

@Composable
private fun NextStageSection(
    modifier: Modifier,
    winnerNames: String,
    duration: String,
    nextStagePlayers: List<Player>,
    eliminatedPlayers: List<Player>,
    onStartGame: () -> Unit,
    onEvent: (PlayedGameWinnerEvents) -> Unit = {},
) {

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
        }

        PlayersSection(
            otherPlayers = nextStagePlayers,
            title = "Next stage players",
            titleColor = MaterialTheme.colorScheme.onBackground
        )

        PlayersSection(
            otherPlayers = eliminatedPlayers,
            title = "Eliminated players",
            titleColor = MaterialTheme.colorScheme.error
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
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
            Text(text = "Go to next stage".uppercase())
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayersSection(
    otherPlayers: List<Player>,
    title: String,
    titleColor: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Black
            ),
            color = titleColor
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            otherPlayers.forEach {
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.PlayerItem(
                        modifier = Modifier.size(42.dp),
                        player = it
                    )
                    Text(
                        text = it.profileName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PlayedGameWinnerLoadingLightPrev() {
    MaterialTheme {
        PlayedGameNextStage(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            playedGameNextStageState = PlayedGameWinnerOrNextStage.PlayedGameNextStageState(),
        )
    }
}

@Preview
@Composable
private fun PlayedGameWinnerLoadingDarkPrev() {
    MaterialTheme {
        PlayedGameNextStage(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            playedGameNextStageState = PlayedGameWinnerOrNextStage.PlayedGameNextStageState(),

            )
    }
}

@Preview
@Composable
private fun PlayedGameWinnerPrev() {
    GamesHubAppTheme(darkTheme = false) {
        PlayedGameNextStage(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            playedGameNextStageState = PlayedGameWinnerOrNextStage.PlayedGameNextStageState(
                loading = false,
                gameDuration = (80000L).toUIDuration(),
                eliminatedPlayers = listOf(
                    Player(
                        "id",
                        profileName = "Hlompho",
                        imageUrl = R.drawable.avatar_boy1.toString()
                    ),
                ),
                nextStagePlayers = listOf(
                    Player(
                        "id2",
                        profileName = "Hlompho",
                        imageUrl = R.drawable.avatar_boy1.toString()
                    ),
                ),
                winnerNames = "Hlompho",
                winnerImageUrl = R.drawable.avatar_boy1.toString()
            ),
        )
    }
}

@Preview
@Composable
private fun PlayedGameWinnerDarkPrev() {
    GamesHubAppTheme(darkTheme = true) {
        PlayedGameNextStage(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            playedGameNextStageState = PlayedGameWinnerOrNextStage.PlayedGameNextStageState(
                loading = false,
                gameDuration = (8L).toUIDuration(),
                eliminatedPlayers = listOf(
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
                nextStagePlayers = listOf(
                    Player(
                        "id3",
                        profileName = "Hlompho",
                        imageUrl = R.drawable.avatar_boy1.toString()
                    ),
                ),
                winnerNames = "Hlompho",
                winnerImageUrl = R.drawable.avatar_boy1.toString()
            ),
        )
    }
}