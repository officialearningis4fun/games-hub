package com.learningis4fun.gameshub.crusade_cards.common.data.data_source

import com.learningis4fun.gameshub.app.MainViewModel
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.util.CardStates
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.commons.domain.util.toCardState
import com.learningis4fun.gameshub.data.data_source.CrusadeCardsPreferencesManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ExperimentalCoroutinesApi
class FakeCrusadeCardsPreferenceManager(
    override val device2: Flow<Player>,
    override val device3: Flow<Player>,
    override val geminiAI: Flow<Player>,
    override val numberOfCardsPerPlayer: Flow<Int>,
    override val numberOfParticipantsInCrusadeGame: Flow<Int>,
    override val eliminationCountLimitForCrusade: Flow<Int>,
    override val prePlayersIds: Flow<List<String>>
) : CrusadeCardsPreferencesManager {

    var _recentCardType = ""
    var _recentGameId = ""
    var _recentCardState: CardStates = CardStates.Start
    var _userCardsSortBy: MainViewModel.UserCardsSortBy = MainViewModel.UserCardsSortBy.CardName
    var _userId = ""
    var _user = Player()
    var _androidUser = Player()

    override val recentCardType: Flow<String>
        get() = flowOf(_recentCardType)
    override val recentGameId: Flow<String>
        get() = flowOf(_recentGameId)
    override val recentCardState: Flow<CardStates>
        get() = flowOf(_recentCardState)
    override val userCardsSortBy: Flow<MainViewModel.UserCardsSortBy>
        get() = flowOf(_userCardsSortBy)
    override val userId: Flow<String?>
        get() = flowOf(_userId)
    override val user: Flow<Player>
        get() = flowOf(_user)
    override val device1: Flow<Player>
        get() = flowOf(_androidUser)

    override suspend fun savePrevPlayersIds(playerIds: List<String>): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun updatePlayer(player: Player): Response<Unit> {
        return try {
            _user = player
            Response.Success(data = Unit)
        } catch (exception: Exception) {
            Response.Error(message = exception.localizedMessage.toString())
        }
    }

    suspend fun updateAndroidPlayer(player: Player): Response<String> {
        return try {
            _androidUser = player
            Response.Success(data = "")
        } catch (exception: Exception) {
            Response.Error(message = exception.localizedMessage.toString())
        }
    }

    override suspend fun updateRecentCardState(stateEntity: com.learningis4fun.gameshub.domain.model.CardStateEntity) {
        val state = toCardState(stateEntity)
        _recentCardState = state
    }

    override suspend fun updateUserId(playerId: String): Response<Unit> {
        return try {
            _user = _user.copy(uniqueId = playerId)
            Response.Success(data = Unit)
        } catch (exception: Exception) {
            Response.Error(message = exception.localizedMessage.toString())
        }
    }

    override suspend fun updateUserCardsSortBy(sortBy: String): Response<Unit> {
        return try {
            _userCardsSortBy = when (sortBy) {
                MainViewModel.UserCardsSortBy.CardName.name -> MainViewModel.UserCardsSortBy.CardName
                MainViewModel.UserCardsSortBy.TimePicked.name -> MainViewModel.UserCardsSortBy.TimePicked
                else -> MainViewModel.UserCardsSortBy.CardType
            }

            Response.Success(data = Unit)
        } catch (exception: Exception) {
            Response.Error(message = exception.localizedMessage.toString())
        }
    }

    override suspend fun updateRecentCardType(recentType: String): Response<Unit> {
        return try {
            _recentCardType = recentType
            Response.Success(data = Unit)
        } catch (exception: Exception) {
            Response.Error(message = exception.localizedMessage.toString())
        }
    }

    override suspend fun updateRecentGameId(gameId: String): Response<Unit> {
        return try {
            _recentGameId = gameId
            Response.Success(data = Unit)
        } catch (exception: Exception) {
            Response.Error(message = exception.localizedMessage.toString())
        }
    }

    override suspend fun updateNumberOfCardsPerPlayer(numOfCards: Int): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun updateEliminationCountLimitForCrusade(count: Int): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun decreaseNumberOfCardsPerPlayer(): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun increaseNumberOfCardsPerPlayer(): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun updateNumberOfParticipantsInCrusadeGame(numOfParticipants: Int): Response<Unit> {
        TODO("Not yet implemented")
    }
}