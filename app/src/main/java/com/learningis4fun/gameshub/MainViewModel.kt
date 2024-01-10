package com.learningis4fun.gameshub

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.learningis4fun.gameshub.feature_crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.CardStates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: DefaultCardRepository,
    private val preference: CrusadeCardsPreferencesManager,
) : ViewModel() {

    val _gameDuration = MutableStateFlow(Pair(false, 0L))
    private var lastCurrentTime = 0L
    val gameDuration: LiveData<String> = _gameDuration
        .map {
            val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            timeFormat.format(it.second)
        }.asLiveData()

    val currCardState: Flow<CardStates>
        get() = preference.recentCardState

    sealed class UserCardsSortBy(val name: String) {
        object CardType : UserCardsSortBy("card type")
        object CardName : UserCardsSortBy("card name")
        object TimePicked : UserCardsSortBy("time picked")
    }

    fun uhfh(){
        UserCardsSortBy.CardName.name
    }


}