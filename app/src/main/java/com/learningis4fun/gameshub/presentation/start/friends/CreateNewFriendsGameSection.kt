package com.learningis4fun.gameshub.presentation.start.friends

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.domain.util.playerName
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.CardsToolDialog
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.CreateGameSetupToolsSection
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.CreateNewGameToolsEvents
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.CreateNewGameToolsState
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.EliminationLimitToolDialog
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.NewGameWithAIState
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.OnlineOfflineMode
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.PlayerInfo
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.ToolChip
import com.learningis4fun.gameshub.presentation.util.GamesHubAppThemePrev
import com.learningis4fun.gameshub.presentation.util.LocalAnimatedContentScope
import com.learningis4fun.gameshub.presentation.util.LocalCustomSharedTransitionScope
import com.learningis4fun.gameshub.presentation.util.sharedElementKeyOfOngoingGameSection

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
fun CreateNewFriendsGameSection(
    modifier: Modifier = Modifier,
    createNewGameToolsState: CreateNewGameToolsState,
    onCreateEvent: (CreateNewGameToolsEvents) -> Unit,
    onStartGame: () -> Unit,
    onStartAIGame: () -> Unit
) {
    val sharedTransitionScope = LocalCustomSharedTransitionScope.current
    val animatedContentScope = LocalAnimatedContentScope.current

    var showEliminationLimitDialog by remember {
        mutableStateOf(false)
    }

    var showCardsToolDialog by remember {
        mutableStateOf(false)
    }

    Box {

        CardsToolDialog(
            showDialog = showCardsToolDialog,
            onDecrease = {
                onCreateEvent(CreateNewGameToolsEvents.DecreaseNumberOfCards)
            },
            onIncrease = {
                onCreateEvent(CreateNewGameToolsEvents.IncreaseNumberOfCards)
            },
            onDismissRequested = {
                showCardsToolDialog = false
            },
            numOfCardsPerPlayer = createNewGameToolsState.numberOfCardsPerPlayer
        )

        EliminationLimitToolDialog(
            showDialog = showEliminationLimitDialog,
            eliminationLimitForCrusade = createNewGameToolsState.eliminationLimitForCrusade,
            onDismissRequest = {
                showEliminationLimitDialog = false
            },
            onDecrease = {
                onCreateEvent(CreateNewGameToolsEvents.DecreaseEliminationLimitForCrusade)
            },
            onIncrease = {
                onCreateEvent(CreateNewGameToolsEvents.IncreaseEliminationLimitForCrusade)
            }
        )

        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                ToolChip(
                    icon = R.drawable.back,
                    label = "${createNewGameToolsState.numberOfCardsPerPlayer} Cards".uppercase(),
                    onClick = {
                        showCardsToolDialog = true
                    }
                )

                ToolChip(
                    imageVector = Icons.Default.Info,
                    imageVectorTint = MaterialTheme.colorScheme.error,
                    label = "${createNewGameToolsState.eliminationLimitForCrusade} Limit".uppercase(),
                    onClick = {
                        showEliminationLimitDialog = true
                    }
                )
            }

            HorizontalDivider()
            with(sharedTransitionScope) {
                Column(
                    modifier = modifier
                        .sharedElement(
                            state = rememberSharedContentState(key = sharedElementKeyOfOngoingGameSection),
                            animatedVisibilityScope = animatedContentScope
                        )
                ) {

                    CreateGameSetupToolsSection(
                        modifier = Modifier
                            .fillMaxWidth(),
                        createGameButtonText = "Play another game",
                        createNewGameToolsState = createNewGameToolsState,
                        onStartGame = onStartGame,
                        onEvent = onCreateEvent
                    )

                }
            }
        }
    }

}


val createNewGameToolsState = CreateNewGameToolsState(
    numberOfParticipants = 3,
    playersInfo = listOf(
        Constants.Players.USER,
        Constants.Players.DEVICE1,
        Constants.Players.DEVICE2,
    ).map {
        PlayerInfo(
            player = it.copy(
                profileName = it.playerName(Constants.Players.USER.uniqueId)
            ),
            canRemove = it.uniqueId != Constants.Players.USER.uniqueId,
            includeInList = true
        )
    }
)

@Preview
@Composable
fun CreateNewGameSectionPrev() {
    GamesHubAppThemePrev {
        CreateNewFriendsGameSection(
            createNewGameToolsState = createNewGameToolsState,
            onCreateEvent = {},
            onStartGame = { /*TODO*/ },
            onStartAIGame = {})
    }
}

@Preview
@Composable
fun CreateNewGameSectionPrevDark() {
    GamesHubAppThemePrev(darkTheme = true) {
        CreateNewFriendsGameSection(
            createNewGameToolsState = createNewGameToolsState,
            onCreateEvent = {},
            onStartGame = { /*TODO*/ },
            onStartAIGame = {})
    }
}