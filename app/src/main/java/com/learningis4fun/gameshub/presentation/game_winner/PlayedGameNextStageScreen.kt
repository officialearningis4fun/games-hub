package com.learningis4fun.gameshub.presentation.game_winner

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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.commons.domain.model.Card
import com.learningis4fun.gameshub.commons.ui.theme.GamesHubAppTheme
import com.learningis4fun.gameshub.domain.model.PlayerScreenInfo

@Composable
fun PlayedGameNextStageScreen(
    modifier: Modifier = Modifier,
    playedGameNextStageState: PlayedGameWinnerOrNextStage.PlayedGameNextStageState,
    onStartGame: () -> Unit = {}
) {

    NextStageSection(
        modifier = modifier,
        playedGameNextStageState = playedGameNextStageState,
        onStartGame = onStartGame,
    )

}

@Composable
private fun NextStageSection(
    modifier: Modifier,
    playedGameNextStageState: PlayedGameWinnerOrNextStage.PlayedGameNextStageState,
    onStartGame: () -> Unit
) {

    var showEliminatedPlayersCardsView by remember {
        mutableStateOf(Pair("", false))
    }

    var showNextPlayersCardsView by remember {
        mutableStateOf(Pair("", false))
    }

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
                text = playedGameNextStageState.winnerNames,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Black
                )
            )

            Text(
                text = " won within ",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = playedGameNextStageState.gameDuration,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Black
                )
            )
        }

        PlayersSection(
            otherPlayers = playedGameNextStageState.nextStagePlayersScreenInfo(),
            cardsOfPlayer = playedGameNextStageState.unPlayedCardsOfPlayer(showNextPlayersCardsView.first),
            onPopupDismissRequested = {
                showNextPlayersCardsView = Pair("", false)
            },
            showCardsView = showNextPlayersCardsView,
            title = "Next stage players",
            titleColor = MaterialTheme.colorScheme.onBackground,
            onPlayerClick = { playerId ->
                playedGameNextStageState.isPlayerHaveCards(playerId) {
                    showNextPlayersCardsView = Pair(playerId, true)
                }
            }
        )

        PlayersSection(
            otherPlayers = playedGameNextStageState.eliminatedPlayersScreenInfo(),
            cardsOfPlayer = playedGameNextStageState.unPlayedCardsOfPlayer(
                showEliminatedPlayersCardsView.first
            ),
            onPopupDismissRequested = {
                showEliminatedPlayersCardsView = Pair("", false)
            },
            showCardsView = showEliminatedPlayersCardsView,
            title = "Eliminated players",
            titleColor = MaterialTheme.colorScheme.error,
            onPlayerClick = { playerId ->
                playedGameNextStageState.isPlayerHaveCards(playerId) {
                    showEliminatedPlayersCardsView = Pair(playerId, true)
                }
            }
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
    otherPlayers: List<PlayerScreenInfo>,
    cardsOfPlayer: List<Card>,
    showCardsView: Pair<String, Boolean>,
    onPopupDismissRequested: () -> Unit,
    onPlayerClick: (String) -> Unit,
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

        PlayerCardsViewPopupContainer(
            cardsOfPlayer = cardsOfPlayer,
            onPopupDismissRequested = onPopupDismissRequested,
            isVisible = showCardsView.second
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            otherPlayers.forEach { player ->
                PlayerItemWithPopup(
                    modifier = Modifier,
                    playerScreenInfo = player,
                    onClick = { onPlayerClick(player.playerId) }
                )


            }
        }
    }
}

@Preview
@Composable
private fun PlayedGameWinnerLoadingLightPrev() {
    GamesHubAppTheme {
        PlayedGameNextStageScreen(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            playedGameNextStageState = playedGameNextStageState,
        )
    }
}

@Preview
@Composable
private fun PlayedGameWinnerDarkPrev() {
    GamesHubAppTheme(darkTheme = true) {
        PlayedGameNextStageScreen(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            playedGameNextStageState = playedGameNextStageState,
        )
    }
}