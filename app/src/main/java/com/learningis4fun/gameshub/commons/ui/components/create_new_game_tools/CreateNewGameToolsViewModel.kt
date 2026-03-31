package com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.domain.util.playerName
import com.learningis4fun.gameshub.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.domain.use_cases.SetupGameUseCase
import com.learningis4fun.gameshub.util.FirebaseLog
import com.learningis4fun.gameshub.util.GamesHubLogTags
import com.learningis4fun.gameshub.util.getDataOrThrowException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CreateNewGameToolsViewModel @OptIn(ExperimentalCoroutinesApi::class)
@Inject constructor(
    private val preferences: CrusadeCardsPreferencesManager,
    private val setupGameUseCase: SetupGameUseCase
) : ViewModel() {

    private val createNewGameToolsStateFlow = MutableStateFlow(CreateNewGameToolsState())
    val createNewGameToolsState = createNewGameToolsStateFlow.asStateFlow()

    private val analytics = Firebase.analytics

    init {

        viewModelScope.launch {
            combine(
                preferences.onlineOfflineMode,
                preferences.numberOfCardsPerPlayer,
                preferences.eliminationCountLimitForCrusade
            ) { mode, numOfCardsPerPlayer, eliminationLimit ->
                Triple(
                    mode,
                    numOfCardsPerPlayer,
                    eliminationLimit
                )
            }.collectLatest { (mode, numOfCardsPerPlayer, eliminationLimit) ->
                Log.d(GamesHubLogTags.CREATE_NEW_GAME_VIEWMODEL, "mode in V: $mode")
                createNewGameToolsStateFlow.update {
                    it.copy(
                        mode = mode,
                        numberOfCardsPerPlayer = numOfCardsPerPlayer,
                        eliminationLimitForCrusade = eliminationLimit
                    )
                }
            }
        }

        viewModelScope.launch {
            try {
                val loggedInUser = preferences.user.first()

                val openAi = preferences.openAI.first()

                val loggedInUserId = loggedInUser.uniqueId

                val playersWithAI = listOf(
                    loggedInUser.copy(
                        profileName = loggedInUser.playerName(loggedInUserId)
                    ),
                    openAi.copy(
                        profileName = openAi.playerName(loggedInUserId)
                    )
                )

                val playersInfo = getPlayers(
                    userId = loggedInUserId
                )

                createNewGameToolsStateFlow.update {
                    it.copy(
                        newGameWithAIState = it.newGameWithAIState.copy(
                            players = playersWithAI
                        ),
                        playersInfo = playersInfo
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onEvent(event: CreateNewGameToolsEvents) {
        when (event) {
            CreateNewGameToolsEvents.DecreaseNumberOfCards -> {
                viewModelScope.launch {
                    val numberOfCardsPerPlayer =
                        createNewGameToolsStateFlow.value.numberOfCardsPerPlayer - 1
                    if (numberOfCardsPerPlayer >= Constants.NumberOfCardsOptions.DEFAULT_CARDS_PER_PLAYER) {
                        preferences.updateNumberOfCardsPerPlayer(numberOfCardsPerPlayer)
                    }
                }
            }

            CreateNewGameToolsEvents.IncreaseNumberOfCards -> {
                viewModelScope.launch {
                    val numberOfCardsPerPlayer =
                        createNewGameToolsStateFlow.value.numberOfCardsPerPlayer + 1
                    if (numberOfCardsPerPlayer <= Constants.NumberOfCardsOptions.HIGHEST_CARDS_PER_PLAYER) {
                        preferences.updateNumberOfCardsPerPlayer(numberOfCardsPerPlayer)
                    }
                }
            }

            CreateNewGameToolsEvents.DecreaseEliminationLimitForCrusade -> {
                viewModelScope.launch {
                    preferences.updateEliminationCountLimitForCrusade(
                        createNewGameToolsStateFlow.value.eliminationLimitForCrusade - 1
                    )
                }
            }

            CreateNewGameToolsEvents.IncreaseEliminationLimitForCrusade -> {
                viewModelScope.launch {
                    preferences.updateEliminationCountLimitForCrusade(
                        createNewGameToolsStateFlow.value.eliminationLimitForCrusade + 1
                    )
                }
            }

            CreateNewGameToolsEvents.AddParticipant -> {

                viewModelScope.launch {
                    createNewGameToolsStateFlow.update {
                        val firstUnCheckedPlayer =
                            it.playersInfo.firstOrNull { player -> player.includeInList.not() }

                        val playerInfos = it.playersInfo.map { playerInfo ->
                            if (playerInfo.player.uniqueId == firstUnCheckedPlayer?.player?.uniqueId) {
                                firstUnCheckedPlayer.copy(
                                    includeInList = true
                                )
                            } else {
                                playerInfo
                            }
                        }
                        it.copy(
                            playersInfo = playerInfos,
                        ).updateNumberOfParticipants()
                    }
                }

            }

            is CreateNewGameToolsEvents.RemoveParticipant -> {
                createNewGameToolsStateFlow.update {

                    it.copy(
                        playersInfo = it.playersInfo.map { playerInfo ->
                            if (playerInfo.player.uniqueId == event.player.uniqueId) {
                                playerInfo.copy(includeInList = false)
                            } else {
                                playerInfo
                            }
                        }
                    ).updateNumberOfParticipants()
                }
            }

            is CreateNewGameToolsEvents.SwitchMode -> {
                viewModelScope.launch {
                    preferences.saveOnlineOfflineMode(event.mode)
                }
            }
        }
    }

    fun setupNewGameOnStartClick(onGameCreated: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {

                val sessionID = preferences.getGameSessionId()

                analytics.logEvent(FirebaseLog.LogName.NEW_OFFLINE_GAME_CREATED) {
                    param(FirebaseLog.LogTag.SESSION_ID, sessionID)
                }

                createNewGameToolsStateFlow.update {
                    it.copy(createGameLoading = true)
                }
                // save chosen players
                // update number of participants

                val players = createNewGameToolsStateFlow
                    .value
                    .availablePlayers
                    .map { it.player }

                val prePlayersIds =
                    players.map { it.uniqueId }
                preferences.savePrevPlayersIds(prePlayersIds).getDataOrThrowException()

                // create a game with the selected participants
                val gameId = setupGameUseCase(
                    players = players
                ).getDataOrThrowException()
                onGameCreated(gameId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                createNewGameToolsStateFlow.update {
                    it.copy(createGameLoading = false)
                }
            }
        }
    }

    fun setupNewGameWithAIOnStartClick(onGameCreated: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val sessionID = preferences.getGameSessionId()

                analytics.logEvent(FirebaseLog.LogName.NEW_GAME_WITH_OPENAI_CREATED) {
                    param(FirebaseLog.LogTag.SESSION_ID, sessionID)
                }

                createNewGameToolsStateFlow.update {
                    it.copy(createGameLoading = true)
                }
                // save chosen players
                // update number of participants

                val players = createNewGameToolsStateFlow
                    .value
                    .newGameWithAIState
                    .players

                // create a game with the selected participants
                val gameId = setupGameUseCase(
                    players = players
                ).getDataOrThrowException()
                onGameCreated(gameId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                createNewGameToolsStateFlow.update {
                    it.copy(createGameLoading = false)
                }
            }
        }
    }

    private suspend fun getPlayers(userId: String): List<PlayerInfo> {
        return try {
            val players = listOf(
                preferences.user.first(),
                preferences.device1.first(),
                preferences.device2.first(),
                preferences.device3.first(),
                // preferences.geminiAI.first()
            ).mapIndexed { index, player ->
                PlayerInfo(
                    player = player.copy(
                        profileName = player.playerName(userId)
                    ),
                    canRemove = player.uniqueId != userId,
                    includeInList = index < 2
                )
            }
            val prevPlayersIds = preferences.prePlayersIds.first()

            if (prevPlayersIds.isEmpty()) {
                players.mapIndexed { index, playerInfo ->
                    playerInfo.copy(
                        player = playerInfo.player.copy(
                            profileName = playerInfo.player.playerName(userId)
                        ),
                        canRemove = playerInfo.player.uniqueId != userId,
                        includeInList = index < 2
                    )
                }
            } else {
                players.map { playerInfo ->
                    playerInfo.copy(
                        player = playerInfo.player.copy(
                            profileName = playerInfo.player.playerName(userId)
                        ),
                        canRemove = playerInfo.player.uniqueId != userId,
                        includeInList = playerInfo.player.uniqueId in prevPlayersIds
                    )
                }
            }


        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}