package com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.ui.components.NumberOfCardsOrPlayersInGameSection
import com.learningis4fun.gameshub.crusade_cards.commons.ui.theme.GamesHubAppTheme
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.PlayerItem

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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NumberOfCardsOrPlayersInGameSection(
            count = 5,
            onDecreaseNumber = {},
            onIncreaseNumber = {}
        )

        val players = listOf(
            Constants.Players.USER,
            Constants.Players.DEVICE1,
            Constants.Players.DEVICE2,
            Constants.Players.DEVICE3,
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(players) { player ->
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PlayerItem(
                        modifier = Modifier.size(42.dp),
                        player = player
                    )
                    Text(
                        text = player.profileName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
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