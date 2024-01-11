package com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.presentation

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.feature_start.CardsStartViewModel
import com.learningis4fun.gameshub.databinding.FragmentPlayCardsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalStdlibApi
@AndroidEntryPoint
class PlayCardsFragment : Fragment(R.layout.fragment_play_cards) {

    private val playCardsViewModel: PlayCardsViewModel by viewModels()
    private lateinit var cardsStartViewModel: CardsStartViewModel

    private var binding: FragmentPlayCardsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cardsStartViewModel = ViewModelProvider(requireActivity())[CardsStartViewModel::class.java]

        requireActivity().onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    playCardsViewModel.onCloseNavigationIconClicked() {
                        findNavController().popBackStack()
                    }
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPlayCardsBinding.bind(view)

        binding?.rootPlayCards?.apply {
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
                            println(it)
                            findNavController().popBackStack()
                        }
                    )
                }
            }
        }

        binding?.toolbar?.setNavigationOnClickListener {
            playCardsViewModel.onCloseNavigationIconClicked {
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}