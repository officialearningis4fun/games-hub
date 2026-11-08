package com.learningis4fun.gameshub.presentation.create_game.online

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.CardsToolDialog
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.EliminationLimitToolDialog
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.PlayerInfo
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.ToolChip
import com.learningis4fun.gameshub.commons.ui.routes.CreateGameGraph
import com.learningis4fun.gameshub.commons.ui.routes.CreateOnlineGameRoute
import com.learningis4fun.gameshub.commons.ui.routes.PlayCardsOnlineScreenRoute
import com.learningis4fun.gameshub.presentation.start.friends.CreateOnlineGameState
import com.learningis4fun.gameshub.presentation.util.GamesHubAnimatedContentScope
import com.learningis4fun.gameshub.presentation.util.GamesHubAppThemePrev
import com.learningis4fun.gameshub.presentation.util.LocalAnimatedContentScope
import com.learningis4fun.gameshub.presentation.util.LocalCustomSharedTransitionScope
import com.learningis4fun.gameshub.presentation.util.sharedElementKeyOfOngoingGameSection
import com.learningis4fun.gameshub.util.ScaleTransitionDirection
import com.learningis4fun.gameshub.util.scaleIntoContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi


@OptIn(ExperimentalCoroutinesApi::class)
fun NavGraphBuilder.createOnlineGameScreenComposable(
    navController: NavHostController,
    onClose: () -> Unit,
    onAddParticipants: () -> Unit = {},
) {
    composable<CreateOnlineGameRoute>(
        popEnterTransition = {
            scaleIntoContainer(direction = ScaleTransitionDirection.INWARDS)
        }) { backStackEntry ->
        backStackEntry.destination.parent

        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry(CreateGameGraph)
        }

        val createOnlineGameViewModel: CreateOnlineGameViewModel = hiltViewModel(parentEntry)

        val createNewGameToolsState by createOnlineGameViewModel.createNewFriendsGameState.collectAsStateWithLifecycle()

        GamesHubAnimatedContentScope(
            animatedContentScope = this@composable
        ) {
            CreateOnlineGameScreen(
                modifier = Modifier.fillMaxSize(),
                createNewGameToolsState = createNewGameToolsState,
                onAddParticipants = onAddParticipants,
                onCreateGame = {
                    createOnlineGameViewModel.onCreateOnlineGame { gameId ->
                        navController.popBackStack(
                            route = CreateGameGraph,
                            inclusive = true
                        )
                        navController.navigate(
                            PlayCardsOnlineScreenRoute(
                                currentGameId = gameId
                            )
                        )
                    }
                },
                onClose = onClose,
                onEvent = createOnlineGameViewModel::onEvent,
            )
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateOnlineGameScreen(
    modifier: Modifier = Modifier,
    createNewGameToolsState: CreateOnlineGameState = CreateOnlineGameState(),
    onAddParticipants: () -> Unit = {},
    onCreateGame: () -> Unit = {},
    onClose: () -> Unit = {},
    onEvent: (CreateOnlineGameEvents) -> Unit = {},
) {

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.shadow(
                    elevation = 5.dp, spotColor = Color.LightGray
                ), colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ), title = {
                    Text(text = stringResource(id = R.string.screen_create_game_title))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onClose()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                })
        })
    { paddingValues ->
        CreateOnlineGameSection(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            createNewGameToolsState = createNewGameToolsState,
            onAddParticipants = onAddParticipants,
            onCreateGame = onCreateGame,
            onEvent = onEvent,
        )
    }

}

@Composable
fun CreateOnlineGameLoadingScreen(
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp)
        )
    }

}

