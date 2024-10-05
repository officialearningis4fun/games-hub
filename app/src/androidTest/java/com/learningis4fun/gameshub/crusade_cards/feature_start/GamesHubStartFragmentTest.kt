package com.learningis4fun.gameshub.crusade_cards.feature_start

import androidx.test.filters.MediumTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule

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