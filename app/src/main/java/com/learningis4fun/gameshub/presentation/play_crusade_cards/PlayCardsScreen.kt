package com.learningis4fun.gameshub.presentation.play_crusade_cards

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.AnimationEndReason
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.commons.domain.model.Card
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.domain.util.JokerActions
import com.learningis4fun.gameshub.commons.ui.routes.PlayCardsScreenRoute
import com.learningis4fun.gameshub.commons.ui.routes.WinnerScreenRoute
import com.learningis4fun.gameshub.commons.ui.theme.GamesHubAppTheme
import com.learningis4fun.gameshub.commons.ui.theme.green
import com.learningis4fun.gameshub.domain.model.PlayerScreenInfo
import com.learningis4fun.gameshub.domain.model.UserCard
import com.learningis4fun.gameshub.presentation.util.GamesHubAnimatedContentScope
import com.learningis4fun.gameshub.presentation.util.GamesHubAppThemePrev
import com.learningis4fun.gameshub.util.ScaleTransitionDirection
import com.learningis4fun.gameshub.util.scaleIntoContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch


@OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class)
fun NavGraphBuilder.playCardsScreenComposable(
    onProfileClick: () -> Unit,
    onBackClick: () -> Unit,
    navigateToWinner: (WinnerScreenRoute) -> Unit
) {
    composable<PlayCardsScreenRoute>(
        enterTransition = {
            scaleIntoContainer()
        },
        popEnterTransition = {
            scaleIntoContainer(direction = ScaleTransitionDirection.INWARDS)
        }
    ) {

        val playCardsViewModel: PlayCardsViewModel = hiltViewModel()

        val playCardsState by playCardsViewModel.playCardsState.collectAsState()
        val gameDuration by playCardsViewModel.gameDuration.collectAsStateWithLifecycle()

        val animatingCardAndStage by playCardsViewModel.animatingCardAndStage.collectAsStateWithLifecycle()

        BackHandler(
            onBack = {
                playCardsViewModel.onCloseNavigationIconClicked {
                    onBackClick()
                }
            }
        )

        GamesHubAnimatedContentScope(
            animatedContentScope = this@composable
        ) {
            PlayCardsScreen(
                playCardsState = playCardsState,
                animatingCardAndStage = animatingCardAndStage,
                onEvent = playCardsViewModel::onEvent,
                timerSectionContent = {
                    TimerSection(
                        textColor = Color.Black,
                        duration = gameDuration,
                        direction = playCardsState.playingDirection
                    )
                },
                headingTimerContent = {
                    Heading(
                        text = gameDuration,
                        textColor = Color.Black
                    )
                },
                winner = { (player, wonGameId) ->
                    navigateToWinner(
                        WinnerScreenRoute(
                            wonGameId = wonGameId,
                            winnerImageUrl = player.imageUrl,
                            playerId = player.uniqueId
                        )
                    )
                },
                onClose = {
                    playCardsViewModel.onCloseNavigationIconClicked {
                        onBackClick()
                    }
                },
                onProfileClick = {
                    onProfileClick()
                },
                onPauseGame = {
                    playCardsViewModel.onAppPaused()
                },
                onResumeGame = {
                    playCardsViewModel.onAppResumed()
                }
            )

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayCardsScreen(
    modifier: Modifier = Modifier,
    playCardsState: PlayCardsState,
    animatingCardAndStage: AnimatingCardAndStage = AnimatingCardAndStage(),
    testing: Boolean = false,
    onEvent: (PlayCardsEvent) -> Unit = {},
    winner: (Pair<Player, String>) -> Unit = {},
    headingTimerContent: @Composable () -> Unit = {},
    timerSectionContent: @Composable () -> Unit = {},
    onClose: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onPauseGame: () -> Unit = {},
    onResumeGame: () -> Unit = {}
) {

    LaunchedEffect(key1 = playCardsState.wonGame) {
        if (playCardsState.wonGame.hasWinner()) {
            winner(Pair(playCardsState.wonGame.winner, playCardsState.wonGame.gameId))
            println("has winner called")
        }
    }

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
        containerColor = MaterialTheme.colorScheme.primary,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = Color.Black,
                    actionIconContentColor = Color.Black,
                    titleContentColor = Color.Black
                ),
                title = {
                    if (playCardsState.isLoading.not()) {
                        if (playCardsState.playersScreenInfo.size > 2) {
                            timerSectionContent()
                        } else {
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.titleSmall,
                                text = "PLAYERS",
                            )
                        }

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
                    .padding(paddingValues),
                textColor = Color.Black
            )
        } else {
            PlayCardsDataRoot(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                playCardsState = playCardsState,
                animatingCardAndStage = animatingCardAndStage,
                testing = testing,
                onEvent = onEvent,
                headingTimerContent = {
                    headingTimerContent()
                }
            )
        }
    }

}

