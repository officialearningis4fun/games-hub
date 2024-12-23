package com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.domain.util.playerName
import com.learningis4fun.gameshub.commons.ui.theme.GamesHubAppTheme

@Composable
fun CreateNewGameWithOrWithoutAIRoot(
    modifier: Modifier = Modifier,
    createNewGameToolsState: CreateNewGameToolsState,
    lazyListScope: LazyListScope? = null
) {

    Column {
        CreateGameSetupToolsSection(
            createNewGameToolsState = createNewGameToolsState
        )



    }
}

@Preview
@Composable
private fun CreateNewGameWithOrWithoutAIRootPrev() {
    GamesHubAppTheme {
        CreateNewGameWithOrWithoutAIRoot(
            createNewGameToolsState = CreateNewGameToolsState(
                playersInfo = listOf(
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
                }
            )
        )
    }
}

