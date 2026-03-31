package com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.domain.util.playerName
import com.learningis4fun.gameshub.presentation.util.GamesHubAppThemePrev

@Composable
fun PlayersToolDialog(
    modifier: Modifier = Modifier,
    showDialog: Boolean = false,
    availablePlayers: List<PlayerInfo>,
    maxNumberOfParticipants: Int,
    onDismissRequest: () -> Unit = {},
    addParticipantClick: () -> Unit,
    removeParticipant: (Player) -> Unit
) {
    if (showDialog) {
        NewGameToolBaseDialog(onDismissRequest = onDismissRequest) {
            Column(
                modifier = modifier.background(MaterialTheme.colorScheme.background)
            ) {

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "Participants"
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(
                        availablePlayers,
                        key = { player -> player.player.uniqueId }) { playerInfo ->
                        PlayerItem(
                            modifier = Modifier.animateItem(),
                            playerInfo = playerInfo,
                            onRemove = {
                                removeParticipant(playerInfo.player)
                            }
                        )
                    }
                    if (availablePlayers.size < maxNumberOfParticipants) {
                        item {
                            AddPlayerButton(
                                onClick = addParticipantClick
                            )
                        }
                    }
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
private fun AddPlayerButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    IconButton(
        modifier = modifier
            .border(
                width = 3.dp,
                shape = CircleShape,
                color = Color.LightGray.copy(alpha = 0.8f)
            )
            .size(56.dp),
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier,
            imageVector = Icons.Default.Add,
            contentDescription = "Add player"
        )
    }

}

@Composable
@Preview
private fun CardsToolDialogPrev() {
    GamesHubAppThemePrev {
        PlayersToolDialog(
            availablePlayers = listOf(
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
            },
            maxNumberOfParticipants = 4,
            addParticipantClick = {},
            removeParticipant = {}
        )
    }
}
