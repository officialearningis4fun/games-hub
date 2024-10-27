package com.learningis4fun.gameshub.crusade_cards.feature_profile.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.ui.theme.GamesHubAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseNumberOfCardsSingleChoiceDialog(
    openDialog: Boolean,
    checkedOption: Int = Constants.NumberOfCardsOptions.DEFAULT_CARDS_PER_PLAYER,
    onDismissRequest: () -> Unit,
    onValueChosen: (Int) -> Unit = {}
) {

    val options = listOf(
        Constants.NumberOfCardsOptions.DEFAULT_CARDS_PER_PLAYER,
        Constants.NumberOfCardsOptions.MIDDLE_CARDS_PER_PLAYER,
        Constants.NumberOfCardsOptions.HIGHEST_CARDS_PER_PLAYER,
    )

    if (openDialog) {
        BasicAlertDialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties()
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentHeight()
                    .wrapContentWidth()
                    .clip(MaterialTheme.shapes.large),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        item {
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = "Choose Number of cards",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        items(options) {
                            ChooseCardNumberItem(
                                modifier = Modifier.fillMaxWidth(),
                                option = it,
                                checkedOption = checkedOption,
                                onChecked = {
                                    onValueChosen(it)
                                }
                            )
                        }
                    }

                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .padding(end = 16.dp, bottom = 16.dp)
                            .align(Alignment.End)
                    ) { Text("CANCEL") }

                }


            }
        }

    }
}

@Composable
private fun ChooseCardNumberItem(
    modifier: Modifier,
    option: Int,
    checkedOption: Int,
    onChecked: () -> Unit
) {

    Row(
        modifier
            .padding(8.dp)
            .clickable {
                onChecked()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = checkedOption == option, onClick = onChecked)
        Text(text = "$option cards per player")
    }

}

@Preview
@Composable
private fun ChooseNumberOfCardsSingleChoiceDialogLightPrev() {
    GamesHubAppTheme {
        ChooseNumberOfCardsSingleChoiceDialog(
            openDialog = true,
            onDismissRequest = {}
        )
    }
}

@Preview
@Composable
private fun ChooseNumberOfCardsSingleChoiceDialogDarkPrev() {
    GamesHubAppTheme(darkTheme = true) {
        ChooseNumberOfCardsSingleChoiceDialog(
            openDialog = true,
            onDismissRequest = {}
        )
    }
}