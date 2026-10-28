package com.learningis4fun.gameshub.presentation.start.friends

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.learningis4fun.gameshub.api.CrusadeGameService
import com.learningis4fun.gameshub.api.use_cases.SetupGameUseCase
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.CreateNewGameToolsEvents
import com.learningis4fun.gameshub.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.util.GamesHubLogTags
import com.learningis4fun.gameshub.util.getDataOrThrowException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CreateNewFriendsGameViewModel @OptIn(ExperimentalCoroutinesApi::class)
@Inject constructor(
    private val preferences: CrusadeCardsPreferencesManager,
    private val setupGameUseCase: SetupGameUseCase,
    private val crusadeGameService: CrusadeGameService
) : ViewModel() {
    private val createNewGameToolsStateFlow = MutableStateFlow(CreateNewFriendsGameState())
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
                        numberOfCardsPerPlayer = numOfCardsPerPlayer,
                        eliminationLimitForCrusade = eliminationLimit
                    )
                }
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


            }

            is CreateNewGameToolsEvents.RemoveParticipant -> {

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

                crusadeGameService.createGame().getDataOrThrowException()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                createNewGameToolsStateFlow.update {
                    it.copy(createGameLoading = false)
                }
            }
        }
    }

}