@Composable
fun PlayCardsDataRoot(
    modifier: Modifier = Modifier,
    playCardsState: PlayCardsState,
    animatingCardAndStage: AnimatingCardAndStage,
    testing: Boolean = false,
    onEvent: (PlayCardsEvent) -> Unit = {},
    headingTimerContent: @Composable () -> Unit = {},
) {

    val animatingCard: UserCard? = animatingCardAndStage.animatingCard

    var playCardAnimDim by remember {
        mutableStateOf(PlayCardAnimDim())
    }

    val animatedOffsetX = remember {
        androidx.compose.animation.core.Animatable(0f)
    }

    val animatedOffsetY = remember {
        androidx.compose.animation.core.Animatable(0f)
    }

    val animatedWidth = remember {
        androidx.compose.animation.core.Animatable(0f)
    }

    val animatedHeight = remember {
        androidx.compose.animation.core.Animatable(0f)
    }

    LaunchedEffect(key1 = animatingCardAndStage.animatingStages) {
        Log.d("PlayCardsScreen", "animating card: ${animatingCard.toString()}")
        if (animatingCard != null && playCardAnimDim != PlayCardAnimDim() && animatingCardAndStage.isAnimating()) {

            animatedOffsetX.snapTo(playCardAnimDim.startOffsetX)
            animatedOffsetY.snapTo(playCardAnimDim.startOffsetY)

            Log.d("PlayCardsScreen", playCardAnimDim.toString())

            animatedWidth.snapTo(playCardAnimDim.startSizeWidth)
            animatedHeight.snapTo(playCardAnimDim.startSizeHeight)

            launch {
                animatedOffsetX.animateTo(
                    playCardAnimDim.targetOffsetX
                )
            }
            launch {
                val result = animatedOffsetY.animateTo(
                    playCardAnimDim.targetOffsetY
                )

                val animatedCard = when (result.endReason) {
                    AnimationEndReason.BoundReached -> {
                        // rest card offset
                        playCardAnimDim = PlayCardAnimDim()

                        animatedOffsetX.snapTo(0f)
                        animatedOffsetY.snapTo(0f)

                        animatedWidth.snapTo(0f)
                        animatedHeight.snapTo(0f)
                        null
                    }

                    AnimationEndReason.Finished -> {
                        playCardAnimDim = PlayCardAnimDim()

                        animatedOffsetX.snapTo(0f)
                        animatedOffsetY.snapTo(0f)

                        animatedWidth.snapTo(0f)
                        animatedHeight.snapTo(0f)
                        null
                    }
                }
                onEvent(PlayCardsEvent.PlayedCard(animatedCard))
            }
            launch {
                animatedWidth.animateTo(
                    playCardAnimDim.targetSizeWidth
                )
            }
            launch {
                animatedHeight.animateTo(
                    playCardAnimDim.targetSizeHeight
                )
            }

        }
    }

    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (playCardsState.playersScreenInfo.size > 2) {
                MultiplePlayersSectionScreen(
                    modifier = Modifier
                        .fillMaxWidth(),
                    textColor = Color.Black,
                    playersScreenInfo = playCardsState.playersScreenInfo,
                )
            } else {
                PlayersSectionScreen(
                    modifier = Modifier
                        .fillMaxWidth(),
                    textColor = Color.Black,
                    playersScreenInfo = playCardsState.playersScreenInfo,
                    headingTimerContent = {
                        headingTimerContent()
                    }
                )
            }

            MessagesSection(
                modifier = Modifier.fillMaxWidth(),
                message = playCardsState.message
            )

            // do not show the card while animating

            PlayedAndDustbinSection(
                playedCards = playCardsState.playedCards.filterNot { it.gameCardId == animatingCard?.gameCardId },
                showAllowOthersPlay = playCardsState.allowOthersPlay,
                showCurrentCardType = playCardsState.showCurrentCardTypeAndImage,
                onDustbinClick = {
                    onEvent(PlayCardsEvent.PickCardFromDustbin)
                },
                onAllowOthersPlay = {
                    onEvent(PlayCardsEvent.AllowOthersPlay)
                },
                targetAnimDim = {
                    playCardAnimDim = playCardAnimDim.copy(
                        target = it
                    )
                }
            )

            UserCardsSection(
                modifier = Modifier.fillMaxWidth(),
                cards = playCardsState.userUnPlayedCards.userCardsWithAnimation(
                    animatingCard = animatingCard,
                    animatingCardStages = animatingCardAndStage.animatingStages
                ),
                error = playCardsState.playCardsErrorState,
                onClick = {
                    onEvent(PlayCardsEvent.PlayCard(it))
                },
                onEvent = onEvent,
                scrollToItemConsumed = {
                    onEvent(PlayCardsEvent.ScrollToItemPositionConsumed)
                },
                startAnimDim = {
                    playCardAnimDim = playCardAnimDim.copy(
                        start = it
                    )
                }
            )
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
        }

        animatingCard?.let {
            val image = it.image.toIntOrNull() ?: R.drawable.card_base
            Image(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            animatedOffsetX.value.toInt(),
                            animatedOffsetY.value.toInt()
                        )
                    }
                    .size(
                        with(LocalDensity.current) { animatedWidth.value.toDp() },
                        with(LocalDensity.current) { animatedHeight.value.toDp() }),
                painter = painterResource(id = image),
                contentDescription = null
            )
        }

        if (playCardsState.showDefineJokerActions) {
            DefineJokerActionScreen(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                onClick = {
                    onEvent(PlayCardsEvent.DefineJokerAction(it))
                }
            )
        }

        if (playCardsState.showChooseCardTypeScreen) {
            ChooseCardTypeScreen(
                modifier = Modifier.align(Alignment.Center),
                onClick = {
                    onEvent(PlayCardsEvent.SetNewCardType(it))
                }
            )
        }
    }
}

