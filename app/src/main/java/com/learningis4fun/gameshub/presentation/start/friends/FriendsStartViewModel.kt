package com.learningis4fun.gameshub.presentation.start.friends

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learningis4fun.gameshub.api.CrusadeGameService
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.model.User
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.domain.util.GameState
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.PlayerInfo
import com.learningis4fun.gameshub.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.data.data_source.remote.GoogleAuthUiClient
import com.learningis4fun.gameshub.presentation.start.CompletedGamesState
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
class FriendsStartViewModel @Inject constructor(
    private val crusadeGameService: CrusadeGameService,
    private val googleAuthUiClient: GoogleAuthUiClient,
    private val preferencesManager: CrusadeCardsPreferencesManager
) : ViewModel() {

    private val completedGamesStateFlow = MutableStateFlow(CompletedGamesState())
    private val friendsStartStateFlow = MutableStateFlow(FriendsStartState())
    private val createNewFriendsGameStateFlow = MutableStateFlow(CreateNewFriendsGameState())
    private val ongoingGamesOrCreateNewGameStateFlow =
        MutableStateFlow<OngoingGamesOrCreateNewGameState>(OngoingGamesOrCreateNewGameState.Loading)
    private val searchQueryFlow = MutableStateFlow("")
    private val isSearchedUserNotAFriendFlow = MutableStateFlow<PlayerInfo?>(null)
    val isSearchedUserNotAFriend = isSearchedUserNotAFriendFlow.asStateFlow()


    val searchQuery = searchQueryFlow.asStateFlow()
    val ongoingGamesOrCreateNewGameState = ongoingGamesOrCreateNewGameStateFlow.asStateFlow()
    val friendsStartState = friendsStartStateFlow.asStateFlow()
    val createNewFriendsGameState = createNewFriendsGameStateFlow.asStateFlow()

    val handler = CoroutineExceptionHandler { _, throwable ->
        Log.d(LogTags.OngoingGamesOrCreateNewGameViewModel, "throwable: $throwable")
    }

    init {
        viewModelScope.launch(handler) {
            crusadeGameService.getCompletedGames()
                .collectLatest { completedGames ->
                    completedGamesStateFlow.update {
                        it.copy(completedGames = completedGames, isLoading = false)
                    }
                }
        }

        viewModelScope.launch(handler) {
            completedGamesStateFlow
                .collectLatest { completed ->
                    val userId = getUser().id

                    friendsStartStateFlow.update {
                        it.copy(
                            completedGamesState = completed.copy(userId = userId),
                            isLoading = completed.isLoading
                        )
                    }
                }
        }

        viewModelScope.launch(handler) {

            crusadeGameService.getUnCompletedGames()
                .collect { ongoingGames ->

                    try {
                        // loading - default
                        // ongoing games
                        // create new game

                        val state = if (ongoingGames.isNotEmpty()) {
                            // show ongoing
                            OngoingGamesOrCreateNewGameState.OngoingGamesState(
                                games = ongoingGames
                            )
                        } else {
                            // show create new game
                            OngoingGamesOrCreateNewGameState.CreateNewGameToolsState
                        }
                        Log.d(LogTags.OngoingGamesOrCreateNewGameViewModel, "state: $state")

                        ongoingGamesOrCreateNewGameStateFlow.update {
                            state
                        }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        e.printStackTrace()
                        ongoingGamesOrCreateNewGameStateFlow.update {
                            OngoingGamesOrCreateNewGameState.CreateNewGameToolsState
                        }
                    }

                }
        }

        viewModelScope.launch(handler) {
            createNewFriendsGameStateFlow.collectLatest {
                Log.d(LogTags.FRIENDS_START_VIEW_MODEL, "userInfo: ${it.userInfo}")
            }
        }

        viewModelScope.launch(handler) {
            googleAuthUiClient.signedInUser.collectLatest { user ->
                if (user == null) {
                    friendsStartStateFlow.update {
                        it.copy(
                            error = "Not signed In"
                        )
                    }
                } else {
                    createNewFriendsGameStateFlow.update {
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
                    friendsStartStateFlow.update {
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
                createNewFriendsGameStateFlow.update {
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
                val isFriend = createNewFriendsGameStateFlow.value.playerInfos.find {
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
                createNewFriendsGameStateFlow.update {
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

    fun getUser(): User {
        return googleAuthUiClient.signedInUser.value ?: throw Exception("User not found")
    }

    fun onEvent(event: FriendsStartEvents) {
        when (event) {

            is FriendsStartEvents.DeleteGame -> {
                onDeleteGame(event.gameId)
            }

            FriendsStartEvents.DecreaseEliminationLimitForCrusade -> {
                viewModelScope.launch {
                    preferencesManager.updateEliminationCountLimitForCrusade(
                        createNewFriendsGameStateFlow.value.eliminationLimitForCrusade - 1
                    )
                }
            }

            FriendsStartEvents.IncreaseEliminationLimitForCrusade -> {
                viewModelScope.launch {
                    preferencesManager.updateEliminationCountLimitForCrusade(
                        createNewFriendsGameStateFlow.value.eliminationLimitForCrusade + 1
                    )
                }
            }

            FriendsStartEvents.DecreaseNumberOfCards -> {
                viewModelScope.launch {
                    val numberOfCardsPerPlayer =
                        createNewFriendsGameStateFlow.value.numberOfCardsPerPlayer - 1
                    if (numberOfCardsPerPlayer >= Constants.NumberOfCardsOptions.DEFAULT_CARDS_PER_PLAYER) {
                        preferencesManager.updateNumberOfCardsPerPlayer(numberOfCardsPerPlayer)
                    }
                }
            }

            FriendsStartEvents.IncreaseNumberOfCards -> {
                viewModelScope.launch {
                    val numberOfCardsPerPlayer =
                        createNewFriendsGameState.value.numberOfCardsPerPlayer + 1
                    if (numberOfCardsPerPlayer <= Constants.NumberOfCardsOptions.HIGHEST_CARDS_PER_PLAYER) {
                        preferencesManager.updateNumberOfCardsPerPlayer(numberOfCardsPerPlayer)
                    }
                }
            }

            is FriendsStartEvents.CheckParticipantClick -> {

                createNewFriendsGameStateFlow.update {

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

            FriendsStartEvents.ClearSearchQuery -> {
                searchQueryFlow.update { "" }
            }

            is FriendsStartEvents.UpdateSearchQuery -> {
                searchQueryFlow.update { event.query }
            }

            is FriendsStartEvents.AddUserToFriends -> {
                viewModelScope.launch(handler) {
                    when (val response = crusadeGameService.addUserToFriends(event.email)) {
                        is Response.Error<*> -> {
                            Log.d(LogTags.FRIENDS_START_VIEW_MODEL, "error: ${response.message}")
                        }

                        is Response.Success<*> -> {
                            Log.d(LogTags.FRIENDS_START_VIEW_MODEL, "success")
                        }
                    }
                }
            }

            FriendsStartEvents.CreateGame -> {
                viewModelScope.launch(handler) {
                    try {
                        createNewFriendsGameStateFlow.update {
                            it.copy(
                                createGameLoading = true,
                                enableCreateGameButton = false
                            )
                        }
                        crusadeGameService.createGame(
                            opponentIds = createNewFriendsGameStateFlow.value.playerInfos.mapNotNull {
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
                        createNewFriendsGameStateFlow.update {
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

    private fun onDeleteGame(gameId: String) {
        viewModelScope.launch {
            try {
                crusadeGameService.deleteGame(gameId).getDataOrThrowException()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}