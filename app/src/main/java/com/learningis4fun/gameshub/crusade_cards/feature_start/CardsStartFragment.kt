package com.learningis4fun.gameshub.crusade_cards.feature_start

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Resource
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.CompletedGamesSection
import com.learningis4fun.gameshub.databinding.FragmentCardsStartBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class CardsStartFragment : Fragment(R.layout.fragment_cards_start) {

    private var binding: FragmentCardsStartBinding? = null
    private lateinit var cardsStartViewModel: CardsStartViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cardsStartViewModel =
            ViewModelProvider(requireActivity())[CardsStartViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCardsStartBinding.bind(view)

        binding?.completedGames?.apply {
            setContent {
                val completedGames by cardsStartViewModel.completedGames.collectAsState()
                MaterialTheme {
                    CompletedGamesSection(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White),
                        completedGames = completedGames
                    )
                }
            }
        }

        registerObservers()
        registerEventListeners()

        binding?.challengeAiBtn?.setOnClickListener {
            cardsStartViewModel.onChallengeAIBtnClick()
        }

        binding?.gamesStartToolbar?.menu?.findItem(R.id.action_account)
            ?.setOnMenuItemClickListener {
                cardsStartViewModel.onAccountBtnClick()
                true
            }

        binding?.viewOngoingGame?.onGoingGameResumeBtn?.setOnClickListener {
            cardsStartViewModel.onResumeGameBtnClick()
        }
    }

    private fun showCreateGameViews(state: CardsStartViewModel.CardsStartUIStates.CreateGame) {
        binding?.createGameActions?.isVisible = true
        binding?.challengeAiBtn?.isEnabled = state.enableChallengeAndroidBtn
    }

    private fun hideCreateGameViews() {
        binding?.createGameActions?.isVisible = false
    }

    private fun hideResumeGameViews() {
        binding?.viewOngoingGame?.root?.isVisible = false
    }

    private fun showResumeGameViews(state: CardsStartViewModel.CardsStartUIStates.ResumeGame) {
        binding?.viewOngoingGame?.root?.isVisible = true
        if (state.onGoingGame.isNotEmpty()) {
            binding?.viewOngoingGame?.gameName?.text = state.onGoingGame[0].players
            binding?.viewOngoingGame?.apply {
                gameName.text = state.onGoingGame[0].players
                timeTaken.text = state.onGoingGame[0].timeElapsed
            }
        }
    }

    private fun registerObservers() {

        cardsStartViewModel.cardsStartUIStates.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Error -> TODO()
                is Resource.Loading -> {
                    binding?.rootStartCards?.isVisible = false
                }

                is Resource.Success -> {
                    when (val state = resource.data) {
                        is CardsStartViewModel.CardsStartUIStates.CreateGame -> {
                            hideResumeGameViews()
                            showCreateGameViews(state)
                        }

                        CardsStartViewModel.CardsStartUIStates.Default -> {
                            hideCreateGameViews()
                            hideResumeGameViews()
                        }

                        is CardsStartViewModel.CardsStartUIStates.ResumeGame -> {
                            hideCreateGameViews()
                            showResumeGameViews(state)

                        }

                        null -> TODO()
                    }
                    binding?.rootStartCards?.isVisible = true
                }
            }
        }

    }

    private fun registerEventListeners() {
        lifecycleScope.launchWhenStarted {
            cardsStartViewModel.cardsStartScreenEvents.collect { event ->
                when (event) {
                    CardsStartViewModel.CardsStartScreenEvents.NavigateToPlayCardsScreen -> {
                        val action =
                            CardsStartFragmentDirections.actionCardsStartFragmentToPlayCardsFragment()
                        findNavController().navigate(action)
                    }

                    CardsStartViewModel.CardsStartScreenEvents.NavigateToAccoutScreen -> {
                        val action =
                            CardsStartFragmentDirections.actionCardsStartFragmentToProfileFragment()
                        findNavController().navigate(action)
                    }

                    CardsStartViewModel.CardsStartScreenEvents.OpenChooseNumberOfCardsScreen -> TODO()
                    is CardsStartViewModel.CardsStartScreenEvents.ShowErrorMessage -> {
                        println(event.message)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}