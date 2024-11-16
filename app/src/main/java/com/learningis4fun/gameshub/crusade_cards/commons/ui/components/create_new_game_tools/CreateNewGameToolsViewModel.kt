package com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.playerName
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case.SetupGameUseCase
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util.GamesHubLogTags
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util.getDataOrThrowException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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

    init {

        viewModelScope.launch {
            preferences.numberOfCardsPerPlayer.collectLatest { numberOfCards ->
                createNewGameToolsStateFlow.update {
                    it.copy(numberOfCardsPerPlayer = numberOfCards)
                }
            }
        }

        viewModelScope.launch {

            preferences.eliminationCountLimitForCrusade.collectLatest { eliminationLimit ->
                createNewGameToolsStateFlow.update {
                    it.copy(
                        eliminationLimitForCrusade = eliminationLimit
                    )
                }
            }
        }


        viewModelScope.launch {
            preferences.numberOfParticipantsInCrusadeGame.collectLatest { numOfParticipants ->
                Log.d(
                    GamesHubLogTags.CREATE_NEW_GAME_VIEWMODEL,
                    "numOfParticipants from db:$numOfParticipants; numOfParticipants in viewmodel:${createNewGameToolsStateFlow.value.numberOfParticipants}"
                )

                if (createNewGameToolsStateFlow.value.playersInfo.isEmpty()) {
                    val loggedInUserId = preferences.user.first().uniqueId
                    createNewGameToolsStateFlow.update {
                        it.copy(
                            playersInfo = getPlayers(
                                count = numOfParticipants,
                                userId = loggedInUserId
                            )
                        ).updateNumberOfParticipants()
                    }
                }

            }
        }

        viewModelScope.launch {
            createNewGameToolsStateFlow.collectLatest {
                Log.d(GamesHubLogTags.CREATE_NEW_GAME_VIEWMODEL, "state: $it")
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
                createNewGameToolsStateFlow.update {
                    val firstUnCheckedPlayer =
                        it.playersInfo.firstOrNull { player -> player.includeInList.not() }

                    it.copy(
                        playersInfo = it.playersInfo.map { playerInfo ->
                            if (playerInfo.player.uniqueId == firstUnCheckedPlayer?.player?.uniqueId) {
                                firstUnCheckedPlayer.copy(
                                    includeInList = true
                                )
                            } else {
                                playerInfo
                            }
                        }
                    ).updateNumberOfParticipants()
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
        }
    }

    fun setupNewGameOnStartClick(onGameCreated: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                createNewGameToolsStateFlow.update {
                    it.copy(createGameLoading = true)
                }
                // update number of participants
                val numberOfParticipants = createNewGameToolsStateFlow.value.availablePlayers.size

                preferences.updateNumberOfParticipantsInCrusadeGame(numberOfParticipants)

                val players = createNewGameToolsStateFlow
                    .value
                    .availablePlayers
                    .map { it.player }

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

    private suspend fun getPlayers(count: Int, userId: String): List<PlayerInfo> {
        return try {
            listOf(
                preferences.user.first(),
                preferences.device1.first(),
                preferences.device2.first(),
                preferences.device3.first(),
            ).mapIndexed { index, player ->
                PlayerInfo(
                    player = player.copy(
                        profileName = player.playerName(userId)
                    ),
                    canRemove = player.uniqueId != userId,
                    includeInList = index + 1 <= count
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}