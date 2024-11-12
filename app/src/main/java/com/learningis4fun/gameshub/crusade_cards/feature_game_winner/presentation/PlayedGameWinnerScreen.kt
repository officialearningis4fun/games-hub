package com.learningis4fun.gameshub.crusade_cards.feature_game_winner.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toUIDuration
import com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools.CreateGameSetupToolsSection
import com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools.CreateNewGameToolsEvents
import com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools.CreateNewGameToolsState
import com.learningis4fun.gameshub.crusade_cards.commons.ui.theme.GamesHubAppTheme
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.Heading
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.OnGoingGame
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.OngoingGamesOrNewGameState
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.OngoingGamesSection
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.PlayerItem
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameWithPlayers


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayedGameWinner(
    modifier: Modifier = Modifier,
    playedGameWinnerState: PlayedGameWinnerOrNextStage.PlayedGameWinnerState,
    createNewGameToolsState: CreateNewGameToolsState,
    onStartGame: () -> Unit = {},
    onResumeClick: (String) -> Unit = {},
    onEvent: (PlayedGameWinnerEvents) -> Unit = {},
    onCreateEvent: (CreateNewGameToolsEvents) -> Unit = {}
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
                text = playedGameWinnerState.winnerNames,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Black
                )
            )

            Text(
                text = " won within ",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = playedGameWinnerState.gameDuration,
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
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            playedGameWinnerState.otherPlayers.forEach {
                PlayerItem(
                    modifier = Modifier.size(42.dp),
                    player = it
                )
            }
        }

        when (playedGameWinnerState.ongoingGamesOrNewGameState) {
            OngoingGamesOrNewGameState.Loading -> {
                CircularProgressIndicator()
            }

            OngoingGamesOrNewGameState.NewGameState -> {
                CreateNewGameSection(
                    createNewGameToolsState = createNewGameToolsState,
                    onCreateEvent = onCreateEvent,
                    onStartGame = onStartGame
                )
            }

            is OngoingGamesOrNewGameState.OngoingGamesState -> {
                PlayedGameOngoingGamesSection(
                    ongoingGamesState = playedGameWinnerState.ongoingGamesOrNewGameState,
                    onDeleteGame = { gameId ->
                        onEvent(PlayedGameWinnerEvents.DeleteGame(gameId))
                    },
                    onResumeClick = onResumeClick
                )
            }
        }


    }
}

@Composable
private fun CreateNewGameSection(
    modifier: Modifier = Modifier,
    createNewGameToolsState: CreateNewGameToolsState,
    onCreateEvent: (CreateNewGameToolsEvents) -> Unit,
    onStartGame: () -> Unit
) {
    Column(modifier) {
        HorizontalDivider()

        LazyColumn {
            item {
                CreateGameSetupToolsSection(
                    modifier = Modifier
                        .fillMaxWidth(),
                    createGameButtonText = "Play another game",
                    createNewGameToolsState = createNewGameToolsState,
                    onEvent = onCreateEvent,
                    onStartGame = onStartGame
                )
            }
        }

    }
}

@Composable
private fun PlayedGameOngoingGamesSection(
    modifier: Modifier = Modifier,
    ongoingGamesState: OngoingGamesOrNewGameState.OngoingGamesState,
    onResumeClick: (String) -> Unit = {},
    onDeleteGame: (String) -> Unit = {}
) {
    Column(modifier) {
        HorizontalDivider()

        Heading(
            text = "Ongoing games",
            modifier = Modifier.padding(16.dp),
            textColor = MaterialTheme.colorScheme.onBackground
        )

        OngoingGamesSection(
            ongoingGamesState = ongoingGamesState,
            onDeleteGame = onDeleteGame,
            onResumeClick = onResumeClick
        )
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
            playedGameWinnerState = PlayedGameWinnerOrNextStage.PlayedGameWinnerState(),
            createNewGameToolsState = CreateNewGameToolsState(),
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
            playedGameWinnerState = PlayedGameWinnerOrNextStage.PlayedGameWinnerState(),
            createNewGameToolsState = CreateNewGameToolsState()
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
            playedGameWinnerState = PlayedGameWinnerOrNextStage.PlayedGameWinnerState(
                loading = false,
                gameDuration = (80000L).toUIDuration(),
                otherPlayers = listOf(
                    Player(
                        "id",
                        profileName = "Hlompho",
                        imageUrl = R.drawable.avatar_boy1.toString()
                    ),
                ),
                winnerNames = "Hlompho",
                winnerImageUrl = R.drawable.avatar_boy1.toString(),
                ongoingGamesOrNewGameState = ongoingGamesState
            ),
            createNewGameToolsState = CreateNewGameToolsState()
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
            playedGameWinnerState = PlayedGameWinnerOrNextStage.PlayedGameWinnerState(
                loading = false,
                gameDuration = (8L).toUIDuration(),
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
                winnerImageUrl = R.drawable.avatar_boy1.toString(),
                ongoingGamesOrNewGameState = ongoingGamesState
            ),
            createNewGameToolsState = CreateNewGameToolsState()
        )
    }
}

val ongoingGamesState = OngoingGamesOrNewGameState.OngoingGamesState(
    listOf(
        OnGoingGame(
            threeCards = Constants
                .Cards
                .cards
                .take(3).mapIndexed { index, card ->
                    card.copy(
                        id = "$index"
                    )
                },
            gameWithPlayers = GameWithPlayers(
                game = Game(),
                players = listOf(
                    Player(
                        uniqueId = "id1",
                        profileName = "Dev1",
                        imageUrl = R.drawable.avatar_boy2.toString()
                    ),
                    Player(
                        uniqueId = "id2",
                        profileName = "You",
                        imageUrl = R.drawable.ai_platform.toString()
                    )
                )
            )
        )
    )
)