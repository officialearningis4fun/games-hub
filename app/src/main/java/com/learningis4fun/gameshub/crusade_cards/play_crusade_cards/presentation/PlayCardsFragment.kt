package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalStdlibApi
@AndroidEntryPoint
class PlayCardsFragment : Fragment() {

    private val playCardsViewModel: PlayCardsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    playCardsViewModel.onCloseNavigationIconClicked() {
                        findNavController().popBackStack()
                    }
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val playCardsState by playCardsViewModel.playCardsState.collectAsState()
                val gameDuration by playCardsViewModel.gameDuration.collectAsState()
                MaterialTheme {
                    PlayCardsScreen(
                        modifier = Modifier
                            .fillMaxSize(),
                        playCardsState = playCardsState,
                        gameDuration = gameDuration,
                        onEvent = playCardsViewModel::onEvent,
                        winner = {
                            findNavController().popBackStack()
                        },
                        onClose = {
                            playCardsViewModel.onCloseNavigationIconClicked {
                                findNavController().popBackStack()
                            }
                        },
                        onProfileClick = {

                        }
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        playCardsViewModel.onAppPaused()
    }

    override fun onResume() {
        super.onResume()
        playCardsViewModel.onAppResumed()
    }

}