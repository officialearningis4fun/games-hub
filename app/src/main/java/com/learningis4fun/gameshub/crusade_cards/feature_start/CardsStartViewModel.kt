package com.learningis4fun.gameshub.crusade_cards.feature_start

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.CardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.GameState
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Quadruple
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Resource
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toTimeFormatted
import com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.domain.use_case.AssignCardsUseCase
import com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.domain.use_case.CreateGameUseCase
import com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.domain.use_case.ResumeGameUseCase
import com.learningis4fun.gameshub.crusade_cards.feature_start.domain.model.CompletedGame
import com.learningis4fun.gameshub.crusade_cards.feature_start.domain.model.OnGoingGame
import com.learningis4fun.gameshub.crusade_cards.feature_start.domain.use_cases.GetAllCards
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.CardsStartState
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.CompletedGamesState
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.OngoingGameState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class CardsStartViewModel @Inject constructor(
    val repository: DefaultCardRepository,
    private val preference: CrusadeCardsPreferencesManager,
    private val createGameUseCase: CreateGameUseCase,
    private val assignCardsUseCase: AssignCardsUseCase,
    private val resumeGameUseCase: ResumeGameUseCase,
    private val gameRepository: GameRepository,
    private val cardRepository: CardRepository,
    private val userRepository: UserRepository,
    private val gameCardRepository: GameCardRepository,
    private val getAllCards: GetAllCards
) : ViewModel() {

    private val cardsStartScreenEventsChannel = Channel<CardsStartScreenEvents>()
    val cardsStartScreenEvents = cardsStartScreenEventsChannel.receiveAsFlow()

    private val ongoingGamesStateFlow = MutableStateFlow(OngoingGameState())
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
                        it.copy(completedGames = completedGames)
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
                    e.printStackTrace()
                    false
                }

                if (!showOngoing) {
                    ongoingGamesStateFlow.update {
                        OngoingGameState()
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
                        OngoingGameState(
                            threeCards = cards,
                            gameWithPlayers = gameWithPlayers,
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

                    val showOngoing = ongoing != OngoingGameState()

                    cardsStartStateFlow.update {
                        it.copy(
                            completedGamesState = completed.copy(userId = userId),
                            ongoingGameState = ongoing,
                            showOngoingGame = showOngoing
                        )
                    }
                }
        }
    }

    private val _ongoingGameIsActive = MutableStateFlow(false)

    // currentGame
    private fun _onGoingGame(): Flow<List<OnGoingGame>> = combine(
        userRepository.getPlayerSignInDetails(),
        preference.recentGameId
    ) { user, gameId ->
        Pair(user, gameId)
    }.flatMapLatest { (user, gameId) ->

        val playerId = user.uniqueId
        gameRepository.getGame(gameId = gameId).map { game ->

            if (game == null) {
                return@map emptyList()
            }
            if (game.id.isEmpty()) {
                return@map emptyList()
            }

            if (user.uniqueId.isEmpty()) {
                return@map emptyList()
            }

            val gameWithPlayers = gameRepository.getGameWithPlayers(gameId = game.id).first()
            val lastIndex = gameWithPlayers.players.lastIndex
            val players = buildString {
                gameWithPlayers.players
                    .forEachIndexed { index, player ->
                        val name = if (player.uniqueId != playerId) {
                            player.profileName
                        } else {
                            "You"
                        }
                        val playerNameSuffixed = if (index == lastIndex) {
                            name
                        } else {
                            "$name and "
                        }
                        append(playerNameSuffixed)
                    }
            }

            val lastPlayedCardsUrls = gameCardRepository.getThreeTopPlayedCards(
                gameId = game.id,
                playerIds = gameWithPlayers.players.map { it.uniqueId })
                .first()
                .map { it.image }

            listOf(
                OnGoingGame(
                    players = players,
                    timeElapsed = game.gameDuration.toTimeFormatted(),
                    lastPlayedCardsUrls = lastPlayedCardsUrls
                )
            )

        }
    }

    private val _cardsStartUIStates: Flow<CardsStartUIStates>
        get() = combine(
            _onGoingGame(),
            cardRepository.getNumOfCards(),
            gameRepository.getCurrentGameId(),
            _ongoingGameIsActive
        ) { ongoingGame, numOfCards, gameId, ongoingGameIsActive ->
            Quadruple(ongoingGame, numOfCards, gameId, ongoingGameIsActive)
        }.flatMapLatest { (ongoingGame, numOfCards, gameId, ongoingGameIsActive) ->

            if (numOfCards != Constants.Cards.cards.size) {
                return@flatMapLatest flowOf(CardsStartUIStates.Default)
            }

            if (gameId.isEmpty()) {
                return@flatMapLatest flowOf(
                    CardsStartUIStates.CreateGame(
                        enableChallengeFriendsBtn = false,
                        enableChallengeAndroidBtn = true
                    )
                )
            }

            flowOf(
                CardsStartUIStates.ResumeGame(
                    onGoingGame = ongoingGame
                )
            )


        }

    fun onChallengeAIBtnClick() = viewModelScope.launch {

        try {
            _ongoingGameIsActive.value = false
            val loggedUser = userRepository.getPlayerSignInDetails().first()

            val participants = listOf(
                loggedUser,
                userRepository.getAndroidPlayerSignInDetails().first()
            )

            val gameId = createGameUseCase.createGame(
                gameName = "",
                participants = participants
            )
            // assign cards
            if (gameId.isEmpty()) {
                throw Exception("Game identifier not available")
            }
            val assigned = assignCardsUseCase.assignCards(
                gameId = gameId
            )
            if (!assigned) {
                throw Exception("Cards not assigned yet")
            }
            gameRepository.updateRecentGameId(gameId = gameId)
            resumeGameUseCase.resumeGame(currGameId = gameId)
            cardsStartScreenEventsChannel.send(CardsStartScreenEvents.NavigateToPlayCardsScreen)
        } catch (exception: Exception) {
            cardsStartScreenEventsChannel.send(CardsStartScreenEvents.ShowErrorMessage(exception.localizedMessage.toString()))
        }

    }

    fun onResumeGameBtnClick() = viewModelScope.launch {
        val currGameId = gameRepository.getCurrentGameId().first()
        when (val response = resumeGameUseCase.resumeGame(currGameId = currGameId)) {
            is Response.Error -> {
                cardsStartScreenEventsChannel.send(CardsStartScreenEvents.ShowErrorMessage(response.message))
            }

            is Response.Success -> {
                cardsStartScreenEventsChannel.send(CardsStartScreenEvents.NavigateToPlayCardsScreen)
            }
        }
    }

    fun onAccountBtnClick() = viewModelScope.launch {
        cardsStartScreenEventsChannel.send(CardsStartScreenEvents.NavigateToAccountScreen)
    }

    sealed class CardsStartUIStates {
        object Default : CardsStartUIStates()
        data class CreateGame(
            val enableChallengeFriendsBtn: Boolean,
            val enableChallengeAndroidBtn: Boolean
        ) : CardsStartUIStates()

        data class ResumeGame(
            val onGoingGame: List<OnGoingGame>
        ) : CardsStartUIStates()
    }

    sealed class CardsStartScreenEvents {
        object NavigateToPlayCardsScreen : CardsStartScreenEvents()
        object OpenChooseNumberOfCardsScreen : CardsStartScreenEvents()
        object NavigateToAccountScreen : CardsStartScreenEvents()
        data class ShowErrorMessage(val message: String) : CardsStartScreenEvents()
    }
}