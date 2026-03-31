package com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.OnlineOfflineMode.OFFLINE
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.OnlineOfflineMode.ONLINE
import com.learningis4fun.gameshub.presentation.util.GamesHubAppThemePrev
import com.learningis4fun.gameshub.util.GamesHubLogTags

@Composable
fun OnlineAndOfflineModeSwitcher(
    mode: OnlineOfflineMode,
    onModeSwitch: (OnlineOfflineMode) -> Unit = {},
    onlineContent: @Composable () -> Unit = {},
    offlineContent: @Composable () -> Unit = {}
) {

    LaunchedEffect(key1 = mode) {
        Log.d(GamesHubLogTags.CREATE_NEW_GAME_VIEWMODEL, "mode in screen: $mode")
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {

            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (mode == OFFLINE) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                onClick = {
                    onModeSwitch(OFFLINE)
                }) {
                if (mode == OFFLINE) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Default.Check,
                        contentDescription = ""
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "PLAY OFFLINE")
            }

            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (mode == ONLINE) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                onClick = {
                    onModeSwitch(ONLINE)
                }) {
                if (mode == ONLINE) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Default.Check,
                        contentDescription = ""
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "PLAY ONLINE")
            }
        }
        when (mode) {
            ONLINE -> onlineContent()
            OFFLINE -> offlineContent()
        }


    }
}

@Preview
@Composable
private fun OnlineOfflineModeSwitcher() {
    GamesHubAppThemePrev {
        OnlineAndOfflineModeSwitcher(
            mode = OFFLINE
        )
    }
}