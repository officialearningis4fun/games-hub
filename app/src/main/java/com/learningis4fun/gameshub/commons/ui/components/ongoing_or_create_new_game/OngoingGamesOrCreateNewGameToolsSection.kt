package com.learningis4fun.gameshub.commons.ui.components.ongoing_or_create_new_game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.CreateNewGameSection
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.CreateNewGameToolsEvents
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.CreateNewGameToolsState
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.NewGameWithAIState
import com.learningis4fun.gameshub.presentation.start.OngoingGamesSection
import com.learningis4fun.gameshub.presentation.util.GamesHubAppThemePrev

@Composable
fun OngoingGamesOrCreateNewGameToolsSection(
    modifier: Modifier = Modifier,
    ongoingGamesOrCreateNewGameState: OngoingGamesOrCreateNewGameState,
    createNewGameToolsState: CreateNewGameToolsState,
    onCreateEvent: (CreateNewGameToolsEvents) -> Unit = {},
    onStartGame: () -> Unit = {},
    onStartAIGame: () -> Unit = {},
    onResumeClick: (String) -> Unit = {},
    onDeleteGame: (String) -> Unit = {},
) {
    Box(modifier = modifier) {
        when (ongoingGamesOrCreateNewGameState) {
            OngoingGamesOrCreateNewGameState.CreateNewGameToolsState -> {
                Column {
                    HorizontalDivider()
                    CreateNewGameSection(
                        modifier = Modifier,
                        createNewGameToolsState = createNewGameToolsState,
                        onCreateEvent = onCreateEvent,
                        onStartGame = onStartGame,
                        onStartAIGame = onStartAIGame
                    )
                }
            }

            OngoingGamesOrCreateNewGameState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopCenter)
                )
            }

            OngoingGamesOrCreateNewGameState.Nothing -> {}
            is OngoingGamesOrCreateNewGameState.OngoingGamesState -> {
                OngoingGamesSection(
                    modifier = Modifier,
                    games = ongoingGamesOrCreateNewGameState.games,
                    onDeleteGame = { gameId ->
                        onDeleteGame(gameId)
                    },
                    onResumeClick = onResumeClick
                )
            }
        }
    }
}

@Preview
@Composable
private fun OngoingGamesOrCreateNewGamePrev() {
    GamesHubAppThemePrev {
        OngoingGamesOrCreateNewGameToolsSection(
            modifier = Modifier.fillMaxWidth(),
            ongoingGamesOrCreateNewGameState = OngoingGamesOrCreateNewGameState.Loading,
            createNewGameToolsState = CreateNewGameToolsState()
        )
    }
}

@Preview
@Composable
private fun OngoingGamesOrCreateNewGamePrevLight() {
    GamesHubAppThemePrev(darkTheme = false) {
        OngoingGamesOrCreateNewGameToolsSection(
            modifier = Modifier
                .fillMaxWidth(),
            ongoingGamesOrCreateNewGameState = OngoingGamesOrCreateNewGameState.CreateNewGameToolsState,
            createNewGameToolsState = CreateNewGameToolsState(
                newGameWithAIState = NewGameWithAIState(
                    players = listOf(
                        Constants.Players.USER,
                        Constants.Players.DEVICE1,
                    )
                )
            )
        )
    }
}