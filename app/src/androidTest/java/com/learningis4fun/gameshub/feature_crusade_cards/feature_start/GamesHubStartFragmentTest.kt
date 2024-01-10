package com.learningis4fun.gameshub.feature_crusade_cards.feature_start

import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

@ExperimentalCoroutinesApi
@MediumTest
@HiltAndroidTest
class GamesHubStartFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }

   /* @Test
    fun clickOnStartCardsGameButton_navigateToPlayCardsFragment() {
         val navController = mock(NavController::class.java)
        launchFragmentInHiltContainer<GamesHubStartFragment> {
            Navigation.setViewNavController(requireView(), navController)
        }

        onView(withId(R.id.startGame)).perform(click())
        verify(navController).navigate(
            GamesHubStartFragmentDirections.actionGamesHubStartFragmentToPlayCardsFragment()
        )
    }*/
}