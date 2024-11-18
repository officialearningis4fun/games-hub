package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.presentation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.learningis4fun.gameshub.MainViewModel
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.domain.controller.CrusadeLoggedUserCardsController
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.CardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStatesDefaultMsg
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardsErrorMessages
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.FiveFold
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.GamePlayerActions
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.GameState
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.JokerActions
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.playerName
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toState
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toTimeFormatted
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.PlayCardsScreenRoute
import com.learningis4fun.gameshub.crusade_cards.di.CrusadeCardsDataModule
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameWithPlayers
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.CardsActionMessage
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.PlayerScreenInfo
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.UserCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case.EndGameUseCase
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case.GetLatestStageNumber
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case.HandleCardStateUseCase
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case.PauseGameUseCase
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case.ResetGameUseCase
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case.ResumeGameUseCase
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util.GamesHubLogTags
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util.getDataOrThrowException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
@HiltViewModel
class PlayCardsViewModel @ExperimentalCoroutinesApi
@Inject constructor(
    private val preference: CrusadeCardsPreferencesManager,
    private val gameRepository: GameRepository,
    private val cardRepository: CardRepository,
    private val userRepository: UserRepository,
    private val handleCardStateUseCase: HandleCardStateUseCase,
    private val resumeGameUseCase: ResumeGameUseCase,
    private val pauseGameUseCase: PauseGameUseCase,
    private val endGameUseCase: EndGameUseCase,
    private val loggedUserCardsController: CrusadeLoggedUserCardsController,
    private val gameCardRepository: GameCardRepository,
    private val getLatestStageNumber: GetLatestStageNumber,
    private val resetGameUseCase: ResetGameUseCase,
    savedStateHandle: SavedStateHandle,
    @CrusadeCardsDataModule.DefaultDispatcher private val defaultCoroutineDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val playCardsStateFlow = MutableStateFlow(PlayCardsState())
    val playCardsState = playCardsStateFlow.asStateFlow()
    private val currentGameId = savedStateHandle.toRoute<PlayCardsScreenRoute>().currentGameId

    /**
     * Force update the first card to be played by computer
     */
    private val updateOnGameStartedFlow = MutableStateFlow(false)

    private val _gameDuration = MutableStateFlow(Pair(false, 0L))
    private var onLaunch: Boolean = true

    private val gameDurationFlow = MutableStateFlow("00:00")
    val gameDuration = gameDurationFlow.asStateFlow()

    private val gamePlayerActionsFlow: MutableStateFlow<GamePlayerActions> =
        MutableStateFlow(GamePlayerActions.ActionMessages("Hitting up the scene"))

    init {
        viewModelScope.launch {
            combine(
                preference.recentCardState,
                updateOnGameStartedFlow
            ) { cardState, update -> Pair(cardState, update) }.collect {
                playCardsGameWhenOnCardStateChange()
            }
        }

        viewModelScope.launch {
            while (true) {
                val recentGame = gameRepository.getGame(currentGameId).first()
                if (recentGame?.gameState == GameState.Started.name) {
                    adjustGameTimer()
                }
                delay(Constants.GAME_DURATION_INTERVAL)
            }
        }

        viewModelScope.launch {

            gameRepository.getGameWithLatestStagePlayers(currentGameId)
                .flatMapLatest { gameWithPlayers ->
                    try {
                        when (toState(gameWithPlayers.game.gameState)) {
                            GameState.Completed,
                            GameState.NotStarted,
                            GameState.Paused -> loadPlayingGameInfo(null)

                            GameState.Started -> loadPlayingGameInfo(gameId = gameWithPlayers.game.id)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        loadPlayingGameInfo(null)
                    }
                }
                .mapLatest { value ->

                    if (value == null) return@mapLatest

                    val (gameWithPlayers, lastThreePlayedCards, gamePlayerActions, userCards, cardState) = value

                    if (playCardsStateFlow.value.wonGame.gameId.isNotEmpty()) {
                        return@mapLatest
                    }

                    val isGameComplete = try {
                        gameWithPlayers.game.gameState == GameState.Completed.name
                    } catch (e: Exception) {
                        false
                    }

                    if (isGameComplete) return@mapLatest

                    updateUIState(
                        gameWithPlayers = gameWithPlayers,
                        lastThreePlayedCards = lastThreePlayedCards,
                        userCards = userCards,
                        cardState = cardState,
                        gamePlayerAction = gamePlayerActions
                    )

                }
                .collect()
        }

        viewModelScope.launch {
            _gameDuration.collectLatest { (shouldUpdate, duration) ->
                if (shouldUpdate) {
                    val durationText = duration.toTimeFormatted()
                    gameDurationFlow.update {
                        durationText
                    }
                }
            }
        }

        // create or resume game
        viewModelScope.launch {
            val response = resumeGame()

            if (response is Response.Error) {
                playCardsStateFlow.update {
                    it.copy(
                        error = response.message,
                        playCardsErrorState = PlayCardsErrorState(
                            errorMessage = response.message.toString(),
                            dismissible = false,
                            errorButtonText = "Resume game",
                            errorAction = PlayCardsErrorAction.RetryResumeGame
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            gameRepository.getGame(currentGameId)
                .collect { game ->
                    game?.gameState?.let {
                        when (toState(game.gameState)) {
                            GameState.Completed,
                            GameState.NotStarted,
                            GameState.Paused -> Unit

                            GameState.Started -> {
                                updateOnGameStartedFlow.update { !it }
                            }
                        }
                    }
                    Log.d(GamesHubLogTags.PLAY_CARDS_VIEW_MODEL, "current game: $game")
                }
        }
    }

    private fun loadPlayingGameInfo(
        gameId: String?
    ): Flow<FiveFold<GameWithPlayers, List<UserCard>, GamePlayerActions, List<UserCard>, CardStates>?> {
        return if (gameId.isNullOrBlank()) {
            flowOf(null)
        } else {
            combine(
                gameRepository.getGameWithLatestStagePlayers(gameId),
                lastThreePlayedCards(),
                gamePlayerActionsFlow,
                getUserCards(),
                preference.recentCardState
            ) { gameWithPlayers, lastThreePlayedCards, gamePlayerActions, userCards, cardState ->
                FiveFold(
                    gameWithPlayers,
                    lastThreePlayedCards,
                    gamePlayerActions,
                    userCards,
                    cardState,
                )
            }
        }
    }

    private fun adjustGameTimer() {

        val previousDuration = _gameDuration.value.second

        val duration = if (onLaunch) {
            onLaunch = false
            previousDuration
        } else {
            previousDuration + Constants.GAME_DURATION_INTERVAL
        }

        _gameDuration.update {
            it.copy(first = true, second = duration)
        }
    }

    val user = userRepository.getPlayerSignInDetails()

    private fun lastThreePlayedCards(): Flow<List<UserCard>> =
        gameRepository.getGameWithLatestStagePlayers(gameId = currentGameId)
            .mapLatest { gameWithPlayers: GameWithPlayers ->
                try {
                    when (toState(gameWithPlayers.game.gameState)) {
                        GameState.Completed,
                        GameState.NotStarted,
                        GameState.Paused -> {
                            null
                        }

                        GameState.Started -> {
                            gameWithPlayers
                        }
                    }
                } catch (e: Exception) {
                    null
                }
            }
            .flatMapLatest { gameWithUsers ->
                if (gameWithUsers == null || gameWithUsers.game.gameState == GameState.Completed.name) {
                    emptyFlow()
                } else {
                    val playersIds = gameWithUsers.players.map { it.uniqueId }

                    gameCardRepository.getThreeTopPlayedCards(
                        gameId = currentGameId,
                        playerIds = playersIds,
                        stageNumber = getLatestStageNumber(gameId = gameWithUsers.game.id)
                    )
                }
            }

    private suspend fun anyPlayedAllCards(gameId: String): Player? =
        withContext(defaultCoroutineDispatcher) {
            if (gameId.isEmpty()) {
                return@withContext null
            }
            val playersOfGame =
                gameRepository.getGameWithLatestStagePlayers(gameId = gameId).first().players
            if (playersOfGame.isEmpty()) {
                return@withContext null
            }

            val playersOrNulls = playersOfGame.map { player ->
                val numOfLeftCards = gameCardRepository.numOfUnPlayedCardsForPlayer(
                    gameId = gameId,
                    playerId = player.uniqueId,
                    stageNumber = getLatestStageNumber(gameId)
                ).first()

                if (numOfLeftCards > 0) {
                    null
                } else {
                    player
                }
            }

            return@withContext playersOrNulls.find {
                it != null
            }
        }

    private suspend fun playCardsGameWhenOnCardStateChange() {
        try {
            val gameId = currentGameId

            val cardState = preference.recentCardState.first()

            Log.d(GamesHubLogTags.PLAY_CARDS_VIEW_MODEL, "cardState $cardState")
            if (gameId.isEmpty()) {
                throw Exception("Game not found: gameId empty")
            }

            val currGame = gameRepository.getGame(gameId).first()
                ?: throw Exception("Game not found")

            val gameState = toState(currGame.gameState)

            Log.d(GamesHubLogTags.PLAY_CARDS_VIEW_MODEL, "gameState $gameState")

            when (gameState) {
                GameState.Completed -> {
                    throw Exception("Game is completed")
                }

                GameState.NotStarted -> {
                    throw Exception("Game is not started")
                }

                GameState.Paused -> {
                    throw Exception("Game is paused")
                }

                GameState.Started -> {
                    // check if any user has no cards - if yes, they wom so, end game
                    val hasWinner = anyPlayedAllCards(gameId = gameId)
                    Log.d(GamesHubLogTags.PLAY_CARDS_VIEW_MODEL, "hasWinner: $hasWinner")

                    val playerIds = gameRepository.getGameWithLatestStagePlayers(gameId = gameId)
                        .first()
                        .players
                        .map { it.uniqueId }

                    val topCard = gameCardRepository.getTopPlayedCard(
                        gameId = gameId,
                        playerIds = playerIds,
                        stageNumber = getLatestStageNumber(gameId)
                    )
                        .first()?.card

                    val isCardSevenOrJack = topCard?.name == Constants.CardNames.SEVEN
                            || topCard?.name == Constants.CardNames.JACK

                    val isCardNotSevenOrJackOnlyForLessThanTwoPlayers =
                        if (playerIds.size > 2) true else !isCardSevenOrJack

                    if (hasWinner != null && isCardNotSevenOrJackOnlyForLessThanTwoPlayers) {
                        requestedToEndGame(
                            winner = hasWinner,
                            gameId = gameId
                        )
                    } else {
                        val playCardState = handleCardStateUseCase
                            .handleCardState(state = cardState, gameId = gameId)

                        Log.d(
                            GamesHubLogTags.PLAY_CARDS_VIEW_MODEL,
                            "nextPlayState: $playCardState"
                        )
                        // game action messages
                        gamePlayerActionsFlow.update {
                            playCardState ?: it
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private suspend fun requestedToEndGame(
        winner: Player,
        gameId: String
    ) {
        try {
            endGameUseCase(
                currGameId = gameId,
                duration = _gameDuration.value.second,
                winnerId = winner.uniqueId
            ).getDataOrThrowException()

            playCardsStateFlow.update {
                it.copy(
                    wonGame = WonGame(winner = winner, gameId = gameId)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getUserCards(): Flow<List<UserCard>> =
        preference.userCardsSortBy
            .flatMapLatest { sortBy ->
                gameCardRepository.getUnPlayedCardsForPlayer(
                    gameId = currentGameId,
                    stageNumber = getLatestStageNumber(currentGameId),
                    playerId = user.map { it.uniqueId }
                        .first()
                ).map { cardsOfUser ->
                    val userCards = cardsOfUser.groupBy { it.type }.toSortedMap()
                        .map { (_, cards) ->
                            cards.sortedBy { userCard ->
                                when (sortBy) {
                                    MainViewModel.UserCardsSortBy.CardName -> userCard.name
                                    MainViewModel.UserCardsSortBy.CardType -> userCard.type
                                    MainViewModel.UserCardsSortBy.TimePicked -> userCard.gameCardId
                                }
                            }
                        }
                        .flatten()

                    userCards

                }
            }

    private suspend fun gameActionMessages(state: CardStates): CardsActionMessage {
        return when (state) {
            is CardStates.ChangeType -> {
                if (state.playerId in userRepository.getComputerPlayerIds()) {
                    CardsActionMessage("...", true)
                } else {
                    CardsActionMessage(
                        message = state.message
                    )
                }
            }

            is CardStates.DefineCardAction -> {
                if (state.playerId in userRepository.getComputerPlayerIds()) {
                    CardsActionMessage("...", true)
                } else {
                    CardsActionMessage(
                        message = state.message
                    )
                }
            }

            // when there are no cards from the dustbin; we should end the game and start again!
            is CardStates.Error -> {
                if (state.message == CardsErrorMessages.NOT_ENOUGH_CARDS_TO_BE_PICKED) {
                    playCardsStateFlow.update {
                        it.copy(
                            playCardsErrorState = PlayCardsErrorState(
                                errorMessage = state.message,
                                dismissible = false,
                                errorButtonText = "Reset game",
                                errorAction = PlayCardsErrorAction.ResetGame
                            )
                        )
                    }
                    pauseGameAndReleaseResources()
                }
                CardsActionMessage(message = state.message)
            }

            is CardStates.Hit -> {
                if (state.playerId in userRepository.getComputerPlayerIds()) {
                    CardsActionMessage("...", true)
                } else {
                    val message = "${CardStatesDefaultMsg.HitBackOrPick} ${state.cardsToPick} cards"
                    CardsActionMessage(
                        message = message
                    )
                }
            }

            is CardStates.Pick,
            is CardStates.Picked,
            is CardStates.Played,
            is CardStates.NoAvailableCard,
            is CardStates.ChangedType,
            is CardStates.Chance -> {
                CardsActionMessage("...", true)
            }

            is CardStates.Play -> {
                if (state.playerId in userRepository.getComputerPlayerIds()) {
                    CardsActionMessage("...", true)
                } else {
                    val message = if (state.canPick) {
                        CardStatesDefaultMsg.PlayOrPick
                    } else {
                        state.message
                    }
                    CardsActionMessage(
                        message = message
                    )
                }
            }

            CardStates.Start -> CardsActionMessage(message = "Preparing to play...", animate = true)
        }
    }

    private suspend fun updateUIState(
        gameWithPlayers: GameWithPlayers,
        lastThreePlayedCards: List<UserCard>,
        userCards: List<UserCard>,
        cardState: CardStates,
        gamePlayerAction: GamePlayerActions
    ) {
        val playerScreenInfo = playersScreenInfo(
            gameWithPlayers = gameWithPlayers,
            cardState = cardState
        )

        val showCurrentCardTypeIfNecessary = showCurrentCardTypeIfNecessary(
            currGameId = gameWithPlayers.game.id,
            recentCardType = cardRepository.getCurrentCardType(),
            recentCardState = cardState
        )

        val showDefineJokerActions =
            gamePlayerAction is GamePlayerActions.PromptPlayerDefineJokerCardAction

        val showChooseCardTypeScreen =
            gamePlayerAction is GamePlayerActions.PromptPlayerChangeCardType

        val showAllowOthersPlayView = when (gamePlayerAction) {
            is GamePlayerActions.PromptPlayerPlayOrPick -> {
                !gamePlayerAction.canPick && lastThreePlayedCards.isNotEmpty()
            }

            else -> {
                false
            }
        }

        val uiState = PlayingScreenState(
            playersScreenInfo = playerScreenInfo,
            lastThreePlayedCards = lastThreePlayedCards,
            messages = gameActionMessages(state = cardState),
            userCards = userCards,
            allowOthersPlay = showAllowOthersPlayView,
            showChooseCardTypeScreen = showChooseCardTypeScreen,
            showDefineJokerActions = showDefineJokerActions,
            showCurrentCardType = Pair(
                showCurrentCardTypeIfNecessary.isNotEmpty(),
                showCurrentCardTypeIfNecessary
            )
        )

        playCardsStateFlow.update {

            val oldUserCards = it.userUnPlayedCards.first
            val newUserCards = uiState.userCards

            val newlyAddedCards =
                newUserCards
                    .filter { newCard ->
                        newCard.gameCardId !in oldUserCards
                            .map { oldCard -> oldCard.gameCardId }
                    }

            val updatedUserCards: Pair<List<UserCard>, UserCard?> = if (newlyAddedCards.size == 1) {
                Pair(newUserCards, newlyAddedCards.firstOrNull())
            } else {
                Pair(newUserCards, null)
            }

            val playingDirection = try {
                gameWithPlayers.game.playingDirection
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }

            it.copy(
                playersScreenInfo = uiState.playersScreenInfo,
                playedCards = uiState.lastThreePlayedCards,
                userUnPlayedCards = updatedUserCards,
                message = uiState.messages.message,
                allowOthersPlay = uiState.allowOthersPlay,
                showDefineJokerActions = uiState.showDefineJokerActions,
                showCurrentCardType = uiState.showCurrentCardType,
                showChooseCardTypeScreen = uiState.showChooseCardTypeScreen,
                playingDirection = playingDirection,
                isLoading = false
            )
        }
    }

    private suspend fun showCurrentCardTypeIfNecessary(
        currGameId: String,
        recentCardType: String,
        recentCardState: CardStates
    ): String {
        var type = ""
        return try {
            if (currGameId.isEmpty()) {
                throw Exception("Game not available")
            }
            val playerIds =
                gameRepository.getGameWithLatestStagePlayers(currGameId)
                    .first().players.map { it.uniqueId }
            val topPlayedCardName =
                gameCardRepository.getTopPlayedCard(
                    gameId = currGameId,
                    stageNumber = getLatestStageNumber(currentGameId),
                    playerIds = playerIds
                ).first()?.card?.name ?: throw Exception("Top card not found")

            Log.d(GamesHubLogTags.PLAY_CARDS_VIEW_MODEL, "top played card name: $topPlayedCardName")

            when {
                topPlayedCardName == Constants.CardNames.JOKER
                        || topPlayedCardName == Constants.CardNames.EIGHT
                        && (recentCardState !is CardStates.DefineCardAction)
                        && (recentCardState !is CardStates.ChangeType) -> {
                    type = recentCardType
                }
            }

            type
        } catch (exception: Exception) {
            exception.printStackTrace()
            type
        }
    }

    private suspend fun isUserTurn(playerId: String, cardState: CardStates): Boolean {
        return when (cardState) {
            is CardStates.Chance -> {
                playerId == cardState.playerId
            }

            is CardStates.ChangeType -> {
                playerId == cardState.playerId
            }

            is CardStates.DefineCardAction -> {
                playerId == cardState.playerId
            }

            is CardStates.Hit -> {
                playerId == cardState.playerId
            }

            is CardStates.Pick -> {
                playerId == cardState.playerId
            }

            is CardStates.Play -> {
                playerId == cardState.playerId
            }

            CardStates.Start -> {
                playerId == userRepository.getComputerPlayerIds().first()
            }

            else -> false
        }
    }

    // The players info update before the cards are shown on screen
    private suspend fun playersScreenInfo(
        cardState: CardStates,
        gameWithPlayers: GameWithPlayers
    ): List<PlayerScreenInfo> {
        return try {

            val userId = user.first().uniqueId

            val players = gameWithPlayers
                .players

            players.map { player ->

                val gameId = gameWithPlayers.game.id

                val numOfCardsLeft = gameCardRepository.numOfUnPlayedCardsForPlayer(
                    gameId = gameId,
                    stageNumber = getLatestStageNumber(gameId = gameId),
                    playerId = player.uniqueId
                ).first()
                    .toString()

                val playerName = player.playerName(userId)

                PlayerScreenInfo(
                    playerId = player.uniqueId,
                    defaultAvatar = R.drawable.game_test_profile,
                    numOfCardsLeft = numOfCardsLeft,
                    playerName = playerName,
                    isCurrentlyPlaying = isUserTurn(
                        playerId = player.uniqueId,
                        cardState = cardState
                    ),
                    imageUrl = player.imageUrl.ifEmpty { null }
                )

            }

        } catch (exception: Exception) {
            emptyList()
        }
    }

    fun onEvent(event: PlayCardsEvent) {
        when (event) {
            PlayCardsEvent.AllowOthersPlay -> onAllowOthersPlayBtnClick()
            PlayCardsEvent.ClosePlayCardsScreen -> onCloseNavigationIconClicked()
            is PlayCardsEvent.DefineJokerAction -> onJokerActionClick(event.action)
            PlayCardsEvent.PickCardFromDustbin -> onDustbinClick()
            is PlayCardsEvent.PlayCard -> onUserCardClick(event.userCard)
            is PlayCardsEvent.SetNewCardType -> onCardTypeClick(event.type)
            PlayCardsEvent.ScrollToItemPositionConsumed -> {
                playCardsStateFlow.update {
                    it.copy(
                        userUnPlayedCards = it.userUnPlayedCards.copy(second = null)
                    )
                }
            }

            PlayCardsEvent.DismissErrorClick -> {
                playCardsStateFlow.update {
                    it.copy(
                        playCardsErrorState = null
                    )
                }
            }

            is PlayCardsEvent.ErrorButtonClick -> {
                when (event.errorAction) {
                    PlayCardsErrorAction.RetryCardState -> {
                        Log.d(GamesHubLogTags.PLAY_CARDS_VIEW_MODEL, "preparing to retry cardState")
                    }

                    PlayCardsErrorAction.ResetGame -> {
                        viewModelScope.launch {
                            try {
                                Log.d(
                                    GamesHubLogTags.PLAY_CARDS_VIEW_MODEL,
                                    "preparing to reset game"
                                )
                                resetGameUseCase(
                                    gameId = currentGameId
                                ).getDataOrThrowException()
                                resumeGame().getDataOrThrowException()

                            } catch (e: Exception) {
                                Log.d(
                                    GamesHubLogTags.PLAY_CARDS_VIEW_MODEL,
                                    "failed to reset game: $e"
                                )

                            }
                        }
                    }

                    PlayCardsErrorAction.RetryResumeGame -> {
                        Log.d(
                            GamesHubLogTags.PLAY_CARDS_VIEW_MODEL,
                            "preparing to retry resuming game"
                        )
                    }

                    PlayCardsErrorAction.Nothing -> {
                        Log.d(
                            GamesHubLogTags.PLAY_CARDS_VIEW_MODEL,
                            "expected no action from user, this is a warning"
                        )
                    }
                }
            }
        }
    }

    private fun onAllowOthersPlayBtnClick() = viewModelScope.launch(defaultCoroutineDispatcher) {
        loggedUserCardsController.userRequestToAllowOthersPlay(user.first().uniqueId)
    }

    private fun onUserCardClick(userCard: UserCard) =
        viewModelScope.launch(defaultCoroutineDispatcher) {

            // hit back or play or error
            when (val currentCardState = preference.recentCardState.first()) {
                is CardStates.Play -> {

                    if (currentCardState.playerId == user.first().uniqueId) {

                        loggedUserCardsController.userRequestToPlay(
                            userCard = userCard,
                            canPick = currentCardState.canPick
                        )
                    }

                }

                is CardStates.Hit -> {

                    if (currentCardState.playerId == user.first().uniqueId) {

                        loggedUserCardsController.userRequestToHitBack(
                            playerId = currentCardState.playerId,
                            cardsToPick = currentCardState.cardsToPick,
                            userCard = userCard
                        )
                    }
                }

                else -> {
                    playCardsStateFlow.update {
                        it.copy(warning = "Hold on, it might not be your turn")
                    }
                }
            }
        }

    private fun onJokerActionClick(action: JokerActions) =
        viewModelScope.launch(defaultCoroutineDispatcher) {
            // define joker action or error

            when (val currentCardState = preference.recentCardState.first()) {
                is CardStates.DefineCardAction -> {

                    if (currentCardState.playerId == user.first().uniqueId) {
                        loggedUserCardsController.userRequestToDefineJokerAction(
                            playerId = currentCardState.playerId,
                            action = action,
                            gameCardId = currentCardState.gameCardId
                        )
                    }
                }

                else -> {
                    playCardsStateFlow.update {
                        it.copy(
                            warning = "Unexpected behaviour, user has to define joker action",
                            playCardsErrorState = PlayCardsErrorState(
                                errorMessage = "Unexpected behaviour, user has to define joker action",
                                dismissible = true,
                            )
                        )
                    }
                }
            }
        }

    private fun onCardTypeClick(type: String) = viewModelScope.launch(defaultCoroutineDispatcher) {
        // change card type or error
        val currentCardState = preference.recentCardState.first()
        val response = loggedUserCardsController.userRequestToChangeCardType(
            type = type,
            currentCardState = currentCardState,
            playerId = user.first().uniqueId
        )
        if (response is Response.Error) {
            playCardsStateFlow.update {
                it.copy(
                    warning = response.message,
                    playCardsErrorState = PlayCardsErrorState(
                        errorMessage = response.message.toString(),
                        dismissible = true
                    )
                )
            }
        }
    }

    private fun onDustbinClick() = viewModelScope.launch(defaultCoroutineDispatcher) {

        // pick cards
        val currentCardState = preference.recentCardState.first()
        val playerId = user.first().uniqueId

        val response = loggedUserCardsController.userRequestToPickCards(
            currentCardState = currentCardState,
            playerId = playerId
        )

        if (response is Response.Error) {
            playCardsStateFlow.update {
                it.copy(
                    warning = response.message, playCardsErrorState = PlayCardsErrorState(
                        errorMessage = response.message.toString(),
                        dismissible = true
                    )
                )
            }
        }
    }

    private suspend fun pauseGameAndReleaseResources() {
        if (currentGameId.isNotEmpty()) {
            val duration = _gameDuration.value.second
            _gameDuration.update { it.copy(first = false) }
            pauseGameUseCase.pauseGame(currGameId = currentGameId, gameDuration = duration)
        }
    }

    @Throws
    private suspend fun resumeGame(): Response<Unit> {

        return try {
            val gameId = currentGameId

            if (currentGameId.isNotEmpty()) {
                resumeGameUseCase.resumeGame(currentGameId).getDataOrThrowException()
            }
            //resume duration
            val game = gameRepository.getGame(gameId).first()
            _gameDuration.update { duration ->
                game?.gameDuration?.let {
                    duration.copy(
                        first = duration.first,
                        second = it
                    )
                } ?: duration
            }
            Response.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Response.Error("Failed to start the game, please start the game again.")
        }


    }

    fun onCloseNavigationIconClicked(
        onPopBack: () -> Unit = {}
    ) = viewModelScope.launch {
        pauseGameAndReleaseResources()
        onPopBack()
    }

    fun onAppPaused() {
        viewModelScope.launch {
            pauseGameAndReleaseResources()
        }
    }

    fun onAppResumed() {
        viewModelScope.launch {
            resumeGame()
        }
    }

    override fun onCleared() {
        super.onCleared()
        onAppPaused()
    }

    data class PlayingScreenState(
        val playersScreenInfo: List<PlayerScreenInfo>,
        val lastThreePlayedCards: List<UserCard>,
        val messages: CardsActionMessage,
        val userCards: List<UserCard>,
        val allowOthersPlay: Boolean,
        val showChooseCardTypeScreen: Boolean,
        val showDefineJokerActions: Boolean,
        val showCurrentCardType: Pair<Boolean, String>
    )
}