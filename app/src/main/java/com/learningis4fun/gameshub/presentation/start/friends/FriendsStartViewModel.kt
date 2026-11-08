package com.learningis4fun.gameshub.presentation.start.friends

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learningis4fun.gameshub.api.CrusadeGameService
import com.learningis4fun.gameshub.commons.domain.model.User
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@ExperimentalCoroutinesApi
@HiltViewModel
class FriendsStartViewModel @Inject constructor(
    private val crusadeGameService: CrusadeGameService,
    private val googleAuthUiClient: GoogleAuthUiClient,
) : ViewModel() {

    private val completedGamesStateFlow = MutableStateFlow(CompletedGamesState())
    private val friendsStartStateFlow = MutableStateFlow(FriendsStartState())
    private val ongoingGamesOrCreateNewGameStateFlow =
        MutableStateFlow<OngoingGamesOrCreateNewGameState>(OngoingGamesOrCreateNewGameState.Loading)
    val ongoingGamesOrCreateNewGameState = ongoingGamesOrCreateNewGameStateFlow.asStateFlow()
    val friendsStartState = friendsStartStateFlow.asStateFlow()

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
                    Log.d(LogTags.FRIENDS_START_VIEW_MODEL, "ongoingGames: $ongoingGames")

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
    }

    fun getUser(): User {
        return googleAuthUiClient.signedInUser.value ?: throw Exception("User not found")
    }

    fun onEvent(event: FriendsStartEvents) {
        when (event) {

            is FriendsStartEvents.DeleteGame -> {
                onDeleteGame(event.gameId)
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