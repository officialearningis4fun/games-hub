package com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.ui.theme.GamesHubAppTheme
import com.learningis4fun.gameshub.presentation.start.Heading

@Composable
fun CreateAIGameSetupToolsSection(
    modifier: Modifier = Modifier,
    createGameButtonText: String = "start",
    newGameWithAIState: NewGameWithAIState = NewGameWithAIState(),
    onStartGame: () -> Unit = {}
) {

    if (newGameWithAIState.players.size >= 2) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Heading(
                text = "Play With AI",
                textColor = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                newGameWithAIState.players.forEachIndexed { index, player ->

                    if (index != 0) {
                        Heading(text = "VS", textColor = MaterialTheme.colorScheme.onBackground)
                    }

                    PlayerItem(
                        player = player
                    )
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth(),
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

}

@Composable
private fun PlayerItem(
    modifier: Modifier = Modifier,
    player: Player,
) {
    val imageRes = player.imageUrl.toIntOrNull() ?: R.drawable.avatar_person1
    val isDarkTheme = isSystemInDarkTheme()
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
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                        .border(
                            width = 3.dp,
                            shape = CircleShape,
                            color = Color.LightGray.copy(alpha = 0.8f)
                        ),
                    contentScale = ContentScale.Crop,
                    painter = painterResource(id = imageRes),
                    contentDescription = null
                )

            }
        }
        Text(
            text = player.profileName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Preview
@Composable
private fun CreateGameSetupTools() {
    GamesHubAppTheme {
        CreateAIGameSetupToolsSection(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background),
            newGameWithAIState = NewGameWithAIState(
                players = listOf(
                    Constants.Players.USER,
                    Constants.Players.OPENAI
                )
            )
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreateGameSetupToolsDark() {
    GamesHubAppTheme {
        CreateAIGameSetupToolsSection(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background),
            newGameWithAIState = NewGameWithAIState(
                players = listOf(
                    Constants.Players.USER,
                    Constants.Players.OPENAI
                )
            )
        )
    }
}