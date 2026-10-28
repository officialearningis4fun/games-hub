package com.learningis4fun.gameshub.app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.learningis4fun.gameshub.api.CrusadeGameService
import com.learningis4fun.gameshub.app.nav.MainNavigationWithBottonBar
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.commons.ui.theme.GamesHubAppTheme
import com.learningis4fun.gameshub.data.data_source.remote.GoogleAuthStages
import com.learningis4fun.gameshub.data.data_source.remote.GoogleAuthUiClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var analytics: FirebaseAnalytics

    @Inject
    lateinit var googleAuthUiClient: GoogleAuthUiClient

    @Inject
    lateinit var crusadeGameService: CrusadeGameService

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val scope = CoroutineScope(Dispatchers.Default)

        setContent {
            GamesHubAppTheme {
                MainNavigationWithBottonBar(
                    navController = rememberNavController(),
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    signInRequest = {

                        scope.launch {
                            googleAuthUiClient.googleAuthStageChanged(
                                stage = GoogleAuthStages.Started
                            )
                            val userResponse = googleAuthUiClient
                                .requestUserOrSignInWithGoogle(this@MainActivity)
                            when (userResponse) {
                                is Response.Error<*> -> {
                                    Log.e("MainActivity", "signIn: ${userResponse.message}")
                                }

                                is Response.Success<*> -> {
                                    when (val saveAccountResponse =
                                        crusadeGameService.saveAccount()) {
                                        is Response.Error<*> -> {
                                            Log.e(
                                                "MainActivity",
                                                "signIn: ${saveAccountResponse.message}"
                                            )
                                        }

                                        is Response.Success<*> -> {}
                                    }
                                }
                            }
                            googleAuthUiClient.googleAuthStageChanged(
                                stage = GoogleAuthStages.Complete
                            )
                        }
                    }
                )
            }

        }

        val backgroundScope = CoroutineScope(Dispatchers.IO)

        backgroundScope.launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@MainActivity) {}
        }

        // firebase analysis
        analytics = Firebase.analytics

        analytics.logEvent("send_gemini_prompt") {
            param("error", "Testing firebase logging")
        }
    }
}