@Composable
fun PlayCardsLoadingScreen(
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onPrimary
) {

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Setting up the scene...", color = textColor)
    }

}

@Composable
fun PlayCardsErrorScreen(
    modifier: Modifier = Modifier,
    playCardsErrorState: PlayCardsErrorState,
    dismissible: Boolean = false,
    onEvent: (PlayCardsEvent) -> Unit
) {

    Card(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row {

                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            horizontal = 16.dp,
                            vertical = 16.dp
                        ),
                    text = playCardsErrorState.errorMessage
                )

                if (dismissible) {
                    IconButton(
                        modifier = Modifier,
                        onClick = {
                            onEvent(PlayCardsEvent.DismissErrorClick)
                        }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "dismiss")
                    }
                }

            }

            if (dismissible.not()) {
                Button(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        onEvent(
                            PlayCardsEvent.ErrorButtonClick(
                                errorAction = playCardsErrorState.errorAction
                            )
                        )
                    }) {
                    Text(text = playCardsErrorState.errorButtonText.uppercase())
                }
            }
        }
    }

}

@Composable
private fun PlayedAndDustbinSection(
    modifier: Modifier = Modifier,
    playedCards: List<UserCard>,
    showAllowOthersPlay: Boolean,
    showCurrentCardType: Pair<Boolean, String?>,
    onDustbinClick: () -> Unit,
    onAllowOthersPlay: () -> Unit,
    targetAnimDim: (CardAnimDim) -> Unit = {}
) {

    Box {

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            PlayedCardsSection(
                modifier = Modifier.weight(1f),
                cards = playedCards,
                showCurrentCardType = showCurrentCardType,
                targetAnimDim = targetAnimDim
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
        Heading(
            modifier = Modifier,
            text = "Dustbin",
            textColor = Color.Black
        )

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
                color = Color.Black
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = message, color = Color.Black,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1
        )
    }

}


