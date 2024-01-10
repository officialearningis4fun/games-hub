package com.learningis4fun.gameshub.feature_crusade_cards.commons.data.data_source

import com.learningis4fun.gameshub.MainViewModel
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.CardStateEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

@ExperimentalCoroutinesApi
interface CrusadeCardsPreferencesManager {

    val recentCardType: Flow<String>

    val recentGameId: Flow<String>

    val recentCardState: Flow<CardStates>

    val userCardsSortBy: Flow<MainViewModel.UserCardsSortBy>

    val userId: Flow<String?>

    val user: Flow<Player>

    val AndroidUser: Flow<Player>

    suspend fun updatePlayer(player: Player): Response<String>

    suspend fun updateAndroidPlayer(player: Player): Response<String>

    suspend fun updateRecentCardState(stateEntity: CardStateEntity)

    suspend fun updateUserId(playerId: String): Response<String>

    suspend fun updateUserCardsSortBy(sortBy: String): Response<String>

    suspend fun updateRecentCardType(recentType: String): Response<String>

    suspend fun updateRecentGameId(gameId: String): Response<String>

}