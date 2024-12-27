package com.learningis4fun.gameshub.app

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import com.learningis4fun.gameshub.commons.ui.routes.EditProfileRoute
import com.learningis4fun.gameshub.commons.ui.routes.ProfileGraph
import com.learningis4fun.gameshub.commons.ui.routes.ViewProfile
import com.learningis4fun.gameshub.presentation.profile.editProfileScreenComposable
import com.learningis4fun.gameshub.presentation.profile.profileScreenComposable

fun NavGraphBuilder.profileGraph(
    navController: NavController
) {
    navigation<ProfileGraph>(startDestination = ViewProfile) {
        profileScreenComposable(
            onBackClick = {
                navController.navigateUp()
            },
            onEditProfile = {
                navController.navigate(EditProfileRoute)
            }
        )

        editProfileScreenComposable(
            onBackClick = {
                navController.navigateUp()
            }
        )
    }
}