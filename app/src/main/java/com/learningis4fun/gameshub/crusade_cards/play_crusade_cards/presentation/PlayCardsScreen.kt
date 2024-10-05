package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.JokerActions
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.PlayCardsScreenRoute
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.PlayerScreenInfo
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.UserCard
import com.learningis4fun.gameshub.util.ScaleTransitionDirection
import com.learningis4fun.gameshub.util.scaleIntoContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi


@OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class)
fun NavGraphBuilder.playCardsScreenComposable(
    onProfileClick: () -> Unit,
    onBackClick: () -> Unit,
    navigateToWinner: (String) -> Unit
) {
    composable<PlayCardsScreenRoute>(
        enterTransition = {
            scaleIntoContainer()
        },
        popEnterTransition = {
            scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS)
        }
    ) {

        val playCardsViewModel: PlayCardsViewModel = hiltViewModel()

        val playCardsState by playCardsViewModel.playCardsState.collectAsState()
        val gameDuration by playCardsViewModel.gameDuration.collectAsState()

        BackHandler(
            onBack = {
                playCardsViewModel.onCloseNavigationIconClicked {
                    onBackClick()
                }
            }
        )

        PlayCardsScreen(
            playCardsState = playCardsState,
            gameDuration = gameDuration,
            onEvent = playCardsViewModel::onEvent,
            winner = { (_, wonGameId) ->
                navigateToWinner(wonGameId)
            },
            onClose = {
                playCardsViewModel.onCloseNavigationIconClicked {
                    onBackClick()
                }
            },
            onProfileClick = {
                onProfileClick()
            },
            onResumeGame = {
                playCardsViewModel.onAppResumed()
            },
            onPauseGame = {
                playCardsViewModel.onAppPaused()
            }

        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayCardsScreen(
    modifier: Modifier = Modifier,
    playCardsState: PlayCardsState,
    gameDuration: String,
    testing: Boolean = false,
    onEvent: (PlayCardsEvent) -> Unit = {},
    winner: (Pair<Player, String>) -> Unit = {},
    onClose: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onPauseGame: () -> Unit = {},
    onResumeGame: () -> Unit = {}
) {

    LaunchedEffect(key1 = playCardsState.wonGame) {
        if (playCardsState.wonGame.gameId.isNotEmpty()) {
            winner(Pair(playCardsState.wonGame.winner, playCardsState.wonGame.gameId))
            println("has winner called")
        }
    }
    val backgroundColor = Color(0xffff8800)

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    val currentOnPause by rememberUpdatedState(newValue = onPauseGame)
    val currentOnResume by rememberUpdatedState(newValue = onResumeGame)

    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentOnResume()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                currentOnPause()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = {

            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    titleContentColor = Color.White
                ),
                title = {
                    if (playCardsState.isLoading.not() && playCardsState.error == null) {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.titleSmall,
                            text = "PLAYERS",
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (playCardsState.isLoading) {
            PlayCardsLoadingScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else if (playCardsState.error != null) {
            PlayCardsErrorScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                error = playCardsState.error
            )
        } else {
            PlayCardsDataRoot(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                playCardsState = playCardsState,
                gameDuration = gameDuration,
                testing = testing,
                onEvent = onEvent
            )
        }
    }

}

@Composable
fun PlayCardsDataRoot(
    modifier: Modifier = Modifier,
    playCardsState: PlayCardsState,
    gameDuration: String,
    testing: Boolean = false,
    onEvent: (PlayCardsEvent) -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PlayersSectionScreen(
                modifier = Modifier
                    .fillMaxWidth(),
                playersScreenInfo = playCardsState.playersScreenInfo,
                duration = gameDuration
            )

            MessagesSection(
                modifier = Modifier.fillMaxWidth(),
                message = playCardsState.message
            )

            PlayedAndDustbinSection(
                playedCards = playCardsState.playedCards,
                showAllowOthersPlay = playCardsState.allowOthersPlay,
                showCurrentCardType = playCardsState.showCurrentCardTypeAndImage,
                onDustbinClick = {
                    onEvent(PlayCardsEvent.PickCardFromDustbin)
                }
            ) {
                onEvent(PlayCardsEvent.AllowOthersPlay)
            }

            UserCardsSection(
                modifier = Modifier.fillMaxWidth(),
                cards = playCardsState.userUnPlayedCards,
                onClick = {
                    onEvent(PlayCardsEvent.PlayCard(it))
                }
            )
        }

        if (playCardsState.showDefineJokerActions) {
            DefineJokerActionScreen(
                modifier = Modifier.padding(end = 16.dp),
                onClick = {
                    onEvent(PlayCardsEvent.DefineJokerAction(it))
                }
            )
        }

        if (playCardsState.showChooseCardTypeScreen) {
            ChooseCardTypeScreen(
                modifier = Modifier,
                onClick = {
                    onEvent(PlayCardsEvent.SetNewCardType(it))
                }
            )
        }
    }
}

@Composable
fun PlayCardsLoadingScreen(
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Setting up the scene...")
    }

}

