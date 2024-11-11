package com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.playerName
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case.AssignCardsUseCase
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case.CreateGameUseCase
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util.getDataOrThrowException
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
    private val gameRepository: GameRepository,
    private val createGameUseCase: CreateGameUseCase,
    private val assignCardsUseCase: AssignCardsUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val createNewGameToolsStateFlow = MutableStateFlow(CreateNewGameToolsState())
    val createNewGameToolsState = createNewGameToolsStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferences.numberOfCardsPerPlayer,
                preferences.eliminationCountLimitForCrusade
            ) { numberOfCards, eliminationLimit ->
                Pair(
                    numberOfCards,
                    eliminationLimit
                )
            }.collectLatest { (numberOfCards, eliminationLimit) ->

                createNewGameToolsStateFlow.update {
                    it.copy(
                        numberOfCardsPerPlayer = numberOfCards,
                        eliminationLimitForCrusade = eliminationLimit
                    )
                }
            }
        }

        viewModelScope.launch {
            preferences.numberOfParticipantsInCrusadeGame.collectLatest { numOfParticipants ->
                val loggedInUserId = preferences.user.first().uniqueId
                createNewGameToolsStateFlow.update {
                    it.copy(
                        playersInfo = getPlayers(count = numOfParticipants, userId = loggedInUserId),
                        numberOfParticipants = numOfParticipants
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
                    )
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

                // create a game with the selected participants
                val participants = createNewGameToolsStateFlow
                    .value
                    .availablePlayers
                    .mapIndexed { index, player -> Pair(player, index + 1) }

                val numOfCardsPerPlayer = preferences.numberOfCardsPerPlayer.first()

                participants.forEach { player ->
                    userRepository.savePlayer(player.first.player).getDataOrThrowException()
                }

                val gameId = createGameUseCase.createGame(
                    gameName = "",
                    participants = participants.map { Pair(it.first.player, it.second) }
                )
                // assign cards
                if (gameId.isEmpty()) {
                    throw Exception("Game identifier not available")
                }

                val assigned = assignCardsUseCase.assignCards(
                    gameId = gameId,
                    howManyCardsPerPlayer = numOfCardsPerPlayer
                )

                if (!assigned) {
                    throw Exception("Cards not assigned yet")
                }
                gameRepository.updateRecentGameId(gameId = gameId).getDataOrThrowException()
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