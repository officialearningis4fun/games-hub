package com.learningis4fun.gameshub.app.nav

import androidx.annotation.Keep
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.graphics.vector.ImageVector
import com.learningis4fun.gameshub.commons.ui.icons.FriendsIcon
import com.learningis4fun.gameshub.commons.ui.icons.UserIcon
import kotlinx.serialization.Serializable

enum class Destination(
    val route: BottonNavRoute,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    HOME(BottonNavRoute.HomeScreenRoute, "Home", UserIcon, "Home"),
    FRIENDS(BottonNavRoute.FriendsScreenRoute, "Friends", FriendsIcon, "Friends"),
    ACCOUNT(
        BottonNavRoute.AccountScreenRoute,
        "Account",
        Icons.Filled.AccountCircle,
        "Account"
    )
}

@Keep
@Serializable
sealed interface BottonNavRoute {

    @Keep
    @Serializable
    data object HomeScreenRoute : BottonNavRoute

    @Keep
    @Serializable
    data object FriendsScreenRoute : BottonNavRoute

    @Keep
    @Serializable
    data object AccountScreenRoute : BottonNavRoute

}