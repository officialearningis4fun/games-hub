package com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
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
class CreateNewGameToolsViewModel @OptIn(ExperimentalCoroutinesApi::class)
@Inject constructor(
    private val preferences: CrusadeCardsPreferencesManager
) : ViewModel() {

    private val createNewGameToolsStateFlow = MutableStateFlow(CreateNewGameToolsState())
    val createNewGameToolsState = createNewGameToolsStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferences.numberOfCardsPerPlayer,
                preferences.numberOfParticipantsInCrusadeGame,
                preferences.eliminationCountLimitForCrusade
            ) { numberOfCards, numOfParticipants, eliminationLimit ->
                Triple(
                    numberOfCards,
                    numOfParticipants,
                    eliminationLimit
                )
            }.collectLatest { (numberOfCards, numberOfParticipants, eliminationLimit) ->
                createNewGameToolsStateFlow.update {
                    CreateNewGameToolsState(
                        numberOfParticipants = numberOfParticipants,
                        numberOfCardsPerPlayer = numberOfCards,
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

            CreateNewGameToolsEvents.DecreaseNumberOfParticipants -> {
                viewModelScope.launch {
                    val numberOfParticipants =
                        createNewGameToolsStateFlow.value.numberOfParticipants - 1
                    if (numberOfParticipants >= Constants.DEFAULT_PARTICIPANTS_IN_CRUSADE_GAME) {
                        preferences.updateNumberOfParticipantsInCrusadeGame(numberOfParticipants)
                    }
                }
            }

            CreateNewGameToolsEvents.IncreaseNumberOfParticipants -> {
                viewModelScope.launch {
                    val numberOfParticipants =
                        createNewGameToolsStateFlow.value.numberOfParticipants + 1
                    if (numberOfParticipants <= Constants.MAX_PARTICIPANTS_IN_CRUSADE_GAME) {
                        preferences.updateNumberOfParticipantsInCrusadeGame(numberOfParticipants)
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
        }
    }
}