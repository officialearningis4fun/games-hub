package com.learningis4fun.gameshub.presentation.create_game.online

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learningis4fun.gameshub.api.CrusadeGameService
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.model.User
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.PlayerInfo
import com.learningis4fun.gameshub.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.data.data_source.remote.GoogleAuthUiClient
import com.learningis4fun.gameshub.presentation.start.CompletedGamesState
import com.learningis4fun.gameshub.presentation.start.friends.CreateOnlineGameState
import com.learningis4fun.gameshub.presentation.start.friends.FriendsStartEvents
import com.learningis4fun.gameshub.presentation.start.friends.FriendsStartState
import com.learningis4fun.gameshub.presentation.start.friends.OngoingGamesOrCreateNewGameState
import com.learningis4fun.gameshub.util.LogTags
import com.learningis4fun.gameshub.util.getDataOrThrowException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@ExperimentalCoroutinesApi
@HiltViewModel
class CreateOnlineGameViewModel @Inject constructor(
    private val crusadeGameService: CrusadeGameService,
    private val googleAuthUiClient: GoogleAuthUiClient,
    private val preferencesManager: CrusadeCardsPreferencesManager
) : ViewModel() {

    private val createOnlineGameStateFlow = MutableStateFlow(CreateOnlineGameState())
    private val searchQueryFlow = MutableStateFlow("")
    private val isSearchedUserNotAFriendFlow = MutableStateFlow<PlayerInfo?>(null)
    val isSearchedUserNotAFriend = isSearchedUserNotAFriendFlow.asStateFlow()


    val searchQuery = searchQueryFlow.asStateFlow()
    val createNewFriendsGameState = createOnlineGameStateFlow.asStateFlow()

    val handler = CoroutineExceptionHandler { _, throwable ->
        Log.d(LogTags.OngoingGamesOrCreateNewGameViewModel, "throwable: $throwable")
    }

    init {

        viewModelScope.launch(handler) {
            createOnlineGameStateFlow.collectLatest {
                Log.d(LogTags.FRIENDS_START_VIEW_MODEL, "userInfo: ${it.userInfo}")
            }
        }

        viewModelScope.launch(handler) {
            googleAuthUiClient.signedInUser.collectLatest { user ->
                if (user == null) {
                    createOnlineGameStateFlow.update {
                        it.copy(
                            error = "Please signIn to create an online game"
                        )
                    }
                } else {
                    createOnlineGameStateFlow.update {
                        it.copy(
                            userInfo = PlayerInfo(
                                player = Player(
                                    uniqueId = user.id,
                                    email = user.email,
                                    profileName = user.name ?: "YOU",
                                    imageUrl = user.photoUrl ?: "",
                                ),
                                canRemove = false,
                                includeInList = true
                            )
                        )
                    }
                    createOnlineGameStateFlow.update {
                        it.copy(
                            error = null,
                        )
                    }
                }
            }
        }

        viewModelScope.launch(handler) {
            combine(
                preferencesManager.numberOfCardsPerPlayer,
                preferencesManager.eliminationCountLimitForCrusade
            ) { numOfCardsPerPlayer, eliminationLimit ->
                numOfCardsPerPlayer to eliminationLimit
            }.collectLatest { (numOfCardsPerPlayer, eliminationLimit) ->
                createOnlineGameStateFlow.update {
                    it.copy(
                        numberOfCardsPerPlayer = numOfCardsPerPlayer,
                        eliminationLimitForCrusade = eliminationLimit
                    )
                }
            }
        }

        viewModelScope.launch(handler) {
            searchQueryFlow.collectLatest { query ->
                Log.d(LogTags.FRIENDS_START_VIEW_MODEL, "query: $query")
                if (query.isEmpty()) {
                    isSearchedUserNotAFriendFlow.update { null }
                    Log.d(LogTags.FRIENDS_START_VIEW_MODEL, "query: $query")
                    return@collectLatest
                }
                val isFriend = createOnlineGameStateFlow.value.playerInfos.find {
                    it.player.email.equals(
                        query,
                        ignoreCase = true
                    )
                }
                Log.d(LogTags.FRIENDS_START_VIEW_MODEL, "friend found: $isFriend")
                if (isFriend == null) {
                    isSearchedUserNotAFriendFlow.update {
                        PlayerInfo(
                            player = Player(
                                uniqueId = "",
                                email = query,
                            ),
                            canRemove = false,
                            includeInList = false
                        )
                    }
                    Log.d(
                        LogTags.FRIENDS_START_VIEW_MODEL,
                        "isNotAFriend: ${isSearchedUserNotAFriendFlow.value}"
                    )
                }
            }
        }

        viewModelScope.launch(handler) {
            crusadeGameService.getFriends().collectLatest { players ->
                createOnlineGameStateFlow.update {
                    it.copy(
                        playerInfos = players.map { player ->
                            PlayerInfo(
                                player = player,
                                canRemove = false,
                                includeInList = false
                            )
                        }
                    )
                }
            }
        }

    }

    fun onCreateOnlineGame(
        onSuccess: (String) -> Unit
    ){
        viewModelScope.launch(handler) {
            try {
                createOnlineGameStateFlow.update {
                    it.copy(
                        createGameLoading = true,
                        enableCreateGameButton = false
                    )
                }
               val gameId = crusadeGameService.createGame(
                    opponentIds = createOnlineGameStateFlow.value.playerInfos.mapNotNull {
                        if (it.includeInList) {
                            it.player.uniqueId
                        } else {
                            null
                        }
                    }
                ).getDataOrThrowException()
                onSuccess(gameId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
            } finally {
                createOnlineGameStateFlow.update {
                    it.copy(
                        createGameLoading = false,
                        enableCreateGameButton = true
                    )
                }
            }

        }
    }

    fun onEvent(event: CreateOnlineGameEvents) {
        when (event) {

            CreateOnlineGameEvents.DecreaseEliminationLimitForCrusade -> {
                viewModelScope.launch {
                    preferencesManager.updateEliminationCountLimitForCrusade(
                        createOnlineGameStateFlow.value.eliminationLimitForCrusade - 1
                    )
                }
            }

            CreateOnlineGameEvents.IncreaseEliminationLimitForCrusade -> {
                viewModelScope.launch {
                    preferencesManager.updateEliminationCountLimitForCrusade(
                        createOnlineGameStateFlow.value.eliminationLimitForCrusade + 1
                    )
                }
            }

            CreateOnlineGameEvents.DecreaseNumberOfCards -> {
                viewModelScope.launch {
                    val numberOfCardsPerPlayer =
                        createOnlineGameStateFlow.value.numberOfCardsPerPlayer - 1
                    if (numberOfCardsPerPlayer >= Constants.NumberOfCardsOptions.DEFAULT_CARDS_PER_PLAYER) {
                        preferencesManager.updateNumberOfCardsPerPlayer(numberOfCardsPerPlayer)
                    }
                }
            }

            CreateOnlineGameEvents.IncreaseNumberOfCards -> {
                viewModelScope.launch {
                    val numberOfCardsPerPlayer =
                        createNewFriendsGameState.value.numberOfCardsPerPlayer + 1
                    if (numberOfCardsPerPlayer <= Constants.NumberOfCardsOptions.HIGHEST_CARDS_PER_PLAYER) {
                        preferencesManager.updateNumberOfCardsPerPlayer(numberOfCardsPerPlayer)
                    }
                }
            }

            is CreateOnlineGameEvents.CheckParticipantClick -> {

                createOnlineGameStateFlow.update {

                    val isCheck = !event.playerInfo.includeInList
                    it.copy(
                        playerInfos = it.playerInfos.map { playerInfo ->
                            if (playerInfo.player.uniqueId == event.playerInfo.player.uniqueId) {
                                playerInfo.copy(includeInList = isCheck)
                            } else {
                                playerInfo
                            }
                        }
                    ).updateNumberOfParticipants()
                }


            }

            CreateOnlineGameEvents.ClearSearchQuery -> {
                searchQueryFlow.update { "" }
            }

            is CreateOnlineGameEvents.UpdateSearchQuery -> {
                searchQueryFlow.update { event.query }
            }

            is CreateOnlineGameEvents.AddUserToFriends -> {
                viewModelScope.launch(handler) {
                    when (val response = crusadeGameService.addUserToFriends(event.email)) {
                        is Response.Error<*> -> {
                            Log.d(LogTags.FRIENDS_START_VIEW_MODEL, "error: ${response.message}")
                        }

                        is Response.Success<*> -> {
                            Log.d(LogTags.FRIENDS_START_VIEW_MODEL, "success")
                            searchQueryFlow.update { "" }
                        }
                    }
                }
            }

            CreateOnlineGameEvents.CreateGame -> {
                viewModelScope.launch(handler) {
                    try {
                        createOnlineGameStateFlow.update {
                            it.copy(
                                createGameLoading = true,
                                enableCreateGameButton = false
                            )
                        }
                        crusadeGameService.createGame(
                            opponentIds = createOnlineGameStateFlow.value.playerInfos.mapNotNull {
                                if (it.includeInList) {
                                    it.player.uniqueId
                                } else {
                                    null
                                }
                            }
                        ).getDataOrThrowException()
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        e.printStackTrace()
                    } finally {
                        createOnlineGameStateFlow.update {
                            it.copy(
                                createGameLoading = false,
                                enableCreateGameButton = true
                            )
                        }
                    }

                }
            }
        }
    }
}