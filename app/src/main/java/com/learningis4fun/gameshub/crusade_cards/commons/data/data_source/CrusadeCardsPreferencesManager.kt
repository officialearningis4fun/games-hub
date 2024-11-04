package com.learningis4fun.gameshub.crusade_cards.commons.data.data_source

import com.learningis4fun.gameshub.MainViewModel
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.CardStateEntity
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

    val device1: Flow<Player>
    val device2: Flow<Player>
    val device3: Flow<Player>

    val numberOfCardsPerPlayer: Flow<Int>

    val numberOfParticipantsInCrusadeGame: Flow<Int>

    val eliminationCountLimitForCrusade: Flow<Int>

    suspend fun updatePlayer(player: Player): Response<Unit>

    suspend fun updateAndroidPlayer(player: Player): Response<Unit>

    suspend fun updateRecentCardState(stateEntity: CardStateEntity)

    suspend fun updateUserId(playerId: String): Response<Unit>

    suspend fun updateUserCardsSortBy(sortBy: String): Response<Unit>

    suspend fun updateRecentCardType(recentType: String): Response<Unit>

    suspend fun updateRecentGameId(gameId: String): Response<Unit>

    suspend fun updateNumberOfCardsPerPlayer(numOfCards: Int): Response<Unit>
    suspend fun updateEliminationCountLimitForCrusade(count: Int): Response<Unit>
    suspend fun decreaseNumberOfCardsPerPlayer(): Response<Unit>
    suspend fun increaseNumberOfCardsPerPlayer(): Response<Unit>
    suspend fun updateNumberOfParticipantsInCrusadeGame(numOfParticipants: Int): Response<Unit>


}