@Composable
private fun UserCardsSection(
    modifier: Modifier,
    cards: Pair<List<UserCard>, UserCard?>,
    error: PlayCardsErrorState? = null,
    startAnimDim: (CardAnimDim) -> Unit = {},
    onClick: (UserCard) -> Unit,
    onEvent: (PlayCardsEvent) -> Unit,
    scrollToItemConsumed: () -> Unit
) {

    val userCardsState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = cards.second) {
        if (cards.second != null) {
            val indexOfNewUserCard = cards.first.indexOf(cards.second)
            if (indexOfNewUserCard != -1) {
                coroutineScope.launch {
                    userCardsState.animateScrollToItem(indexOfNewUserCard)
                    scrollToItemConsumed()
                }
            }
        }
    }

    Box {
        Column(
            modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Heading(
                modifier = Modifier.padding(top = 16.dp),
                text = "Your Cards",
                textColor = Color.Black
            )

            UserCardsSummary(
                modifier = Modifier
                    .padding(bottom = 16.dp, top = 8.dp),
                cards = cards.first.map { it.toCard() }
            )

            val imageOffsets = remember { mutableStateMapOf<String, Pair<Offset, IntSize>>() }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                state = userCardsState
            ) {
                items(cards.first, key = { it.cardId }) {
                    val image = it.image.toIntOrNull() ?: R.drawable.card_base
                    Image(
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                val updatedStartOffset = coordinates.localToWindow(Offset.Zero)
                                imageOffsets[it.cardId] = Pair(updatedStartOffset, coordinates.size)
                                val offsetMsg =
                                    "imageId: ${it.cardId} offset:${coordinates.localToWindow(Offset.Zero)}"
                                Log.d("PlayCardsScreen", offsetMsg)
                            }
                            .animateItem(fadeInSpec = null, fadeOutSpec = null)
                            .clickable {
                                try {
                                    val cardId = it.cardId
                                    val offsetAndSize = imageOffsets[cardId]!!

                                    val cardAnimDim = CardAnimDim(
                                        size = offsetAndSize.second,
                                        offset = offsetAndSize.first
                                    )

                                    startAnimDim(cardAnimDim)

                                    Log.d("PlayCardsScreen", "card:${it.cardId}-$cardAnimDim")
                                    Log.d("PlayCardsScreen", imageOffsets.toString())
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                } finally {
                                    onClick(it)
                                }
                            }
                            .aspectRatio(0.7f),
                        painter = painterResource(id = image),
                        contentDescription = it.name
                    )
                }
            }
        }

        if (error != null) {
            PlayCardsErrorScreen(
                modifier = Modifier
                    .align(Alignment.TopCenter),
                playCardsErrorState = error,
                onEvent = onEvent
            )
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
            contentDescription = null,
            tint = Color.Black
        )
        Text(text = text, color = Color.Black)
    }
}


@Composable
private fun ChooseCardTypeScreen(
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                    type = Constants.CardTypes.CLUBS,
                    icon = R.drawable.suit_club,
                    buttonColor = Color.Black,
                    onClick = onClick
                )
                ChooseCardTypeItem(
                    type = Constants.CardTypes.DIAMOND,
                    icon = R.drawable.suit_diamond,
                    buttonColor = Color.Red,
                    onClick = onClick
                )
                ChooseCardTypeItem(
                    type = Constants.CardTypes.HEART,
                    icon = R.drawable.suit_heart,
                    buttonColor = Color.Red,
                    onClick = onClick
                )

                ChooseCardTypeItem(
                    type = Constants.CardTypes.SPADE,
                    icon = R.drawable.suit_spade,
                    buttonColor = Color.Black,
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
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
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
                buttonColor = green
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

    Column(
        modifier = modifier,
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
            textAlign = TextAlign.Center,
            color = Color.Black
        )

        Icon(
            modifier = Modifier.size(36.dp),
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color.Black
        )

    }

}

@Composable
fun Heading(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color = Color.White
) {
    Text(
        modifier = modifier.padding(vertical = 8.dp),
        text = text.uppercase(),
        color = textColor
    )
}

@Composable
fun TimerSection(
    modifier: Modifier = Modifier,
    textColor: Color,
    duration: String,
    direction: String = ""
) {

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.titleSmall,
            text = duration,
            color = textColor
        )
        if (direction.isNotEmpty()) {
            val scaleX = if (direction == Constants.CrusadePlayingDirection.CLOCKWISE) 1f else -1f
            Icon(
                modifier = Modifier
                    .scale(
                        scaleX,
                        1f
                    ),
                imageVector = Icons.Default.Refresh,
                contentDescription = ""
            )
        }
    }

}


