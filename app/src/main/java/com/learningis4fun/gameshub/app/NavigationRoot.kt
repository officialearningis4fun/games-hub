@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.learningis4fun.gameshub.app

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.learningis4fun.gameshub.commons.ui.routes.CoreNavGraphRoute
import com.learningis4fun.gameshub.commons.ui.routes.StartScreenRoute
import com.learningis4fun.gameshub.presentation.util.GamesHubTransitionScope


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavigationRoot() {
    val navController = rememberNavController()

    SharedTransitionLayout {
        GamesHubTransitionScope(transitionScope = this) {
            NavHost(
                modifier = Modifier,
                navController = navController,
                startDestination = CoreNavGraphRoute
            ) {
                coreGraph(navController = navController)

                profileGraph(navController = navController)

            }
        }
    }

}