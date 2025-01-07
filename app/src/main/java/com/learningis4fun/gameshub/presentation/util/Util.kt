package com.learningis4fun.gameshub.presentation.util

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import com.learningis4fun.gameshub.commons.ui.theme.GamesHubAppTheme

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalCustomSharedTransitionScope = compositionLocalOf<SharedTransitionScope> {
    error("Shared Transition scope not provided")
}


val LocalAnimatedContentScope = compositionLocalOf<AnimatedContentScope> {
    error("Animated content scope not provided")
}

val LocalGameDurationProvider = compositionLocalOf<String> {
    error("GameId not provided")
}

@Composable
fun GameDurationProvider(
    gameDuration: String,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalGameDurationProvider provides gameDuration
    ) {
        content()
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun GamesHubTransitionScopeAndAnimatedContentScope(
    transitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAnimatedContentScope provides animatedContentScope,
        LocalCustomSharedTransitionScope provides transitionScope
    ) {
        content()
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun GamesHubTransitionScope(
    transitionScope: SharedTransitionScope,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalCustomSharedTransitionScope provides transitionScope
    ) {
        content()
    }
}


@Composable
fun GamesHubAnimatedContentScope(
    animatedContentScope: AnimatedContentScope,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAnimatedContentScope provides animatedContentScope,
    ) {
        content()
    }
}

fun sharedImageElementKey(playerId: String, gameId: String): String = "image${gameId}${playerId}"

val sharedElementKeyOfOngoingGameSection: String = "ongoingGameKey"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun GamesHubAppThemePrev(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    SharedTransitionLayout {
        AnimatedContent(targetState = true, label = "animated content") {
            it
            GamesHubAppTheme(darkTheme = darkTheme) {
                GamesHubTransitionScopeAndAnimatedContentScope(
                    transitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this
                ) {
                    GameDurationProvider(gameDuration = "00:20") {
                        content()
                    }
                }
            }
        }
    }
}