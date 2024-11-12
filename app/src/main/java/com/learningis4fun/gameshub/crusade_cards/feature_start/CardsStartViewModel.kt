package com.learningis4fun.gameshub.crusade_cards.feature_start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.crusade_cards.feature_start.domain.model.CompletedGame
import com.learningis4fun.gameshub.crusade_cards.feature_start.domain.use_cases.GetOngoingGamesUseCase
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.CardsStartState
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.CompletedGamesState
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.OngoingGamesOrNewGameState
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util.getDataOrThrowException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class CardsStartViewModel @Inject constructor(
    val repository: DefaultCardRepository,
    private val gameRepository: GameRepository,
    private val userRepository: UserRepository,
    private val getOngoingGamesUseCase: GetOngoingGamesUseCase
) : ViewModel() {

    private val completedGamesStateFlow = MutableStateFlow(CompletedGamesState())
    private val cardsStartStateFlow = MutableStateFlow(CardsStartState())
    val cardsStartState = cardsStartStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            gameRepository.getCompletedGamesWithPlayers()
                .collectLatest { gamesWithPlayers ->

                    val completedGames = gamesWithPlayers.map {
                        CompletedGame(
                            duration = it.game.gameDuration,
                            game = it.game,
                            players = it.players
                        )
                    }

                    completedGamesStateFlow.update {
                        it.copy(completedGames = completedGames, isLoading = false)
                    }
                }
        }

        viewModelScope.launch {
            combine(
                getOngoingGamesUseCase(),
                completedGamesStateFlow
            ) { ongoing, completed -> Pair(ongoing, completed) }
                .collectLatest { (ongoing, completed) ->
                    val userId = userRepository.getUserId()

                    val showOngoing = ongoing.games.isNotEmpty()

                    val ongoingGamesOrNewGameState = if (showOngoing) {
                        OngoingGamesOrNewGameState.OngoingGamesState(
                            games = ongoing.games,
                            isLoading = ongoing.isLoading
                        )
                    } else {
                        OngoingGamesOrNewGameState.NewGameState
                    }

                    cardsStartStateFlow.update {
                        it.copy(
                            completedGamesState = completed.copy(userId = userId),
                            ongoingGamesOrNewGameState = ongoingGamesOrNewGameState,
                            isLoading = completed.isLoading || ongoing.isLoading
                        )
                    }
                }
        }
    }

    fun onEvent(event: CardsStartEvents) {
        when (event) {

            is CardsStartEvents.DeleteGame -> {
                onDeleteGame(event.gameId)
            }
        }
    }

    private fun onDeleteGame(gameId: String) {
        viewModelScope.launch {
            try {
                gameRepository.deleteGame(gameId).getDataOrThrowException()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}