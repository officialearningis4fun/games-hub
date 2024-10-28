package com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.add_participants

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.CardNumInfo
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.ObservableList
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class AddParticipantsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val gameRepository: GameRepository
) :ViewModel(){

    val user = userRepository.getPlayerSignInDetails()
    val androidUser = userRepository.getDevice1PlayerSignInDetails()

    val _gameDuration = MutableStateFlow(Pair(false, 0L))
    private var lastCurrentTime = 0L
    val gameDuration: LiveData<String> = _gameDuration
        .map {
            val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            timeFormat.format(it.second)
        }.asLiveData()

    val _numberOfCards = MutableStateFlow(Constants.DEFAULT_CARDS_PER_USER)

    fun changeNumberOfCardsToAssign(numOfCardsToAssign: Int) = viewModelScope.launch {
        _numberOfCards.value = numOfCardsToAssign
    }

    val addedParticipants: LiveData<List<Player>>
        get() = _addedParticipants.asLiveData()

    val participants = MutableStateFlow(emptyList<Player>())
    val _valuesOfCardsToAssign = MutableStateFlow(ObservableList<CardNumInfo>())

    val _addedParticipants: Flow<List<Player>> =
        gameRepository.getCurrentGameId().flatMapLatest { gameId ->
            if (gameId.isEmpty()) {

                if (participants.value.isEmpty()) {
                    defaultParticipants()
                }

                participants
            } else {
                gameRepository.getGameWithPlayers(gameId).map {
                    it.players
                }
            }
        }

    val _minAndMaxCardsToAssign: Flow<Pair<Int, Int>> = _addedParticipants.map {
        val numOfParticipants = it.size

        when (numOfParticipants) {
            in 3..5 -> Pair(
                first = 3,
                second = 5
            )
            else -> Pair(
                first = 5,
                second = 10
            )
        }
    }

    val valuesOfNumberOfCardsToAssign: LiveData<List<CardNumInfo>> =
        combine(_minAndMaxCardsToAssign, _valuesOfCardsToAssign)
        { minAndMax, cardNumInfos ->
            Pair(minAndMax, cardNumInfos)
        }.map { (minAndMax, cardNumInfos) ->
            val numOfCardsToAssign = (minAndMax.first..minAndMax.second).toList()
            // check if list changed
            if (cardNumInfos.data.size != numOfCardsToAssign.size) {
                numOfCardsToAssign.map {
                    if (it == 5) {
                        CardNumInfo(selected = true, number = it)
                    } else {
                        CardNumInfo(number = it)
                    }

                }
            } else {
                cardNumInfos.data
            }
        }.asLiveData()

    suspend fun defaultParticipants(): List<Player> {
        val user = this.user.first()
        val computer = androidUser.first()
        return listOf(
            user,
            computer
        )
    }

    fun numberOfCardsToAssignChanged(cardNumInfo: CardNumInfo) = viewModelScope.launch {
        if (!cardNumInfo.selected) {
            val setNumOfCards = cardNumInfo.number
            val allCardNumInfo = _valuesOfCardsToAssign.value.data.toMutableList()
            val change = _valuesOfCardsToAssign.value.changed
            val selectedItem = allCardNumInfo.find { it.number == setNumOfCards }
            selectedItem?.let {
                val indexOfSelectedItem = allCardNumInfo.indexOf(selectedItem)
                allCardNumInfo[indexOfSelectedItem] = selectedItem.copy(selected = true)
                _valuesOfCardsToAssign.value =
                    ObservableList(data = allCardNumInfo.toList(), !change)
            }
        }
    }

    val minAndMaxCardsToAssign: LiveData<Pair<Int, Int>> = _minAndMaxCardsToAssign.asLiveData()
}