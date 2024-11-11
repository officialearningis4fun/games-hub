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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.numberRepresentation
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.playerName
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toDoubleDigits
import com.learningis4fun.gameshub.crusade_cards.commons.ui.theme.GamesHubAppTheme

@Composable
fun CreateGameSetupToolsSection(
    modifier: Modifier = Modifier,
    maxPlayers: Int = Constants.MAX_PARTICIPANTS_IN_CRUSADE_GAME,
    cards: List<Card> = Constants.EliminationLimitNumRepCard.cards,
    createGameButtonText: String = "start",
    createNewGameToolsState: CreateNewGameToolsState = CreateNewGameToolsState(),
    onStartGame: () -> Unit = {},
    onEvent: (CreateNewGameToolsEvents) -> Unit = {}
) {

    var expandTools by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 32.dp)
    ) {

        Row(
            modifier = Modifier
                .clickable {
                    expandTools = !expandTools
                }
                .padding(top = 8.dp, bottom = 8.dp, start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {

            Row(
                modifier = Modifier
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Image(
                        modifier = Modifier
                            .height(24.dp)
                            .padding(2.dp)
                            .aspectRatio(0.7f),
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "",
                    )
                    Text(
                        modifier = Modifier,
                        text = "${createNewGameToolsState.numberOfCardsPerPlayer} Cards".uppercase(),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                VerticalDivider(Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = ""
                    )
                    Text(
                        modifier = Modifier,
                        text = "${createNewGameToolsState.numberOfParticipants} Players".uppercase(),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                VerticalDivider(Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        modifier = Modifier,
                        text = "${createNewGameToolsState.eliminationLimitForCrusade} Limit".uppercase(),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            IconButton(
                modifier = Modifier,
                onClick = { expandTools = !expandTools }
            ) {
                Icon(
                    imageVector = if (expandTools) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = ""
                )
            }
        }

        if (expandTools) {
            ToolsSection(
                modifier = Modifier,
                maxPlayers = maxPlayers,
                cards = cards,
                createNewGameToolsState = createNewGameToolsState,
                onEvent = onEvent
            )
        }

        HorizontalDivider()

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
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
private fun ToolsSection(
    modifier: Modifier = Modifier,
    maxPlayers: Int = Constants.MAX_PARTICIPANTS_IN_CRUSADE_GAME,
    cards: List<Card> = Constants.EliminationLimitNumRepCard.cards,
    createNewGameToolsState: CreateNewGameToolsState,
    onEvent: (CreateNewGameToolsEvents) -> Unit = {}
) {
    Column(
        modifier = modifier.padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            HorizontalDivider()

            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                text = "Number of cards per player"
            )

            LazyRow(
                modifier = Modifier.height(144.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                item {
                    IncreaseDecreaseItem(
                        modifier = Modifier.padding(end = 8.dp),
                        count = createNewGameToolsState.numberOfCardsPerPlayer,
                        onDecrease = {
                            onEvent(CreateNewGameToolsEvents.DecreaseNumberOfCards)
                        },
                        onIncrease = {
                            onEvent(CreateNewGameToolsEvents.IncreaseNumberOfCards)
                        }
                    )
                }
                items((0..5).toList()) {
                    BackCardItem(
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            }

        }

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
                items(createNewGameToolsState.availablePlayers) { playerInfo ->
                    PlayerItem(
                        modifier = Modifier.animateItem(),
                        playerInfo = playerInfo,
                        onRemove = {
                            onEvent(CreateNewGameToolsEvents.RemoveParticipant(playerInfo.player))
                        }
                    )
                }
                if (createNewGameToolsState.availablePlayers.size < maxPlayers) {
                    item {
                        AddPlayerButton(
                            onClick = {
                                onEvent(CreateNewGameToolsEvents.AddParticipant)
                            }
                        )
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
                    IncreaseDecreaseItem(
                        modifier = Modifier.padding(end = 8.dp),
                        count = createNewGameToolsState.eliminationLimitForCrusade,
                        onDecrease = {
                            onEvent(CreateNewGameToolsEvents.DecreaseEliminationLimitForCrusade)
                        },
                        onIncrease = {
                            onEvent(CreateNewGameToolsEvents.IncreaseEliminationLimitForCrusade)
                        }
                    )
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
    playerInfo: PlayerInfo,
    onRemove: () -> Unit = {}
) {
    val imageRes = playerInfo.player.imageUrl.toIntOrNull() ?: R.drawable.avatar_person1

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

                if (playerInfo.canRemove) {
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
        }
        Text(
            text = playerInfo.player.profileName,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun IncreaseDecreaseItem(
    modifier: Modifier = Modifier,
    count: Int,
    onDecrease: () -> Unit = {},
    onIncrease: () -> Unit = {},
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
            onClick = { onIncrease() }
        ) {
            Icon(
                modifier = Modifier
                    .rotate(-90f),
                imageVector = Icons.AutoMirrored.Sharp.ArrowForward,
                contentDescription = "increase"
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
            onClick = { onDecrease() }
        ) {
            Icon(
                modifier = Modifier
                    .rotate(90f),
                imageVector = Icons.AutoMirrored.Sharp.ArrowForward,
                contentDescription = "Increase"
            )
        }
    }

}

@Composable
private fun BackCardItem(
    modifier: Modifier = Modifier,
) {
    val imageRes = R.drawable.back

    Column(
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(0.7f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(0.7f),
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
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 1.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = RectangleShape
                    )
                    .padding(8.dp),
                text = count.toString(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onError,
                style = MaterialTheme.typography.titleMedium
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
        CreateGameSetupToolsSection(
            createNewGameToolsState = CreateNewGameToolsState(
                playersInfo = listOf(
                    Constants.Players.USER,
                    Constants.Players.DEVICE1,
                    Constants.Players.DEVICE2,
                    Constants.Players.DEVICE3
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
        )
    }
}