@Composable
fun PlayCardsErrorScreen(
    modifier: Modifier = Modifier,
    error: String
) {

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(text = error)
    }

}

@Composable
fun PlayersSectionScreen(
    modifier: Modifier = Modifier,
    playersScreenInfo: List<PlayerScreenInfo>,
    duration: String
) {

    val playerImgHeight = 64.dp

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val player1 = if (playersScreenInfo.isNotEmpty()) playersScreenInfo[0] else null

        val player2 = if (playersScreenInfo.size >= 2) playersScreenInfo[1] else null

        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (player2 == null || player1 == null) {
                Spacer(modifier = Modifier.height(playerImgHeight))
            } else {

                PlayerItem(
                    modifier = Modifier,
                    playerScreenInfo = player1
                )
                Heading(modifier = Modifier, text = duration)

                PlayerItem(
                    modifier = Modifier,
                    playerScreenInfo = player2
                )
            }

        }
    }

}

@Composable
private fun PlayerItem(
    modifier: Modifier,
    playerScreenInfo: PlayerScreenInfo
) {

    val image = playerScreenInfo.imageUrl?.toIntOrNull() ?: playerScreenInfo.defaultAvatar

    val isPlaying = playerScreenInfo.isCurrentlyPlaying

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Card(
            modifier = Modifier,
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
            ) {

                Image(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(
                            width = 3.dp,
                            shape = CircleShape,
                            color = if (isPlaying) Color.White else Color.LightGray.copy(alpha = 0.8f)
                        ),
                    contentScale = ContentScale.Crop,
                    painter = painterResource(id = image),
                    contentDescription = null
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center),
                        text = playerScreenInfo.numOfCardsLeft,
                        color = MaterialTheme.colorScheme.onError,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                }

            }
        }

        Text(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp),
            text = playerScreenInfo.playerName,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
    }


}

@Composable
private fun PlayedAndDustbinSection(
    modifier: Modifier = Modifier,
    playedCards: List<UserCard>,
    showAllowOthersPlay: Boolean,
    showCurrentCardType: Pair<Boolean, String?>,
    onDustbinClick: () -> Unit,
    onAllowOthersPlay: () -> Unit
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        PlayedCardsSection(
            modifier = Modifier.weight(1f),
            cards = playedCards,
            showCurrentCardType = showCurrentCardType
        )

        Spacer(modifier = Modifier.weight(0.6f))

        DustbinSection(
            modifier = Modifier.weight(1f),
            onDustbinClick = onDustbinClick,
            onAllowOthersPlay = onAllowOthersPlay,
            showAllowOthersPlay = showAllowOthersPlay
        )
    }

}

