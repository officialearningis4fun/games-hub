package com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.learningis4fun.gameshub.presentation.util.GamesHubAppThemePrev

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGameToolBaseDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {

    BasicAlertDialog(onDismissRequest = { onDismissRequest() }) {
        Surface(
            modifier = modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
                TextButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.End),
                    onClick = { onDismissRequest() }) {
                    Text(text = "Close")
                }

            }
        }
    }
}

@Composable
@Preview
private fun CardsToolDialogPrev() {
    GamesHubAppThemePrev {
        NewGameToolBaseDialog(
            onDismissRequest = {},
            content = {}
        )
    }
}