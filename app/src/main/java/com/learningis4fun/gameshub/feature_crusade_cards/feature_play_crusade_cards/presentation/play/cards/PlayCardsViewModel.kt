package com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.presentation.play.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learningis4fun.gameshub.MainViewModel
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.feature_crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.controller.CrusadeLoggedUserCardsController
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.controller.MultiplayerCrusadeCardsController
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.CardRepository
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.CardStatesDefaultMsg
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.FiveFold
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.GameState
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.JokerActions
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.TrackingPlay
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.toState
import com.learningis4fun.gameshub.feature_crusade_cards.di.CrusadeCardsDataModule
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.CardsActionMessage
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.GamePlayerActions
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.PlayerScreenInfo
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.UserCard
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.UserCardScrollInfo
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.use_case.EndGameUseCase
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.use_case.HandleCardStateUseCase
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.use_case.PauseGameUseCase
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.use_case.ResumeGameUseCase
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.presentation.PlayCardsEvent
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.presentation.PlayCardsState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.util.InternalAPI
import io.ktor.util.date.toDate
import io.ktor.util.date.toJvmDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

@OptIn(InternalAPI::class)
@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
@HiltViewModel
class PlayCardsViewModel @ExperimentalCoroutinesApi
@Inject constructor(
    private val repository: DefaultCardRepository,
    private val preference: CrusadeCardsPreferencesManager,
    private val gameRepository: GameRepository,
    private val cardRepository: CardRepository,
    private val userRepository: UserRepository,
    private val handleCardStateUseCase: HandleCardStateUseCase,
    private val resumeGameUseCase: ResumeGameUseCase,
    private val pauseGameUseCase: PauseGameUseCase,
    private val endGameUseCase: EndGameUseCase,
    private val cardsController: MultiplayerCrusadeCardsController,
    private val loggedUserCardsController: CrusadeLoggedUserCardsController,
    private val gameCardRepository: GameCardRepository,
    @CrusadeCardsDataModule.DefaultDispatcher private val defaultCoroutineDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val playCardsStateFlow = MutableStateFlow(PlayCardsState())
    val playCardsState = playCardsStateFlow.asStateFlow()

    private val _gameDuration = MutableStateFlow(Pair(false, 0L))
    var _lastCurrentTime: Long? = null

    private val _playCardsStates: MutableStateFlow<GamePlayerActions> =
        MutableStateFlow(GamePlayerActions.ActionMessages("Hitting up the scene"))

    init {
        viewModelScope.launch(defaultCoroutineDispatcher) {
            preference.recentCardState.collect {
                when (val tracking = playCardsGameWhenOnCardStateChange()) {
                    is TrackingPlay.HasWinner -> {
                        playCardsStateFlow.update {
                            it.copy(winner = tracking.player)
                        }
                    }

                    else -> Unit
                }
            }
        }

        viewModelScope.launch(defaultCoroutineDispatcher) {
            while (true) {
                val currGameId = gameRepository.getCurrentGameId().first()
                _gameDuration.value = adjustGameTimer(
                    currGameId = currGameId,
                    gameDuration = _gameDuration.value,
                    lastDuration = _lastCurrentTime
                )
                delay(Constants.GAME_DURATION_INTERVAL)
            }
        }

        viewModelScope.launch {
            playCardsUIState().collectLatest { uIStates ->
                when (uIStates) {
                    PlayCardsUIStates.Loading -> TODO()
                    PlayCardsUIStates.WinnerScreen -> TODO()
                    is PlayCardsUIStates.PlayingScreen -> {
                        playCardsStateFlow.update {
                            it.copy(
                                playersScreenInfo = uIStates.playersScreenInfo,
                                playedCards = uIStates.lastThreePlayedCards,
                                duration = uIStates.durationText,
                                userUnPlayedCards = uIStates.userCards.first,
                                message = uIStates.messages.message,
                                allowOthersPlay = uIStates.allowOthersPlay,
                                showDefineJokerActions = uIStates.showDefineJokerActions,
                                showCurrentCardType = uIStates.showCurrentCardType,
                                showChooseCardTypeScreen = uIStates.showChooseCardTypeScreen
                            )
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            val calender = Calendar.getInstance()
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            _gameDuration.collectLatest {(shouldUpdate, duration) ->
                if(shouldUpdate){
                    val date = calender.toDate(duration).toJvmDate()
                    playCardsStateFlow.update {
                        it.copy(
                            duration = timeFormat.format(date)
                        )
                    }
                }
            }
        }
    }

    suspend fun adjustGameTimer(
        currGameId: String,
        gameDuration: Pair<Boolean, Long>,
        lastDuration: Long?
    ): Pair<Boolean, Long> {
        var recentGameDuration: Pair<Boolean, Long>
        var lastCurrentTime = lastDuration

        recentGameDuration =
            if (currGameId.isEmpty() || gameRepository.getGame(currGameId).firstOrNull() == null) {
                Pair(false, 0L)
            } else if (gameDuration.first) {
                gameDuration
            } else {
                val duration = gameRepository.getGame(currGameId).first()?.gameDuration!!
                Pair(true, duration)
            }

        val shouldUpdateTime = recentGameDuration.first

        if (shouldUpdateTime) {
            lastCurrentTime = lastCurrentTime ?: System.currentTimeMillis()

            val currentTime = System.currentTimeMillis()

            val prevDuration = recentGameDuration.second
            val durationInterval = abs(currentTime - lastCurrentTime)
            val duration = prevDuration + durationInterval

            recentGameDuration = Pair(true, duration)
            _lastCurrentTime = currentTime
        }
        return recentGameDuration
    }

    val user = userRepository.getPlayerSignInDetails()

    private fun updatePlayCardsStates(state: GamePlayerActions) {
        _playCardsStates.value = state
    }

    private fun lastThreePlayedCards(): Flow<List<UserCard>> =
        gameRepository.getCurrentGameId().flatMapLatest { gameId ->
            if (gameId.isEmpty()) {
                return@flatMapLatest emptyFlow()
            }
            gameRepository.getGameWithPlayers(gameId = gameId)
                .flatMapLatest { gameWithUsers ->
                    val playersIds = gameWithUsers.players.map { it.uniqueId }
                    gameCardRepository.getThreeTopPlayedCards(
                        gameId = gameId,
                        playerIds = playersIds
                    )
                }
        }

    private suspend fun anyPlayedAllCards(gameId: String): Player? =
        withContext(defaultCoroutineDispatcher) {
            if (gameId.isEmpty()) {
                return@withContext null
            }
            val playersOfGame = gameRepository.getGameWithPlayers(gameId = gameId).first().players
            if (playersOfGame.isEmpty()) {
                return@withContext null
            }

            val playersOrNulls = playersOfGame.map { player ->
                val numOfLeftCards = gameCardRepository.numOfUnPlayedCardsForPlayer(
                    gameId = gameId,
                    playerId = player.uniqueId
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

    private suspend fun playCardsGameWhenOnCardStateChange(): TrackingPlay =
        withContext(defaultCoroutineDispatcher) {

            val gameId = gameRepository.getCurrentGameId().first()
            val cardState = preference.recentCardState.first()

            if (gameId.isEmpty()) {
                return@withContext TrackingPlay.Error("empty gameId")
            }

            val currGame = gameRepository.getGame(gameId).first()

            val gameState = currGame?.let {
                toState(currGame.gameState)
            } ?: return@withContext TrackingPlay.Error("Game was not found")

            return@withContext when (gameState) {
                GameState.Completed -> {
                    TrackingPlay.Error("game is completed")
                }

                GameState.NotStarted -> {
                    resumeGameUseCase.resumeGame(gameId)
                    TrackingPlay.Playing
                }

                GameState.Paused -> {
                    resumeGameUseCase.resumeGame(gameId)
                    TrackingPlay.Error("Game is paused")
                }

                GameState.Started -> {
                    // check if any user has no cards - if yes, they wom so, end game
                    val hasWinner = anyPlayedAllCards(gameId = gameId)
                    val playerIds = gameRepository.getGameWithPlayers(gameId = gameId)
                        .first()
                        .players
                        .map { it.uniqueId }

                    val topCard =
                        gameCardRepository.getTopPlayedCard(gameId = gameId, playerIds = playerIds)
                            .first()?.card

                    val isCardSevenOrJack = topCard?.name == Constants.CardNames.SEVEN
                            || topCard?.name == Constants.CardNames.JACK

                    if (hasWinner != null && !isCardSevenOrJack) {
                        endGameUseCase.endGame(
                            currGameId = gameId,
                            duration = 0L,
                            winnerId = hasWinner.uniqueId
                        )
                        TrackingPlay.HasWinner(hasWinner)
                    } else {
                        val playCardState = handleCardStateUseCase.handleCardState(cardState)
                        playCardState?.let { updatePlayCardsStates(playCardState) }
                        TrackingPlay.ContinuePlaying
                    }
                }
            }

        }

    private fun getPositionOfRecentlyAddedCardIfAdded(
        userCards: List<UserCard>,
        cardThatMightRecentlyBeAddedCard: UserCard
    ): UserCardScrollInfo {

        val nowTime = System.currentTimeMillis()

        val timeDifference = abs(nowTime.minus(cardThatMightRecentlyBeAddedCard.pickedTime))

        val isAddedRecently = timeDifference <= 2000L

        val message = "start: ${cardThatMightRecentlyBeAddedCard.pickedTime} now: $nowTime"

        return if (isAddedRecently) {
            val positionOfRecentlyAddedCard = userCards.indexOf(cardThatMightRecentlyBeAddedCard)

            UserCardScrollInfo(true, positionOfRecentlyAddedCard, timeDifference)
        } else {
            UserCardScrollInfo(false, 0, timeDifference, message)
        }
    }

    private fun _getUserCardsAndScrollInfo(): Flow<Pair<List<UserCard>, UserCardScrollInfo>> =
        combine(
            gameRepository.getCurrentGameId(),
            preference.userCardsSortBy
        ) { currGameId, sortBy ->
            Pair(currGameId, sortBy)
        }.flatMapLatest { (currGameId, sortBy) ->
            if (currGameId.isEmpty()) {
                flowOf(Pair(emptyList(), UserCardScrollInfo()))
            } else {
                gameCardRepository.getUnPlayedCardsForPlayer(
                    gameId = currGameId,
                    playerId = preference.user.map { it.uniqueId }.first()
                ).map {
                    val userCards = it.sortedBy { userCard ->
                        when (sortBy) {
                            MainViewModel.UserCardsSortBy.CardName -> userCard.name
                            MainViewModel.UserCardsSortBy.CardType -> userCard.type
                            MainViewModel.UserCardsSortBy.TimePicked -> userCard.type
                        }
                    }

                    val recentlyAddedCard = it.maxByOrNull { userCard ->
                        userCard.pickedTime
                    }

                    val userCardScrollInfo = recentlyAddedCard?.let { userCard ->
                        getPositionOfRecentlyAddedCardIfAdded(it, userCard)
                    } ?: UserCardScrollInfo()

                    Pair(userCards, userCardScrollInfo)

                }
            }
        }

    private suspend fun gameActionMessages(state: CardStates): CardsActionMessage {
        return when (state) {
            is CardStates.ChangeType -> {
                if (state.playerId == userRepository.getAndroidUserId()) {
                    CardsActionMessage("...", true)
                } else {
                    CardsActionMessage(
                        message = state.message
                    )
                }
            }

            is CardStates.DefineCardAction -> {
                if (state.playerId == userRepository.getAndroidUserId()) {
                    CardsActionMessage("...", true)
                } else {
                    CardsActionMessage(
                        message = state.message
                    )
                }
            }

            is CardStates.Error -> CardsActionMessage(message = state.message)
            is CardStates.Hit -> {
                if (state.playerId == userRepository.getAndroidUserId()) {
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
                if (state.playerId == userRepository.getAndroidUserId()) {
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

    private fun playCardsUIState(): Flow<PlayCardsUIStates> = combine(
        gameRepository.getCurrentGameId(),
        lastThreePlayedCards(),
        _playCardsStates,
        _getUserCardsAndScrollInfo(),
        preference.recentCardState
    ) { gameId, lastThreePlayedCards, playCardsStates, userCardsAndScrollInfo, cardState ->
        FiveFold(
            gameId,
            lastThreePlayedCards,
            playCardsStates,
            userCardsAndScrollInfo,
            cardState,
        )
    }.flatMapLatest { (gameId, lastThreePlayedCards, playCardsStates, userCardsAndScrollInfo, cardState) ->
        if (gameId.isEmpty()) {
            flowOf(PlayCardsUIStates.Loading)
        }

        val playerScreenInfo = playersScreenInfo(currGameId = gameId, cardState = cardState)

        val showCurrentCardTypeIfNecessary = showCurrentCardTypeIfNecessary(
            currGameId = gameId,
            recentCardType = cardRepository.getCurrentCardType(),
            recentCardState = cardState,
            playCardState = playCardsStates
        )

        val showDefineJokerActions = when (playCardsStates) {
            is GamePlayerActions.PromptPlayerDefineJokerCardAction -> {
                true
            }

            else -> {
                false
            }
        }

        val showChooseCardTypeScreen = when (playCardsStates) {
            is GamePlayerActions.PromptPlayerChangeCardType -> {
                true
            }

            else -> {
                false
            }
        }

        val showAllowOthersPlayView = when (playCardsStates) {
            is GamePlayerActions.PromptPlayerPlayOrPick -> {
                !playCardsStates.canPick
            }

            else -> {
                false
            }
        }

        flowOf(
            PlayCardsUIStates.PlayingScreen(
                playersScreenInfo = playerScreenInfo,
                dustbinImageUrl = "",
                lastThreePlayedCards = lastThreePlayedCards,
                durationText = "",
                messages = gameActionMessages(state = cardState),
                userCards = userCardsAndScrollInfo,
                allowOthersPlay = showAllowOthersPlayView,
                showChooseCardTypeScreen = showChooseCardTypeScreen,
                showDefineJokerActions = showDefineJokerActions,
                showCurrentCardType = Pair(
                    showCurrentCardTypeIfNecessary.isNotEmpty(),
                    showCurrentCardTypeIfNecessary
                )
            )
        )

    }

    private suspend fun showCurrentCardTypeIfNecessary(
        currGameId: String,
        recentCardType: String,
        recentCardState: CardStates,
        playCardState: GamePlayerActions
    ): String {
        var type = ""
        return try {
            if (currGameId.isEmpty()) {
                throw Exception("Game not available")
            }
            val playerIds =
                gameRepository.getGameWithPlayers(currGameId).first().players.map { it.uniqueId }
            val topPlayedCardName =
                gameCardRepository.getTopPlayedCard(
                    gameId = currGameId,
                    playerIds = playerIds
                ).first()?.card?.name ?: throw Exception("Top card not found")

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
                playerId == userRepository.getAndroidUserId()
            }

            else -> false
        }
    }

    private suspend fun playersScreenInfo(
        currGameId: String,
        cardState: CardStates
    ): List<PlayerScreenInfo> {
        return try {
            if (currGameId.isEmpty()) {
                throw Exception("Game not available")
            }
            val userId = userRepository.getPlayerSignInDetails().first().uniqueId

            val playersIds = gameRepository.getGameWithPlayers(currGameId)
                .first()
                .players
                .map {
                    if (it.uniqueId == userId) {
                        it.copy(profileName = "You")
                    } else {
                        it
                    }
                }
                .sortedBy {
                    it.profileName.length
                }

            val smallLength = playersIds.first().profileName.length

            playersIds.map { player ->

                val numOfCardsLeft = gameCardRepository.numOfUnPlayedCardsForPlayer(
                    gameId = currGameId,
                    playerId = player.uniqueId
                ).first()
                    .toString()
                val playerName = player.profileName.slice(0 until smallLength)

                PlayerScreenInfo(
                    playerId = player.uniqueId,
                    defaultAvatar = R.drawable.game_test_profile,
                    numOfCardsLeft = numOfCardsLeft,
                    playerName = playerName,
                    isCurrentlyPlaying = isUserTurn(
                        playerId = player.uniqueId,
                        cardState = cardState
                    ),
                    imageUrl = if (player.imageUrl.isEmpty()) null else player.imageUrl
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
        }
    }

    fun onAllowOthersPlayBtnClick() = viewModelScope.launch(defaultCoroutineDispatcher) {
        loggedUserCardsController.userRequestToAllowOthersPlay(user.first().uniqueId)
    }

    fun onUserCardClick(userCard: UserCard) = viewModelScope.launch(defaultCoroutineDispatcher) {

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
                    it.copy(error = "Hold on, it might not be your turn event: $currentCardState")
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
                            error = "Unexpected behaviour, user has to define joker action"
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
                    error = response.message
                )
            }
        }
    }

    fun onDustbinClick() = viewModelScope.launch(defaultCoroutineDispatcher) {

        // pick cards
        val currentCardState = preference.recentCardState.first()
        val playerId = user.first().uniqueId

        val response = loggedUserCardsController.userRequestToPickCards(
            currentCardState = currentCardState,
            playerId = playerId
        )

        if (response is Response.Error) {
            playCardsStateFlow.update {
                it.copy(error = response.message)
            }
        }
    }

    private suspend fun pauseGameAndReleaseResources() {
        val currentGameId = gameRepository.getCurrentGameId().first()
        if (currentGameId.isNotEmpty()) {
            pauseGameUseCase.pauseGame(currGameId = currentGameId, _gameDuration.value.second)
        }
    }

    fun onCloseNavigationIconClicked(
        onPopBack: () -> Unit = {}
    ) = viewModelScope.launch {
        pauseGameAndReleaseResources()
        onPopBack()
    }

    sealed class PlayCardsUIStates {
        object Loading : PlayCardsUIStates()
        object WinnerScreen : PlayCardsUIStates()
        data class PlayingScreen(
            val playersScreenInfo: List<PlayerScreenInfo>,//done
            val dustbinImageUrl: String, //later
            val lastThreePlayedCards: List<UserCard>,//done
            val durationText: String,//later
            val messages: CardsActionMessage, //done
            val userCards: Pair<List<UserCard>, UserCardScrollInfo>,//done
            val allowOthersPlay: Boolean,
            val showChooseCardTypeScreen: Boolean,
            val showDefineJokerActions: Boolean,
            val showCurrentCardType: Pair<Boolean, String> // done
        ) : PlayCardsUIStates()
    }
}