@Composable
private fun CurrentCardTypeScreen(
    modifier: Modifier,
    showCurrentCardType: Pair<Boolean, String?>
) {

    showCurrentCardType.second?.let {
        val show = showCurrentCardType.first
        if (show) {
            val cardTypeImage = it.toIntOrNull()
            cardTypeImage?.let {
                Column(
                    modifier = modifier
                        .clip(RoundedCornerShape(4.dp))
                        .fillMaxWidth()
                        .aspectRatio(0.7f)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .background(Color.White.copy(alpha = 0.7f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Image(
                        modifier = Modifier.size(48.dp),
                        painter = painterResource(id = cardTypeImage),
                        contentDescription = null
                    )

                }
            }
        }
    }


}

@Composable
private fun DustbinSection(
    modifier: Modifier,
    showAllowOthersPlay: Boolean,
    onDustbinClick: () -> Unit,
    onAllowOthersPlay: () -> Unit
) {

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Heading(modifier = Modifier, text = "Dustbin")

        Box {
            Image(
                modifier = Modifier
                    .clickable { onDustbinClick() }
                    .fillMaxWidth()
                    .aspectRatio(0.7f),
                painter = painterResource(id = R.drawable.back),
                contentDescription = null
            )
            if (showAllowOthersPlay) {
                AllowOthersPlayScreen(
                    onClick = onAllowOthersPlay
                )
            }
        }
    }

}

@Composable
private fun MessagesSection(
    modifier: Modifier,
    message: String
) {

    Box(
        modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(
                width = 1.dp,
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = message, color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1
        )
    }

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UserCardsSection(
    modifier: Modifier,
    cards: List<UserCard>,
    onClick: (UserCard) -> Unit
) {

    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Heading(modifier = Modifier.padding(top = 16.dp), text = "Your Cards")

        UserCardsSummary(
            modifier = Modifier
                .padding(bottom = 16.dp, top = 8.dp),
            cards = cards.map { it.toCard() }
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(cards, key = { it.cardId }) {
                val image = it.image.toIntOrNull() ?: R.drawable.card_base
                Image(
                    modifier = Modifier
                        .animateItemPlacement()
                        .clickable { onClick(it) }
                        .aspectRatio(0.7f),
                    painter = painterResource(id = image),
                    contentDescription = it.name
                )
            }
        }
    }

}

@Composable
fun UserCardsSummary(
    modifier: Modifier,
    cards: List<Card>
) {

    val spade = cards.count { it.type == Constants.CardTypes.SPADE }
    val club = cards.count { it.type == Constants.CardTypes.CLUBS }
    val heart = cards.count { it.type == Constants.CardTypes.HEART }
    val diamond = cards.count { it.type == Constants.CardTypes.DIAMOND }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        UserCardsSummaryItem(
            text = club.toString(),
            icon = R.drawable.suit_club
        )
        UserCardsSummaryItem(
            text = diamond.toString(),
            icon = R.drawable.suit_diamond
        )
        UserCardsSummaryItem(
            text = heart.toString(),
            icon = R.drawable.suit_heart
        )
        UserCardsSummaryItem(
            text = spade.toString(),
            icon = R.drawable.suit_spade
        )
    }
}

@Composable
fun UserCardsSummaryItem(
    modifier: Modifier = Modifier,
    text: String,
    icon: Int
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = icon),
            contentDescription = null
        )
        Text(text = text)
    }
}


@Composable
private fun ChooseCardTypeScreen(
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit
) {

    Box {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChooseCardTypeItem(
                    type = Constants.CardTypes.SPADE,
                    icon = R.drawable.suit_spade,
                    buttonColor = Color.Black,
                    onClick = onClick
                )
                ChooseCardTypeItem(
                    type = Constants.CardTypes.CLUBS,
                    icon = R.drawable.suit_club,
                    buttonColor = Color.Black,
                    onClick = onClick
                )
                ChooseCardTypeItem(
                    type = Constants.CardTypes.HEART,
                    icon = R.drawable.suit_heart,
                    buttonColor = Color.Red,
                    onClick = onClick
                )
                ChooseCardTypeItem(
                    type = Constants.CardTypes.DIAMOND,
                    icon = R.drawable.suit_diamond,
                    buttonColor = Color.Red,
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
private fun ChooseCardTypeItem(
    type: String,
    icon: Int,
    buttonColor: Color,
    onClick: (String) -> Unit
) {

    Image(
        modifier = Modifier
            .clip(RectangleShape)
            .size(56.dp)
            .aspectRatio(1f)
            .clickable { onClick(type) }
            .border(color = Color.LightGray, width = 1.dp)
            .padding(8.dp),
        painter = painterResource(id = icon),
        contentDescription = "spade",
        colorFilter = ColorFilter.tint(buttonColor)
    )
}

@Composable
private fun DefineJokerActionScreen(
    modifier: Modifier,
    onClick: (JokerActions) -> Unit
) {
    Box {
        Column(
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            JokerActionButton(
                modifier = Modifier,
                onClick = {
                    onClick(JokerActions.Hit)
                },
                text = "Hit",
                imageResource = R.drawable.ic_baseline_fire,
                buttonColor = Color.Red
            )

            JokerActionButton(
                modifier = Modifier,
                onClick = {
                    onClick(JokerActions.ChangeType)
                },
                text = "Change",
                imageResource = R.drawable.ic_baseline_swap,
                buttonColor = Color.Green
            )
        }
    }
}

@Composable
private fun JokerActionButton(
    modifier: Modifier,
    onClick: () -> Unit,
    text: String,
    imageResource: Int,
    buttonColor: Color
) {
    Box {
        Column(
            modifier = modifier
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier
                    .clip(RectangleShape)
                    .size(56.dp)
                    .aspectRatio(1f)
                    .clickable { onClick() }
                    .border(color = Color.LightGray, width = 1.dp)
                    .padding(8.dp),
                painter = painterResource(id = imageResource),
                contentDescription = "spade",
                colorFilter = ColorFilter.tint(buttonColor)
            )
            Heading(
                modifier = Modifier,
                text = text,
                textColor = Color.Black
            )
        }
    }
}

@Composable
private fun AllowOthersPlayScreen(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Column(
        modifier = modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(4.dp))
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = "Allow others play",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Icon(
            modifier = Modifier.size(36.dp),
            imageVector = Icons.Default.Check,
            contentDescription = null
        )

    }

}

@Composable
fun Heading(
    modifier: Modifier,
    text: String,
    textColor: Color = Color.White
) {
    Text(
        modifier = modifier.padding(vertical = 8.dp),
        text = text.uppercase(),
        color = textColor
    )
}


@Preview
@Composable
fun PlayCardsDefaultScreenPrev() {
    val backgroundColor = Color(0xffff8800)
    PlayCardsScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        testing = true,
        gameDuration = "00:01",
        playCardsState = DefaultPlayCardsState.copy(
            isLoading = false,
            error = null
        )
    )
}

