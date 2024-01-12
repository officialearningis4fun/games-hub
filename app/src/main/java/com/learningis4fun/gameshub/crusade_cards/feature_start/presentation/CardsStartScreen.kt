package com.learningis4fun.gameshub.crusade_cards.feature_start.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.data.data_source.relations.GameWithPlayers
import com.learningis4fun.gameshub.crusade_cards.feature_start.domain.model.CompletedGame

@Composable
fun CardsStartScreen(
    modifier: Modifier,
    cardsStartState: CardsStartState,
    onStartClick: () -> Unit = {},
    onResumeClick: () -> Unit = {},
) {

    val completedGamesState = cardsStartState.completedGamesState

    val cardHeight = 96.dp

    Column(modifier) {

        CompletedGamesSection(
            completedGamesState = completedGamesState
        )

        Divider()

        if (cardsStartState.showOngoingGame) {
            Heading(
                text = "Ongoing games",
                modifier = Modifier.padding(16.dp),
                textColor = Color.Black
            )

            OngoingGameSection(
                modifier = Modifier.fillMaxWidth(),
                cardHeight = cardHeight,
                ongoingGameState = cardsStartState.ongoingGameState,
                onResumeClick = onResumeClick
            )
        } else {
            CreateGameSection(
                modifier = Modifier.fillMaxWidth(),
                cardHeight = cardHeight,
                onStartClick = onStartClick
            )
        }
    }

}


@Preview
@Composable
private fun CardsStartScreenPrev() {
    CardsStartScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        cardsStartState = CardsStartState(
            completedGamesState = CompletedGamesState(
                completedGames = listOf(
                    CompletedGame(
                        game = Game(id = "", name = "Game 01", winnerId = "id"),
                        players = listOf(
                            Player("id", profileName = "Hlompho"),
                            Player("id2", profileName = "Android"),
                        )
                    ),
                    CompletedGame(
                        game = Game(id = "", name = "Game 01", winnerId = "id"),
                        players = listOf(
                            Player("id", profileName = "Hlompho"),
                            Player("id2", profileName = "Android"),
                        )
                    )
                )
            ),
            ongoingGameState = OngoingGameState(
                threeCards = Constants
                    .Cards
                    .cards
                    .take(3).mapIndexed { index, card ->
                        card.copy(
                            id = "$index"
                        )
                    },
                gameWithPlayers = GameWithPlayers(
                    game = Game(),
                    players = listOf(
                        Player(
                            uniqueId = "id1",
                            profileName = "Android"
                        ),
                        Player(
                            uniqueId = "id2",
                            profileName = "Hlompho"
                        )
                    )
                )
            )
        )
    )
}