package com.learningis4fun.gameshub.presentation.play_crusade_cards.online

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.learningis4fun.gameshub.api.CrusadeGameService
import com.learningis4fun.gameshub.api.HandleCardStateUseCase
import com.learningis4fun.gameshub.api.UserCardsController
import com.learningis4fun.gameshub.api.use_cases.toGamePlayerAction
import com.learningis4fun.gameshub.app.MainViewModel
import com.learningis4fun.gameshub.commons.domain.model.ActiveGame
import com.learningis4fun.gameshub.commons.domain.model.GameStage
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.model.User
import com.learningis4fun.gameshub.commons.domain.util.CardStates
import com.learningis4fun.gameshub.commons.domain.util.CardStatesDefaultMsg
import com.learningis4fun.gameshub.commons.domain.util.CardsErrorMessages
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.domain.util.FiveFold
import com.learningis4fun.gameshub.commons.domain.util.GamePlayerActions
import com.learningis4fun.gameshub.commons.domain.util.GameState
import com.learningis4fun.gameshub.commons.domain.util.JokerActions
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.commons.domain.util.toCardState
import com.learningis4fun.gameshub.commons.domain.util.toState
import com.learningis4fun.gameshub.commons.domain.util.toTimeFormatted
import com.learningis4fun.gameshub.commons.ui.routes.PlayCardsOnlineScreenRoute
import com.learningis4fun.gameshub.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.data.data_source.relations.GameWithPlayers
import com.learningis4fun.gameshub.data.data_source.remote.GoogleAuthUiClient
import com.learningis4fun.gameshub.di.CrusadeCardsDataModule
import com.learningis4fun.gameshub.domain.model.CardsActionMessage
import com.learningis4fun.gameshub.domain.model.PlayerScreenInfo
import com.learningis4fun.gameshub.domain.model.UserCard
import com.learningis4fun.gameshub.presentation.play_crusade_cards.AnimatingCardAndStage
import com.learningis4fun.gameshub.presentation.play_crusade_cards.AnimatingCardStages
import com.learningis4fun.gameshub.presentation.play_crusade_cards.CurrentlyPlayingAnim
import com.learningis4fun.gameshub.presentation.play_crusade_cards.PlayCardsErrorAction
import com.learningis4fun.gameshub.presentation.play_crusade_cards.PlayCardsErrorState
import com.learningis4fun.gameshub.presentation.play_crusade_cards.PlayCardsEvent
import com.learningis4fun.gameshub.presentation.play_crusade_cards.PlayCardsState
import com.learningis4fun.gameshub.presentation.play_crusade_cards.WonGame
import com.learningis4fun.gameshub.presentation.play_crusade_cards.isAnimating
import com.learningis4fun.gameshub.presentation.play_crusade_cards.isNotAnimating
import com.learningis4fun.gameshub.util.GamesHubLogTags
import com.learningis4fun.gameshub.util.LogTags
import com.learningis4fun.gameshub.util.getDataOrThrowException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds


@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
@HiltViewModel
@OptIn(ExperimentalStdlibApi::class)
class PlayCardsOnlineViewModel
@Inject constructor(
    private val crusadeGameService: CrusadeGameService,
    savedStateHandle: SavedStateHandle,
    private val googleAuthUiClient: GoogleAuthUiClient,
    private val handleCardStateUseCase: HandleCardStateUseCase,
    private val userCardsController: UserCardsController,
    private val preference: CrusadeCardsPreferencesManager,
    @CrusadeCardsDataModule.DefaultDispatcher private val defaultCoroutineDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val playCardsStateFlow = MutableStateFlow(PlayCardsState())
    private val animatingCardAndStageFlow = MutableStateFlow(AnimatingCardAndStage())
    private val recentGameFlow = MutableStateFlow<ActiveGame?>(null)
    private val currentGameId = savedStateHandle.toRoute<PlayCardsOnlineScreenRoute>().currentGameId
    private val updateOnGameStartedFlow = MutableStateFlow(false)
    private val _gameDuration = MutableStateFlow(Pair(false, 0L))
    private val gameDurationFlow = MutableStateFlow("00:00")
    private var onLaunch: Boolean = true


    val playCardsState = playCardsStateFlow.asStateFlow()
    val animatingCardAndStage = animatingCardAndStageFlow.asStateFlow()

    val recentCardState = MutableStateFlow<CardStates?>(null)

    val gameDuration = gameDurationFlow.asStateFlow()

    val handler = CoroutineExceptionHandler { _, throwable ->
        Log.d(LogTags.PLAY_CARDS_VIEW_MODEL, "throwable: $throwable")
    }

    init {

        viewModelScope.launch {
            playCardsStateFlow.collect {
                Log.d(LogTags.PLAY_CARDS_VIEW_MODEL, "playCardsState: ${it}")
                Log.d(LogTags.PLAY_CARDS_VIEW_MODEL, "showCurrentCardTypeStateAndImage: ${it.showCurrentCardTypeAndImage}")
            }
        }

        viewModelScope.launch(handler) {
            crusadeGameService.recentGame(currentGameId).collectLatest { game ->
                Log.d(GamesHubLogTags.PLAY_CARDS_VIEW_MODEL, "gameId:$currentGameId")
                Log.d(GamesHubLogTags.PLAY_CARDS_VIEW_MODEL, "recentGame onChange:$game")
                recentGameFlow.update { game }
            }
        }

        viewModelScope.launch(handler) {
            recentGameFlow.collectLatest { activeGame ->
                if (activeGame == null) return@collectLatest
                recentCardState.update { toCardState(activeGame.recentCardState) }
            }
        }

        viewModelScope.launch(handler) {
            combine(
                recentCardState.filterNotNull(),
                updateOnGameStartedFlow,
                animatingCardAndStageFlow
            ) { cardState, update, animatingCardAndStage ->
                Triple(
                    cardState,
                    update,
                    animatingCardAndStage
                )
            }.collect { (state, _, animatingCardAndStage) ->
                // if animating, skip
                if (animatingCardAndStage.isNotAnimating()) {
                    playCardsGameWhenOnCardStateChange(
                        cardState = state
                    )
                }
            }
        }

        viewModelScope.launch(handler) {
            while (true) {
                val gameState = recentGameFlow.value?.gameState
                if (gameState == GameState.Started.name) {
                    adjustGameTimer()
                } else if(gameState == GameState.Completed.name){
                   try {
                       val winnerId = recentGameFlow.value?.winnerId
                       val winner = recentGameFlow.value?.players
                           ?.find { player -> player.uniqueId == winnerId }
                           ?: throw Exception("Winner not found but game completed")

                       playCardsStateFlow.update {
                           it.copy(
                               wonGame = WonGame(winner = winner, gameId = currentGameId)
                           )
                       }
                   } catch (e: Exception){
                       e.printStackTrace()
                       playCardsStateFlow.update {
                           it.copy(
                           warning = e.message
                           )
                       }
                   }
                }
                delay(Constants.GAME_DURATION_INTERVAL.milliseconds)
            }
        }

        viewModelScope.launch(handler) {
            getGameWithLatestStagePlayers()
                .filterNotNull()
                .flatMapLatest { gameWithPlayers ->
                    try {
                        when (toState(gameWithPlayers.game.gameState)) {
                            GameState.Completed,
                            GameState.NotStarted,
                            GameState.Paused -> loadPlayingGameInfo()

                            GameState.Started -> loadPlayingGameInfo(gameWithPlayers = gameWithPlayers)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        loadPlayingGameInfo()
                    }
                }
                .map { value ->

                    Log.d(GamesHubLogTags.PLAY_CARDS_VIEW_MODEL, "state mapLatest $value")
                    if (value == null) return@map

                    val (gameWithPlayers, lastThreePlayedCards, userCards, cardState, _) = value

                    if (playCardsStateFlow.value.wonGame.hasWonGameId()) {
                        return@map
                    }

                    val isGameComplete = try {
                        gameWithPlayers.game.gameState == GameState.Completed.name
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        e.printStackTrace()
                        false
                    }

                    if (isGameComplete) return@map

                    if (animatingCardAndStageFlow.value.isAnimating()) {
                        updateUIStateOnCardAnimating(
                            newUserCards = userCards,
                            lastThreePlayedCards = lastThreePlayedCards

                        )
                    } else {
                        updateUIState(
                            gameWithPlayers = gameWithPlayers,
                            lastThreePlayedCards = lastThreePlayedCards,
                            userCards = userCards,
                            cardState = cardState,
                        )
                    }

                }
                .collect()
        }

        viewModelScope.launch(handler) {
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
        viewModelScope.launch(handler) {
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

        viewModelScope.launch(handler) {
            recentGameFlow
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
                }
        }
    }

    private fun getGameWithLatestStagePlayers(): Flow<GameWithPlayers?> {
        return recentGameFlow.map { recentGame ->
            if (recentGame == null) return@map null
            val stagePlayerIds = getLatestGameStage().playerIds.map { it.playerId }
            GameWithPlayers(
                game = recentGame.toGame(gameId = currentGameId),
                players = recentGame.players.filter { it.uniqueId in stagePlayerIds }
            )
        }
    }

    fun getGameStages(): List<GameStage> {
        val gameStages = recentGameFlow.value?.gameStages ?: throw Exception("Stage not found")
        return gameStages
    }

    fun getLatestGameStage(): GameStage {
        val gameStages = recentGameFlow.value?.gameStages ?: throw Exception("Stage not found")
        return runCatching {
            gameStages.maxByOrNull { it.stageNumber } ?: throw Exception("Stage not found")
        }.onFailure {
            Log.d(GamesHubLogTags.PLAY_CARDS_VIEW_MODEL, "failed to get latest stage: $it")
        }.getOrThrow()
    }

    fun getLatestStageNumber(): Int {
        return getLatestGameStage().stageNumber
    }

    fun getCurrentCardType(): String {
        return recentGameFlow.value?.currentCardType ?: throw Exception("Card not found")
    }

    fun getCurrentCardName(): String {
        return recentGameFlow.value?.currentCardName ?: throw Exception("Card not found")
    }

    /**
     * GamePlayerActions -> changes when the cardState changes
     */
    private fun loadPlayingGameInfo(
        gameWithPlayers: GameWithPlayers? = null
    ): Flow<FiveFold<GameWithPlayers, List<UserCard>, List<UserCard>, CardStates, AnimatingCardAndStage>?> {
        return if (gameWithPlayers == null) {
            flowOf(null)
        } else {

            combine(
                lastThreePlayedCards(),
                getUserCards(),
                recentCardState.filterNotNull(),
                animatingCardAndStageFlow
            ) { lastThreePlayedCards, userCards, cardState, animatingCardAndStage ->
                Log.d(GamesHubLogTags.PLAY_CARDS_VIEW_MODEL, "userCards: $userCards")
                Log.d(GamesHubLogTags.PLAY_CARDS_VIEW_MODEL, "recentcardState: $cardState")
                FiveFold(
                    gameWithPlayers,
                    lastThreePlayedCards,
                    userCards,
                    cardState,
                    animatingCardAndStage
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

    fun getUser(): User {
        return googleAuthUiClient.signedInUser.value ?: throw Exception("User not found")
    }

    private fun lastThreePlayedCards(): Flow<List<UserCard>> =
        getGameWithLatestStagePlayers()
            .filterNotNull()
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
                    e.printStackTrace()
                    null
                }
            }
            .flatMapLatest { gameWithUsers ->
                if (gameWithUsers == null || gameWithUsers.game.gameState == GameState.Completed.name) {
                    emptyFlow()
                } else {
                    val playersIds = gameWithUsers.players.map { it.uniqueId }

                    crusadeGameService.getTopThreePlayedCards(
                        gameId = currentGameId,
                        playerIds = playersIds,
                        stageNumber = getLatestStageNumber()
                    )
                }
            }

    private suspend fun anyPlayedAllCards(
        gameWithPlayers: GameWithPlayers,
        stageNumber: Int
    ): Player? {

        return try {

            val playersWithCardsLeft: List<Pair<Player, Int>> = gameWithPlayers
                .players
                .mapNotNull { player ->
                    val numOfCardsLeft = crusadeGameService.getNumOfUnPlayedCardsForUser(
                        gameId = gameWithPlayers.game.id,
                        playerId = player.uniqueId,
                        stageNumber = stageNumber
                    ).firstOrNull()
                    numOfCardsLeft?.let {
                        Pair(player, numOfCardsLeft)
                    }
                }

            val winner = playersWithCardsLeft.firstOrNull { (_, numOfCardsLeft) ->
                numOfCardsLeft == 0
            }?.first

            Log.d(
                GamesHubLogTags.PLAY_CARDS_VIEW_MODEL,
                "stageNum: $stageNumber game:${gameWithPlayers.game} players: ${
                    playersWithCardsLeft.map {
                        Pair(
                            it.first.uniqueId,
                            it.second
                        )
                    }
                }"
            )
            winner
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            null
        }


    }

    // when the last card is played; after animating, the played card jumps to the user's cards
    private suspend fun playCardsGameWhenOnCardStateChange(
        cardState: CardStates
    ) {
        try {
            val gameId = currentGameId

            Log.d(GamesHubLogTags.PLAY_CARDS_VIEW_MODEL, "cardState $cardState")
            if (gameId.isEmpty()) {
                throw Exception("Game not found: gameId empty")
            }

            val currGame = recentGameFlow.first()
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


                    val gameWithPlayers =
                        getGameWithLatestStagePlayers().filterNotNull().first()


                    val stageNumber = getLatestStageNumber()

                    val hasWinner = anyPlayedAllCards(
                        gameWithPlayers = gameWithPlayers,
                        stageNumber = stageNumber
                    )

                    val playerIds = gameWithPlayers
                        .players
                        .map { it.uniqueId }

                    Log.d(LogTags.PLAY_CARDS_VIEW_MODEL, "playerIds: $playerIds")

                    val topCard = crusadeGameService.getTopPlayedCard(
                        gameId = gameId,
                        playerIds = playerIds,
                        stageNumber = stageNumber
                    )
                        .data

                    val isCardSevenOrJack =
                        topCard != null && topCard.name == Constants.CardNames.SEVEN
                                || topCard != null && topCard.name == Constants.CardNames.JACK

                    val isCardNotSevenOrJackOnlyForLessThanTwoPlayers =
                        if (playerIds.size > 2) true else !isCardSevenOrJack

                    if (hasWinner != null && isCardNotSevenOrJackOnlyForLessThanTwoPlayers) {
                        requestedToEndGame(
                            winner = hasWinner,
                            gameId = gameId
                        )
                    } else {
                        Log.d(LogTags.PLAY_CARDS_VIEW_MODEL, "handleCardState called $cardState")
                        val playCardState = handleCardStateUseCase
                            .handleCardState(
                                state = cardState,
                                gameId = gameId,
                                gameStages = getGameStages(),
                                winnerId = recentGameFlow.value?.winnerId
                            )

                        Log.d(
                            GamesHubLogTags.PLAY_CARDS_VIEW_MODEL,
                            "nextPlayState: $playCardState"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            Log.d(GamesHubLogTags.PLAY_CARDS_VIEW_MODEL, "failed to play cards: $e")
        }

    }

    private suspend fun requestedToEndGame(
        winner: Player,
        gameId: String,
    ) {
        try {
            val playerIds = getGameWithLatestStagePlayers().first()?.players?.map { it.uniqueId }
                ?: throw Exception("Players not found to end the game")
            playCardsStateFlow.update { it.copy(wonGame = WonGame(gameId = gameId)) }
            crusadeGameService.endGame(
                currGameId = gameId,
                stageNumber = getLatestStageNumber(),
                duration = _gameDuration.value.second,
                winnerId = winner.uniqueId,
                playerIds = playerIds
            ).getDataOrThrowException()

            playCardsStateFlow.update {
                it.copy(
                    wonGame = WonGame(winner = winner, gameId = gameId)
                )
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
        }
    }

    private fun getUserCards(): Flow<List<UserCard>> =
        preference.userCardsSortBy
            .flatMapLatest { sortBy ->
                crusadeGameService.getUnPlayedCardsForUser(
                    gameId = currentGameId,
                    stageNumber = getLatestStageNumber(),
                    playerId = getUser().id
                ).mapLatest { cardsOfUser ->
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
        val userId = getUser().id
        return when (state) {
            is CardStates.ChangeType -> {
                if (state.playerId != userId) {
                    CardsActionMessage("...", true)
                } else {
                    CardsActionMessage(
                        message = state.message
                    )
                }
            }

            is CardStates.DefineCardAction -> {
                if (state.playerId != userId) {
                    CardsActionMessage("...", true)
                } else {
                    CardsActionMessage(
                        message = state.message
                    )
                }
            }

            // when there are no cards from the dustbin; we should end the game and start again!
            is CardStates.Error -> {
                if (state.playerId.isNotEmpty()) {
                    // request retry
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
                if (state.playerId != userId) {
                    CardsActionMessage("...", true)
                } else {
                    val message = "${CardStatesDefaultMsg.HitBackOrPick} ${state.cardsToPick} cards"
                    CardsActionMessage(
                        message = message
                    )
                }
            }

            is CardStates.Pick -> {
                CardsActionMessage("...", true)
            }

            is CardStates.Picked -> {
                CardsActionMessage("...", true)
            }

            is CardStates.Played -> {
                CardsActionMessage("...", true)
            }

            is CardStates.NoAvailableCard -> {
                CardsActionMessage("...", true)
            }

            is CardStates.ChangedType,
            is CardStates.Chance -> {
                CardsActionMessage("...", true)
            }

            is CardStates.Play -> {
                if (state.playerId != userId) {
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

            CardStates.Start -> CardsActionMessage(
                message = "Preparing to play...",
                animate = true
            )
        }
    }

    private suspend fun updateUIState(
        gameWithPlayers: GameWithPlayers,
        lastThreePlayedCards: List<UserCard>,
        userCards: List<UserCard>,
        cardState: CardStates,
    ) {

        val gamePlayerAction = cardState.toGamePlayerAction(userId = getUser().id)

        val playerScreenInfo = playersScreenInfo(
            gameWithPlayers = gameWithPlayers,
            cardState = cardState
        )

        val currentlyPlaying = playerScreenInfo.firstOrNull { it.isCurrentlyPlaying }

        val showCurrentCardTypeIfNecessary = showCurrentCardTypeIfNecessary(
            recentCardState = cardState
        )

        Log.d(GamesHubLogTags.PLAY_CARDS_VIEW_MODEL, "showCurrentCardTypeIfNecessary: $showCurrentCardTypeIfNecessary")

        val showDefineJokerActions =
            gamePlayerAction is GamePlayerActions.PromptPlayerDefineJokerCardAction

        val showChooseCardTypeScreen =
            gamePlayerAction is GamePlayerActions.PromptPlayerChangeCardType

        val showAllowOthersPlayView = when (gamePlayerAction) {
            is GamePlayerActions.PromptPlayerPlayOrPick -> {

                val showAllOwOthersPlay =
                    if (gamePlayerAction.message == CardStatesDefaultMsg.PlayOnly) {
                        // should show allowOthersPlay
                        lastThreePlayedCards.isNotEmpty() && gamePlayerAction.canPick.not()
                    } else {
                        // shouldn't show allowOthersPlay
                        false
                    }
                showAllOwOthersPlay
            }

            else -> {
                false
            }
        }

        // when animating card only update the user's cards

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

            val isAnimate = it.currPlaying.currPlaying != currentlyPlaying

            val currentlyPlayingAnim = CurrentlyPlayingAnim(
                currPlaying = currentlyPlaying,
                animatingStages = if (isAnimate) AnimatingCardStages.INITIATED else it.currPlaying.animatingStages
            )

            Log.d(GamesHubLogTags.PLAY_CARDS_VIEW_MODEL, "currPlaying: $currentlyPlayingAnim")

            it.copy(
                playersScreenInfo = uiState.playersScreenInfo,
                currPlaying = currentlyPlayingAnim,
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

    private fun updateUIStateOnCardAnimating(
        newUserCards: List<UserCard>,
        lastThreePlayedCards: List<UserCard>
    ) {
        playCardsStateFlow.update {
            val oldUserCards = it.userUnPlayedCards.first

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
            it.copy(
                userUnPlayedCards = updatedUserCards,
                playedCards = lastThreePlayedCards
            )
        }
    }

    private fun showCurrentCardTypeIfNecessary(
        recentCardState: CardStates
    ): String {

       var type = ""
        return try {
            val recentCardType = getCurrentCardType()
            Log.d(
                GamesHubLogTags.PLAY_CARDS_VIEW_MODEL,
                "showCurrentCardTypeIfNecessary: $recentCardState - $recentCardType"
            )
            val topPlayedCardName = getCurrentCardName()

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
            if (exception is CancellationException) throw exception
            Log.d(GamesHubLogTags.PLAY_CARDS_VIEW_MODEL, "exception: $exception")
            exception.printStackTrace()
            ""
        }
    }

    private fun isUserTurn(playerId: String, cardState: CardStates): Boolean {
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
                false
            }

            else -> false
        }
    }

    // The players numOfCardsLeft update before the cards or nowPlaying is shown are shown on screen
    private suspend fun playersScreenInfo(
        cardState: CardStates,
        gameWithPlayers: GameWithPlayers
    ): List<PlayerScreenInfo> {
        return try {

            val userId = getUser().id

            val players = gameWithPlayers
                .players

            val playersScreenInfo = playCardsStateFlow.value.playersScreenInfo

            players.map { player ->

                val gameId = gameWithPlayers.game.id

                val isUserTurn = isUserTurn(
                    playerId = player.uniqueId,
                    cardState = cardState
                )

                val numOfCardsLeft = numOfCardsLeft(
                    player,
                    isUserTurn,
                    gameId = gameId,
                    playerScreenInfo = playersScreenInfo.firstOrNull { it.playerId == player.uniqueId }
                )

                player.toPlayerScreenInfo(userId = userId).copy(
                    numOfCardsLeft = numOfCardsLeft,
                    isCurrentlyPlaying = isUserTurn,
                )
            }

        } catch (exception: Exception) {
            if (exception is CancellationException) throw exception
            exception.printStackTrace()
            emptyList()
        }
    }

    private suspend fun numOfCardsLeft(
        player: Player,
        isUserTurn: Boolean = false,
        gameId: String,
        playerScreenInfo: PlayerScreenInfo?
    ): String {

        return if (playerScreenInfo != null) {
            // get new old when is user turn
            if (isUserTurn) {
                crusadeGameService.getNumOfUnPlayedCardsForUser(
                    gameId = gameId,
                    stageNumber = getLatestStageNumber(),
                    playerId = player.uniqueId
                ).first()
                    .toString()
            } else {
                Log.d(GamesHubLogTags.PLAY_CARDS_VIEW_MODEL, "not user turn: $playerScreenInfo")
                playerScreenInfo.numOfCardsLeft
            }
        } else {
            crusadeGameService.getNumOfUnPlayedCardsForUser(
                gameId = gameId,
                stageNumber = getLatestStageNumber(),
                playerId = player.uniqueId
            ).first()
                .toString()
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
                                crusadeGameService.resetGame(
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

            is PlayCardsEvent.PlayedCard -> {

                // if the card is successfully played; start animation
                if (event.userCard == null) {
                    // completed the animation

                    animatingCardAndStageFlow.update {
                        it.copy(
                            animatingStages = AnimatingCardStages.NOT_STARTED,
                            animatingCard = null
                        )
                    }
                } else {
                    animatingCardAndStageFlow.update {
                        it.copy(
                            animatingStages = AnimatingCardStages.ANIMATING,
                            animatingCard = event.userCard
                        )
                    }
                }

            }
        }
    }

    private fun onAllowOthersPlayBtnClick() = viewModelScope.launch(defaultCoroutineDispatcher) {
        userCardsController.userRequestToAllowOthersPlay(
            gameId = currentGameId,
            playerId = getUser().id
        )
    }

    private fun onUserCardClick(userCard: UserCard) =
        viewModelScope.launch(defaultCoroutineDispatcher) {
            // hit back or play or error
            when (val currentCardState = recentCardState.first()) {
                is CardStates.Play -> {

                    if (currentCardState.playerId == getUser().id) {

                        animatingCardAndStageFlow.update {
                            it.copy(
                                animatingCard = userCard,
                                animatingStages = AnimatingCardStages.INITIATED
                            )
                        }
                        val response = userCardsController.userRequestToPlay(
                            userCard = userCard,
                            canPick = currentCardState.canPick,
                            currentCardType = recentGameFlow.value?.currentCardType ?: "",
                            currentCardName = recentGameFlow.value?.currentCardName ?: ""

                        )
                        when (response) {
                            is Response.Error -> {

                                animatingCardAndStageFlow.update {
                                    it.copy(
                                        animatingCard = null,
                                        animatingStages = AnimatingCardStages.NOT_STARTED
                                    )
                                }
                            }

                            is Response.Success -> {

                                animatingCardAndStageFlow.update {
                                    it.copy(
                                        animatingStages = AnimatingCardStages.ANIMATING,
                                        animatingCard = userCard
                                    )
                                }
                            }
                        }
                    }

                }

                is CardStates.Hit -> {

                    if (currentCardState.playerId == googleAuthUiClient.signedInUser.first()?.id) {

                        animatingCardAndStageFlow.update {
                            it.copy(
                                animatingStages = AnimatingCardStages.INITIATED,
                                animatingCard = userCard
                            )
                        }
                        val response = userCardsController.userRequestToHitBack(
                            playerId = currentCardState.playerId,
                            cardsToPick = currentCardState.cardsToPick,
                            userCard = userCard
                        )
                        when (response) {
                            is Response.Error -> {

                                animatingCardAndStageFlow.update {
                                    it.copy(
                                        animatingStages = AnimatingCardStages.NOT_STARTED,
                                        animatingCard = null
                                    )
                                }
                            }

                            is Response.Success -> {
                                animatingCardAndStageFlow.update {
                                    it.copy(
                                        animatingStages = AnimatingCardStages.ANIMATING,
                                        animatingCard = userCard
                                    )
                                }
                            }
                        }
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

            when (val currentCardState = recentCardState.first()) {
                is CardStates.DefineCardAction -> {

                    if (currentCardState.playerId == googleAuthUiClient.signedInUser.first()?.id) {
                        userCardsController.userRequestToDefineJokerAction(
                            gameId = currentGameId,
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

    private fun onCardTypeClick(type: String) = viewModelScope.launch(handler) {
        // change card type or error
        Log.d(GamesHubLogTags.PLAY_CARDS_VIEW_MODEL, "onCardTypeClick: $type")
        val currentCardState = recentCardState.first()!!
        val response = userCardsController.userRequestToChangeCardType(
            type = type,
            gameId = currentGameId,
            currentCardState = currentCardState,
            playerId = getUser().id
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
        val currentCardState = recentCardState.first()!!
        val playerId = getUser().id

        val response = userCardsController.userRequestToPickCards(
            currentCardState = currentCardState,
            gameId = currentGameId,
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
            crusadeGameService.pauseGame(gameId = currentGameId, duration = duration)
        }
    }

    @Throws
    private suspend fun resumeGame(): Response<Unit> {

        return try {

            if (currentGameId.isNotEmpty()) {
                crusadeGameService.resumeGame(currentGameId).getDataOrThrowException()
            }
            //resume duration
            val game = recentGameFlow.first()
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