@Composable
fun CreateOnlineGameSection(
    modifier: Modifier,
    createNewGameToolsState: CreateOnlineGameState,
    onAddParticipants: () -> Unit,
    onCreateGame: () -> Unit,
    onEvent: (CreateOnlineGameEvents) -> Unit = {}
) {
    LazyColumn(modifier) {

        item {
            CreateNewGameSection(
                modifier = Modifier.fillMaxWidth(),
                createNewGameToolsState = createNewGameToolsState,
                onAddParticipants = onAddParticipants,
                onCreateGame = onCreateGame,
                onCreateEvent = onEvent
            )
        }

    }

}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
private fun CreateNewGameSection(
    modifier: Modifier = Modifier,
    createNewGameToolsState: CreateOnlineGameState,
    onCreateEvent: (CreateOnlineGameEvents) -> Unit,
    onCreateGame: () -> Unit,
    onAddParticipants: () -> Unit,
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
            showDialog = showCardsToolDialog, onDecrease = {
                onCreateEvent(CreateOnlineGameEvents.DecreaseNumberOfCards)
            }, onIncrease = {
                onCreateEvent(CreateOnlineGameEvents.IncreaseNumberOfCards)
            }, onDismissRequested = {
                showCardsToolDialog = false
            }, numOfCardsPerPlayer = createNewGameToolsState.numberOfCardsPerPlayer
        )

        EliminationLimitToolDialog(
            showDialog = showEliminationLimitDialog,
            eliminationLimitForCrusade = createNewGameToolsState.eliminationLimitForCrusade,
            onDismissRequest = {
                showEliminationLimitDialog = false
            },
            onDecrease = {
                onCreateEvent(CreateOnlineGameEvents.DecreaseEliminationLimitForCrusade)
            },
            onIncrease = {
                onCreateEvent(CreateOnlineGameEvents.IncreaseEliminationLimitForCrusade)
            })

        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    })

                ToolChip(
                    imageVector = Icons.Default.Info,
                    imageVectorTint = MaterialTheme.colorScheme.error,
                    label = "${createNewGameToolsState.eliminationLimitForCrusade} Limit".uppercase(),
                    onClick = {
                        showEliminationLimitDialog = true
                    })
            }

            HorizontalDivider()
            with(sharedTransitionScope) {
                Column(
                    modifier = modifier.sharedElement(
                        state = rememberSharedContentState(key = sharedElementKeyOfOngoingGameSection),
                        animatedVisibilityScope = animatedContentScope
                    )
                ) {

                    CreateGameSetupToolsSection(
                        modifier = Modifier.fillMaxWidth(),
                        createNewGameToolsState = createNewGameToolsState,
                        onEvent = onCreateEvent,
                        onCreateGame = onCreateGame,
                        onAddParticipants = onAddParticipants
                    )

                }
            }
        }
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreateGameSetupToolsSection(
    modifier: Modifier = Modifier,
    createNewGameToolsState: CreateOnlineGameState,
    onAddParticipants: () -> Unit,
    onCreateGame: () -> Unit,
    onEvent: (CreateOnlineGameEvents) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Column(
            modifier = modifier.background(MaterialTheme.colorScheme.background)
        ) {

            createNewGameToolsState.userInfo?.let {
                PlayerItem(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    playerInfo = createNewGameToolsState.userInfo,
                    onRemove = {})
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.padding(end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "OPPONENTS",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                TextButton(
                    onClick = {
                        onAddParticipants()
                    }) {
                    Text(text = "ADD")
                }
            }

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {

                createNewGameToolsState.availablePlayers.forEach { playerInfo ->
                    PlayerItem(
                        modifier = Modifier, playerInfo = playerInfo, onRemove = {
                            onEvent(CreateOnlineGameEvents.CheckParticipantClick(playerInfo))
                        })
                }
            }
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            enabled = createNewGameToolsState.enableCreateGameButton,
            onClick = {
               onCreateGame()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            if (createNewGameToolsState.createGameLoading) {
                CircularProgressIndicator(
                    Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    trackColor = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(8.dp))
            }
            Icon(
                imageVector = Icons.Default.Edit, contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Create game".uppercase())
        }
    }

}

@Composable
private fun PlayerItem(
    modifier: Modifier = Modifier, playerInfo: PlayerInfo, onRemove: () -> Unit = {}
) {
    val imageRes = playerInfo.player.imageUrl.toIntOrNull() ?: R.drawable.avatar_person1

    Row(
        modifier = modifier
            .border(
                width = 1.dp,
                shape = RoundedCornerShape(32.dp),
                color = Color.LightGray.copy(alpha = 0.8f)
            )
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
        ) {

            Image(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .border(
                        width = 1.dp,
                        shape = CircleShape,
                        color = Color.LightGray.copy(alpha = 0.8f)
                    ),
                contentScale = ContentScale.Crop,
                painter = painterResource(id = imageRes),
                contentDescription = null
            )

            if (playerInfo.canRemove) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onRemove() }
                        .padding(2.dp)) {
                    Icon(
                        imageVector = Icons.Default.Clear, contentDescription = "Remove player"
                    )
                }
            }

        }
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = playerInfo.player.email,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview
@Composable
private fun CreateOnlineGameScreenLightPrev() {
    GamesHubAppThemePrev {
        CreateOnlineGameScreen(
            modifier = Modifier.fillMaxSize(),
            createNewGameToolsState = CreateOnlineGameState(
                userInfo = PlayerInfo(
                    player = Player(
                        profileName = "You",
                        uniqueId = "fjghsdkhjbvsdoj;kfsgvu",
                        email = "you@example.com"
                    ), canRemove = false, includeInList = true
                ), playerInfos = listOf(
                    PlayerInfo(
                        player = Player(
                            profileName = "Teboho",
                            uniqueId = "djhsdfhgdasljkdbj",
                            email = "teboho@example.com"
                        ), canRemove = true, includeInList = true
                    ),
                    PlayerInfo(
                        player = Player(
                            profileName = "Kgotso",
                            uniqueId = "kgotso-unique-id",
                            email = "kgotso@example.com",
                        ), canRemove = true, includeInList = true
                    ),
                    PlayerInfo(
                        player = Player(
                            profileName = "Mpho",
                            uniqueId = "mpho-unique-id",
                            email = "mpho@example.com",
                        ), canRemove = true, includeInList = true
                    ),
                )
            )
        )
    }
}

@Preview
@Composable
private fun CreateOnlineGameScreenDarkPrev() {
    GamesHubAppThemePrev(darkTheme = true) {
        CreateOnlineGameScreen(
            modifier = Modifier.fillMaxSize(),
            createNewGameToolsState = CreateOnlineGameState(
                playerInfos = listOf(
                    PlayerInfo(
                        player = Player(
                            profileName = "Teboho",
                            uniqueId = "djhsdfhgdasljkdbj",
                            email = "teboho@example.com"
                        ), canRemove = true, includeInList = true
                    ),
                    PlayerInfo(
                        player = Player(
                            profileName = "Kgotso",
                            uniqueId = "kgotso-unique-id",
                            email = "kgotso@example.com",
                        ), canRemove = true, includeInList = true
                    ),
                    PlayerInfo(
                        player = Player(
                            profileName = "Mpho",
                            uniqueId = "mpho-unique-id",
                            email = "mpho@example.com",
                        ), canRemove = true, includeInList = true
                    ),
                ), userInfo = PlayerInfo(
                    player = Player(
                        profileName = "You",
                        uniqueId = "fjghsdkhjbvsdoj;kfsgvu",
                        email = "you@example.com"
                    ), canRemove = false, includeInList = true
                )
            )
        )
    }
}