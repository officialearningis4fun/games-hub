package com.learningis4fun.gameshub.crusade_cards.start

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.start.presentation.CardsStartScreen
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

        binding?.rootStartCards?.apply {
            setContent {
                val cardsStartState by cardsStartViewModel.cardsStartState.collectAsState()
                MaterialTheme {
                    CardsStartScreen(
                        modifier = Modifier.fillMaxSize(),
                        cardsStartState = cardsStartState,
                        onStartClick = {
                            cardsStartViewModel.onChallengeAIBtnClick()
                        },
                        onResumeClick = {
                            cardsStartViewModel.onResumeGameBtnClick()
                        }
                    )
                }
            }
        }

        registerEventListeners()

        binding?.gamesStartToolbar?.menu?.findItem(R.id.action_account)
            ?.setOnMenuItemClickListener {
                cardsStartViewModel.onAccountBtnClick()
                true
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

                    CardsStartViewModel.CardsStartScreenEvents.NavigateToAccountScreen -> {

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