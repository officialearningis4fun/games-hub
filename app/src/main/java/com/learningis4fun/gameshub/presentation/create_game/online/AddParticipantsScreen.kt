package com.learningis4fun.gameshub.presentation.create_game.online

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
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
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.PlayerInfo
import com.learningis4fun.gameshub.commons.ui.routes.AddParticipantsRoute
import com.learningis4fun.gameshub.commons.ui.routes.CreateGameGraph
import com.learningis4fun.gameshub.commons.ui.theme.GamesHubAppTheme
import com.learningis4fun.gameshub.presentation.util.GamesHubAnimatedContentScope
import com.learningis4fun.gameshub.util.ScaleTransitionDirection
import com.learningis4fun.gameshub.util.scaleIntoContainer
import com.learningis4fun.papers.ui.common.icons.CheckCircle
import com.learningis4fun.papers.ui.common.icons.RadioButtonUnchecked
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
fun NavGraphBuilder.addParticipantsScreenComposable(
    navController: NavHostController,
    onNavBackClick: () -> Unit,
) {
    composable<AddParticipantsRoute>(
        popEnterTransition = {
            scaleIntoContainer(direction = ScaleTransitionDirection.INWARDS)
        }) { backStackEntry ->

        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry(CreateGameGraph)
        }

        val createOnlineGameViewModel: CreateOnlineGameViewModel = hiltViewModel(parentEntry)
        val searchQuery by createOnlineGameViewModel.searchQuery.collectAsStateWithLifecycle()
        val createNewFriendsGameState by createOnlineGameViewModel.createNewFriendsGameState.collectAsStateWithLifecycle()
        val isSearchedUserNotAFriend by createOnlineGameViewModel.isSearchedUserNotAFriend.collectAsStateWithLifecycle()
        GamesHubAnimatedContentScope(
            animatedContentScope = this@composable
        ) {
            AddParticipantsScreen(
                modifier = Modifier.fillMaxSize(),
                onNavBackClick = onNavBackClick,
                players = createNewFriendsGameState.playerInfos,
                isSearchedUserNotAFriend = isSearchedUserNotAFriend,
                onEvent = createOnlineGameViewModel::onEvent,
                searchQuery = searchQuery
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddParticipantsScreen(
    modifier: Modifier = Modifier,
    onNavBackClick: () -> Unit = {},
    players: List<PlayerInfo> = emptyList(),
    isSearchedUserNotAFriend: PlayerInfo? = null,
    onEvent: (CreateOnlineGameEvents) -> Unit = {},
    searchQuery: String = ""
) {

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = searchQuery,
                selection = TextRange(searchQuery.length)
            )
        )
    }

    LaunchedEffect(searchQuery) {
        if (textFieldValue.text != searchQuery) {
            textFieldValue = textFieldValue.copy(
                text = searchQuery,
                selection = TextRange(searchQuery.length)
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
                            value = textFieldValue,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            onValueChange = {
                                textFieldValue = it
                                onEvent(CreateOnlineGameEvents.UpdateSearchQuery(it.text))
                            },
                            decorationBox = { innerTextField ->
                                Box {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Name, email",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    innerTextField()
                                }
                            }
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    onEvent(CreateOnlineGameEvents.ClearSearchQuery)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "clear search"
                                )
                            }
                        }
                    }
                }, navigationIcon = {
                    IconButton(onClick = { onNavBackClick() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                })
        })
    { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
        ) {

            if (isSearchedUserNotAFriend != null) {
                item {
                    AddFriendItem(
                        modifier = Modifier.fillMaxWidth(),
                        playerInfo = isSearchedUserNotAFriend,
                        onAdd = {
                            onEvent(CreateOnlineGameEvents.AddUserToFriends(email = isSearchedUserNotAFriend.player.email))
                        }
                    )
                }
            }

            item {
                Text(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    text = "PARTICIPANTS",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            item {
                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(players.filter { it.includeInList }) {
                        PlayerItem(
                            playerInfo = it
                        )
                    }
                }
            }

            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            item {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    text = "Recently Added Opponents",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            items(players) {
                FriendItem(
                    playerInfo = it,
                    onCheckClick = {
                        onEvent(CreateOnlineGameEvents.CheckParticipantClick(it))
                    }
                )
            }
        }
    }
}


@Composable
private fun PlayerItem(
    modifier: Modifier = Modifier, playerInfo: PlayerInfo, onRemove: () -> Unit = {}
) {
    val imageRes = playerInfo.player.imageUrl.toIntOrNull() ?: R.drawable.avatar_person1

    Column(
        modifier = modifier
            .width(88.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
        ) {

            Image(
                modifier = Modifier
                    .size(56.dp)
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

@Composable
private fun AddFriendItem(
    modifier: Modifier = Modifier, playerInfo: PlayerInfo, onAdd: () -> Unit = {}
) {
    val imageRes = playerInfo.player.imageUrl.toIntOrNull() ?: R.drawable.avatar_person1

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
        ) {

            Image(
                modifier = Modifier
                    .size(40.dp)
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

        }
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            text = playerInfo.player.email,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        IconButton(
            onClick = onAdd
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "add",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun FriendItem(
    modifier: Modifier = Modifier,
    playerInfo: PlayerInfo,
    onCheckClick: () -> Unit = {}
) {
    val imageRes = playerInfo.player.imageUrl.toIntOrNull() ?: R.drawable.avatar_person1

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onCheckClick() },
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

        }
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            text = playerInfo.player.email,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Icon(
            modifier = Modifier,
            imageVector = if (playerInfo.includeInList) CheckCircle else RadioButtonUnchecked,
            contentDescription = "checked",
            tint = if (playerInfo.includeInList) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun AddParticipantsScreenLightPrev() {
    GamesHubAppTheme(
        darkTheme = false
    ) {
        AddParticipantsScreen(
            modifier = Modifier.fillMaxSize(),
            players = listOf(
                PlayerInfo(
                    player = Player(
                        profileName = "Teboho",
                        uniqueId = "djhsdfhgdasljkdbj",
                        email = "teboho@example.com"
                    ), canRemove = true, includeInList = false
                ),
                PlayerInfo(
                    player = Player(
                        profileName = "Kgotso",
                        uniqueId = "kgotso-unique-id",
                        email = "kgotso@example.com",
                    ), canRemove = true, includeInList = false
                ),
                PlayerInfo(
                    player = Player(
                        profileName = "Mpho",
                        uniqueId = "mpho-unique-id",
                        email = "mpho@example.com",
                    ), canRemove = true, includeInList = true
                ),
            ),
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun AddParticipantsScreenDarkPrev() {
    GamesHubAppTheme(
        darkTheme = true
    ) {
        AddParticipantsScreen(
            modifier = Modifier.fillMaxSize(),
            players = listOf(
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
                    ), canRemove = true, includeInList = false
                ),
                PlayerInfo(
                    player = Player(
                        profileName = "Mpho",
                        uniqueId = "mpho-unique-id",
                        email = "mpho@example.com",
                    ), canRemove = true, includeInList = true
                ),
            ),
            searchQuery = "officiallearningis4fun@gmail.co",
            onNavBackClick = {}
        )
    }
}