@Preview
@Composable
fun PlayCardsLoadingScreenPrev() {
    val backgroundColor = Color(0xffff8800)
    PlayCardsScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        testing = true,
        gameDuration = "00:01",
        playCardsState = PlayCardsState()
    )
}

@Preview
@Composable
fun PlayCardsErrorScreenPrev() {
    val backgroundColor = Color(0xffff8800)
    PlayCardsScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        testing = true,
        gameDuration = "00:01",
        playCardsState = PlayCardsState(error = "Unexpected error occurred", isLoading = false)
    )
}

@Preview
@Composable
fun PlayCardsChangeJokerActionScreenPrev() {
    val backgroundColor = Color(0xffff8800)
    PlayCardsScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        testing = true,
        gameDuration = "00:01",
        playCardsState = DefaultPlayCardsState.copy(
            showDefineJokerActions = true,
        )
    )
}

@Preview
@Composable
fun PlayCardsChangeCardTypeScreenPrev() {
    val backgroundColor = Color(0xffff8800)
    PlayCardsScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        testing = true,
        gameDuration = "00:01",
        playCardsState = DefaultPlayCardsState.copy(
            showChooseCardTypeScreen = true
        )
    )
}


private val DefaultPlayCardsState = PlayCardsState(
    playersScreenInfo = listOf(
        PlayerScreenInfo(
            "id1",
            "Hlompho",
            "",
            R.drawable.game_test_profile,
            "7",
            false,
        ),
        PlayerScreenInfo(
            "id2",
            "Android",
            "",
            R.drawable.game_test_profile,
            "4",
            true,
        )
    ),
    showCurrentCardType = Pair(true, Constants.CardTypes.DIAMOND),
    showDefineJokerActions = false,
    showChooseCardTypeScreen = false,
    playedCards = listOf(
        UserCard(
            gameCardId = "1",
            playerId = "id2",
            cardId = "1d1",
            name = "",
            type = "",
            gameId = "id",
            isPlayed = true,
            image = R.drawable.diamond_king.toString(),
            playTime = 0L,
            pickedTime = 0L
        ),
        UserCard(
            gameCardId = "1",
            playerId = "id2",
            cardId = "1d2",
            name = "",
            type = "",
            gameId = "id",
            isPlayed = true,
            image = R.drawable.diamond_2.toString(),
            playTime = 0L,
            pickedTime = 0L
        ),
        UserCard(
            gameCardId = "1",
            playerId = "id2",
            cardId = "1d3",
            name = "",
            type = "",
            gameId = "id",
            isPlayed = true,
            image = R.drawable.diamond_3.toString(),
            playTime = 0L,
            pickedTime = 0L
        )
    ),
    message = "We getting the game ready for you...",
    userUnPlayedCards = Constants.Cards.cards.take(10).mapIndexed { index, card ->
        val cardId = "$index"
        UserCard(
            gameCardId = "game",
            playerId = "id2",
            cardId = cardId,
            name = card.name,
            type = card.type,
            gameId = "id",
            isPlayed = true,
            image = card.image,
            playTime = 0L,
            pickedTime = 0L
        )
    },
    isLoading = false,
    error = null
)

