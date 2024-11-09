package com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.automirrored.sharp.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.numberRepresentation
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toDoubleDigits
import com.learningis4fun.gameshub.crusade_cards.commons.ui.components.NumberOfCardsOrPlayersInGameSection
import com.learningis4fun.gameshub.crusade_cards.commons.ui.theme.GamesHubAppTheme

@Composable
fun CreateNewGameToolsSection(
    modifier: Modifier = Modifier,
    createGameButtonText: String = "Start",
    createNewGameToolsState: CreateNewGameToolsState,
    onStartGame: () -> Unit = {},
    onEvent: (CreateNewGameToolsEvents) -> Unit = {}
) {

    val pages = listOf(
        Pair("Number of cards per player", createNewGameToolsState.numberOfCardsPerPlayer),
        Pair("Number of participants including you", createNewGameToolsState.numberOfParticipants),
        Pair("Elimination limit for losers", createNewGameToolsState.eliminationLimitForCrusade)
    )

    val pagerState = rememberPagerState {
        pages.size
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 16.dp),
    ) {

        HorizontalDivider()

        HorizontalPager(
            modifier = Modifier,
            state = pagerState
        ) {
            val page = pages[pagerState.currentPage]

            NumberOfCardsOrPlayersInGameSection(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth(),
                count = page.second,
                title = page.first,
                onDecreaseNumber = {
                    when (pagerState.currentPage) {
                        0 -> {
                            onEvent(CreateNewGameToolsEvents.DecreaseNumberOfCards)
                        }

                        1 -> {
                            onEvent(CreateNewGameToolsEvents.DecreaseNumberOfParticipants)
                        }

                        else -> {
                            onEvent(CreateNewGameToolsEvents.DecreaseEliminationLimitForCrusade)
                        }
                    }
                },
                onIncreaseNumber = {
                    when (pagerState.currentPage) {
                        0 -> {
                            onEvent(CreateNewGameToolsEvents.IncreaseNumberOfCards)
                        }

                        1 -> {
                            onEvent(CreateNewGameToolsEvents.IncreaseNumberOfParticipants)
                        }

                        else -> {
                            onEvent(CreateNewGameToolsEvents.IncreaseEliminationLimitForCrusade)
                        }
                    }
                }
            )

        }

        val selectedColor = MaterialTheme.colorScheme.onBackground
        val unselectedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            pages.forEachIndexed { index, _ ->
                Spacer(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (pagerState.currentPage == index) selectedColor else unselectedColor)
                )
            }
        }

        Spacer(
            modifier = Modifier
                .height(16.dp)
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
            Text(text = createGameButtonText.uppercase())
        }
    }

}

@Composable
fun CreateGameSetupToolsSection(
    modifier: Modifier = Modifier,
    maxPlayers: Int = Constants.MAX_PARTICIPANTS_IN_CRUSADE_GAME,
    cards: List<Card> = Constants.EliminationLimitNumRepCard.cards
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        NumberOfCardsOrPlayersInGameSection(
            modifier = Modifier.padding(horizontal = 16.dp),
            count = 5,
            onDecreaseNumber = {},
            onIncreaseNumber = {}
        )

        val players = listOf(
            Constants.Players.USER,
            Constants.Players.DEVICE1,
            Constants.Players.DEVICE2,
        )

        Column {
            HorizontalDivider()
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                text = "Number of participants"
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(players) { player ->
                    PlayerItem(player = player)
                }
                if (players.size < maxPlayers) {
                    item {
                        AddPlayerButton()
                    }
                }
            }
        }

        Column {
            HorizontalDivider()
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                text = "Elimination limit for losers"
            )

            LazyRow(
                modifier = Modifier.height(144.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                item {
                    IncreaseDecreaseLimitCardItem(count = 6)
                }
                items(cards) { card ->
                    CardItem(
                        modifier = Modifier.fillMaxHeight(),
                        card = card,
                        count = card.numberRepresentation()
                    )
                }
            }


        }
    }
}

@Composable
private fun PlayerItem(
    modifier: Modifier = Modifier,
    player: Player,
    onRemove: () -> Unit = {}
) {
    val imageRes = player.imageUrl.toIntOrNull() ?: R.drawable.avatar_person1

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(
                            width = 3.dp,
                            shape = CircleShape,
                            color = Color.LightGray.copy(alpha = 0.8f)
                        ),
                    contentScale = ContentScale.Crop,
                    painter = painterResource(id = imageRes),
                    contentDescription = null
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onRemove() }
                        .padding(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Remove player"
                    )
                }

            }
        }
        Text(
            text = player.profileName,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun IncreaseDecreaseLimitCardItem(
    modifier: Modifier = Modifier,
    count: Int
) {
    Column(
        modifier = modifier
            .border(
                width = 1.dp,
                MaterialTheme.colorScheme.onBackground,
                shape = MaterialTheme.shapes.medium
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            modifier = Modifier,
            onClick = {}
        ) {
            Icon(
                modifier = Modifier
                    .rotate(90f),
                imageVector = Icons.AutoMirrored.Sharp.ArrowForward,
                contentDescription = "decrease"
            )
        }

        HorizontalDivider(
            modifier = Modifier.width(24.dp)
        )

        Text(
            modifier = Modifier.padding(vertical = 8.dp),
            text = count.toDoubleDigits(),
            style = MaterialTheme.typography.titleLarge
        )

        HorizontalDivider(
            modifier = Modifier.width(24.dp)
        )

        IconButton(
            modifier = Modifier,
            onClick = {}
        ) {
            Icon(
                modifier = Modifier
                    .rotate(90f),
                imageVector = Icons.AutoMirrored.Sharp.ArrowBack,
                contentDescription = "Increase"
            )
        }
    }

}

@Composable
private fun CardItem(
    modifier: Modifier = Modifier,
    card: Card,
    count: Int
) {
    val imageRes = card.image.toIntOrNull() ?: R.drawable.card_base

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
        ) {
            Card(
                modifier = Modifier,
                shape = RoundedCornerShape(4.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(0.7f),
                    contentScale = ContentScale.Crop,
                    painter = painterResource(id = imageRes),
                    contentDescription = null
                )

            }
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .padding(16.dp),
                text = count.toString(),
                style = MaterialTheme.typography.bodyMedium
            )
        }

    }

}

@Composable
private fun AddPlayerButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        IconButton(
            modifier = Modifier
                .size(56.dp)
                .border(
                    width = 3.dp,
                    shape = CircleShape,
                    color = Color.LightGray.copy(alpha = 0.8f)
                ),
            onClick = onClick
        ) {
            Icon(
                modifier = Modifier.fillMaxSize(),
                imageVector = Icons.Default.Add,
                contentDescription = "Add player"
            )
        }
    }
}

@Preview
@Composable
private fun CreateGameSetupTools() {
    GamesHubAppTheme {
        CreateGameSetupToolsSection()
    }
}

@Preview
@Composable
private fun CreateNewGameToolsSectionLightPrev() {
    GamesHubAppTheme {
        CreateNewGameToolsSection(
            createNewGameToolsState = CreateNewGameToolsState()
        )
    }
}


@Preview
@Composable
private fun CreateNewGameToolsSectionDarkPrev() {
    GamesHubAppTheme(darkTheme = true) {
        CreateNewGameToolsSection(
            createNewGameToolsState = CreateNewGameToolsState()
        )
    }
}