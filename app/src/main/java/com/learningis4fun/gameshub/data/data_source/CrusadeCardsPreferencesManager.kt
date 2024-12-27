package com.learningis4fun.gameshub.data.data_source

import com.learningis4fun.gameshub.app.MainViewModel
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.util.CardStates
import com.learningis4fun.gameshub.commons.domain.util.Response
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

    val geminiAI: Flow<Player>

    val numberOfCardsPerPlayer: Flow<Int>

    val numberOfParticipantsInCrusadeGame: Flow<Int>

    val eliminationCountLimitForCrusade: Flow<Int>

    val prePlayersIds: Flow<List<String>>

    suspend fun savePrevPlayersIds(playerIds: List<String>): Response<Unit>

    suspend fun updatePlayer(player: Player): Response<Unit>

    suspend fun updateRecentCardState(stateEntity: com.learningis4fun.gameshub.domain.model.CardStateEntity)

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