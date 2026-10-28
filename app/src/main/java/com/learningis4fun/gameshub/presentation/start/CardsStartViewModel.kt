package com.learningis4fun.gameshub.presentation.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learningis4fun.gameshub.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.util.getDataOrThrowException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class CardsStartViewModel @Inject constructor(
    val repository: DefaultCardRepository,
    private val gameRepository: GameRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val completedGamesStateFlow = MutableStateFlow(CompletedGamesState())
    private val cardsStartStateFlow = MutableStateFlow(CardsStartState())
    val cardsStartState = cardsStartStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            gameRepository.getCompletedGamesWithPlayers()
                .collectLatest { gamesWithPlayers ->

                    val completedGames = gamesWithPlayers.map {
                        com.learningis4fun.gameshub.domain.model.CompletedGame(
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
            completedGamesStateFlow
                .collectLatest { completed ->
                    val userId = userRepository.getUserId()

                    cardsStartStateFlow.update {
                        it.copy(
                            completedGamesState = completed.copy(userId = userId),
                            isLoading = completed.isLoading
                        )
                    }
                }
        }


        viewModelScope.launch {
            cardsStartStateFlow.collectLatest {
                println("cardsStartStateFlow: $it")
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