@Preview
@Composable
fun PlayCardsDefaultScreenLightPrev() {
    GamesHubAppThemePrev {
        PlayCardsScreen(
            modifier = Modifier
                .fillMaxSize(),
            playCardsState = DefaultPlayCardsState.copy(
                isLoading = false,
                error = "Not enough cards to be picked",
                playCardsErrorState = PlayCardsErrorState(
                    errorMessage = "Not enough cards to be picked",
                    dismissible = false,
                    errorButtonText = "Reset game"
                )
            )
        )
    }
}

@Preview
@Composable
fun PlayCardsDefaultScreenDarkPrev() {
    GamesHubAppTheme(darkTheme = true) {
        PlayCardsScreen(
            modifier = Modifier
                .fillMaxSize(),
            playCardsState = DefaultPlayCardsState.copy(
                isLoading = false,
                error = null
            )
        )
    }
}

@Preview
@Composable
fun PlayCardsDefaultScreenWithMultiPlayerLightPrev() {
    GamesHubAppTheme {
        PlayCardsScreen(
            modifier = Modifier
                .fillMaxSize(),
            playCardsState = DefaultPlayCardsState.copy(
                isLoading = false,
                error = null,
                playersScreenInfo = playersScreenInfo,
                playingDirection = Constants.CrusadePlayingDirection.ANTICLOCKWISE
            )
        )
    }
}

@Preview
@Composable
fun PlayCardsDefaultScreenWithMultiPlayerDarkPrev() {
    GamesHubAppTheme(darkTheme = true) {
        PlayCardsScreen(
            modifier = Modifier
                .fillMaxSize(),
            playCardsState = DefaultPlayCardsState.copy(
                isLoading = false,
                error = null,
                playersScreenInfo = playersScreenInfo
            )
        )
    }
}

@Preview
@Composable
fun PlayCardsChangeJokerActionScreenDarkPrev() {

    GamesHubAppTheme(darkTheme = true) {
        PlayCardsScreen(
            modifier = Modifier
                .fillMaxSize(),
            playCardsState = DefaultPlayCardsState.copy(
                showDefineJokerActions = true,
            )
        )
    }
}

@Preview
@Composable
fun PlayCardsChangeJokerActionScreenLightPrev() {

    GamesHubAppTheme {
        PlayCardsScreen(
            modifier = Modifier
                .fillMaxSize(),
            playCardsState = DefaultPlayCardsState.copy(
                showDefineJokerActions = true,
            )
        )
    }
}

@Preview
@Composable
fun PlayCardsChangeCardTypeScreenLightPrev() {
    GamesHubAppTheme {
        PlayCardsScreen(
            modifier = Modifier
                .fillMaxSize(),
            playCardsState = DefaultPlayCardsState.copy(
                showChooseCardTypeScreen = true
            )
        )
    }
}

@Preview
@Composable
fun PlayCardsChangeCardTypeScreenDarkPrev() {
    GamesHubAppTheme(darkTheme = true) {
        PlayCardsScreen(
            modifier = Modifier
                .fillMaxSize(),
            playCardsState = DefaultPlayCardsState.copy(
                showChooseCardTypeScreen = true
            )
        )
    }
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
        ),
        PlayerScreenInfo(
            "id3",
            "OpenAI",
            "",
            R.drawable.openai_white_monoblossom,
            "4",
            true,
        )
    ),
    showCurrentCardType = Pair(true, Constants.CardTypes.DIAMOND),
    showDefineJokerActions = false,
    showChooseCardTypeScreen = false,
    allowOthersPlay = true,
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
    userUnPlayedCards = Pair(
        Constants.Cards.cards.take(10).mapIndexed { index, card ->
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
        null
    ),
    isLoading = false,
    error = null
)

private val playersScreenInfo = listOf(
    PlayerScreenInfo(
        "id1",
        "You",
        "",
        R.drawable.game_test_profile,
        "7",
        false,
    ),
    PlayerScreenInfo(
        "id2",
        "Dev1",
        "",
        R.drawable.game_test_profile,
        "4",
        false,
    ),
    PlayerScreenInfo(
        "id3",
        "Dev2",
        "",
        R.drawable.game_test_profile,
        "4",
        true,
    )
)