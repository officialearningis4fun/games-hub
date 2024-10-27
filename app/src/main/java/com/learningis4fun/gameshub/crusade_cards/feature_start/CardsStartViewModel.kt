package com.learningis4fun.gameshub.crusade_cards.feature_start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.GameState
import com.learningis4fun.gameshub.crusade_cards.feature_start.domain.model.CompletedGame
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.CardsStartState
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.CompletedGamesState
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.OnGoingGame
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.OngoingGamesState
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util.getDataOrThrowException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class CardsStartViewModel @Inject constructor(
    val repository: DefaultCardRepository,
    private val gameRepository: GameRepository,
    private val userRepository: UserRepository,
    private val gameCardRepository: GameCardRepository,
    private val preferences: CrusadeCardsPreferencesManager
) : ViewModel() {

    private val ongoingGamesStateFlow = MutableStateFlow(OngoingGamesState())
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
            gameRepository.getCurrentGameId().flatMapLatest {
                gameRepository.getGameWithPlayers(it)
            }.collectLatest { gameWithPlayers ->

                val showOngoing = try {
                    gameWithPlayers.game.gameState == GameState.Paused.name
                } catch (e: Exception) {
                    false
                }

                if (!showOngoing) {
                    ongoingGamesStateFlow.update {
                        OngoingGamesState(isLoading = false)
                    }

                    return@collectLatest
                }

                gameCardRepository.getThreeTopPlayedCards(
                    gameId = gameWithPlayers.game.id,
                    playerIds = gameWithPlayers.players.map { it.uniqueId }
                ).collectLatest {
                    val cards = it.map { userCard ->
                        Card(
                            id = userCard.cardId,
                            name = userCard.name,
                            type = userCard.type,
                            image = userCard.image
                        )
                    }

                    ongoingGamesStateFlow.update {
                        OngoingGamesState(
                            games = listOf(
                                OnGoingGame(
                                    threeCards = cards,
                                    gameWithPlayers = gameWithPlayers,
                                )
                            ),
                            isLoading = false
                        )
                    }
                }
            }

        }

        viewModelScope.launch {
            combine(
                ongoingGamesStateFlow,
                completedGamesStateFlow
            ) { ongoing, completed -> Pair(ongoing, completed) }
                .collectLatest { (ongoing, completed) ->
                    val userId = userRepository.getUserId()

                    val showOngoing = ongoing.games.isNotEmpty()

                    cardsStartStateFlow.update {
                        it.copy(
                            completedGamesState = completed.copy(userId = userId),
                            ongoingGamesState = ongoing,
                            showOngoingGame = showOngoing,
                            isLoading = completed.isLoading || ongoing.isLoading
                        )
                    }
                }
        }

        viewModelScope.launch {
            preferences.numberOfCardsPerPlayer.collectLatest { numOfCards ->
                cardsStartStateFlow.update {
                    it.copy(numberOfCardsPerPlayer = numOfCards)
                }
            }
        }
    }

    fun onEvent(event: CardsStartEvents) {
        when (event) {
            CardsStartEvents.DecreaseNumberOfCards -> {
                viewModelScope.launch {
                    val numberOfCardsPerPlayer =
                        cardsStartStateFlow.value.numberOfCardsPerPlayer - 1
                    if (numberOfCardsPerPlayer >= Constants.NumberOfCardsOptions.DEFAULT_CARDS_PER_PLAYER) {
                        preferences.updateNumberOfCardsPerPlayer(numberOfCardsPerPlayer)
                    }
                }
            }

            CardsStartEvents.IncreaseNumberOfCards -> {
                viewModelScope.launch {
                    val numberOfCardsPerPlayer =
                        cardsStartStateFlow.value.numberOfCardsPerPlayer + 1
                    if (numberOfCardsPerPlayer <= Constants.NumberOfCardsOptions.HIGHEST_CARDS_PER_PLAYER) {
                        preferences.updateNumberOfCardsPerPlayer(numberOfCardsPerPlayer)
                    }
                }
            }

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