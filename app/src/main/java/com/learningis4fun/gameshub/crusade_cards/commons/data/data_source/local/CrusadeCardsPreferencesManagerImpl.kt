package com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.learningis4fun.gameshub.MainViewModel
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toCardState
import com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.domain.model.CardStateEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ExperimentalCoroutinesApi
class CrusadeCardsPreferencesManagerImpl @Inject constructor(
    @ApplicationContext val context: Context
) : CrusadeCardsPreferencesManager {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("gamesDataStore")

    override val recentCardType: Flow<String> = context.dataStore.data.map {
        it[PreferencesKeys.RECENT_CARD_TYPE] ?: ""
    }

    override val recentGameId: Flow<String> = context.dataStore.data.map {
        it[PreferencesKeys.RECENT_GAME_ID] ?: ""
    }

    override val recentCardState: Flow<CardStates> = context.dataStore.data.map {
        val stateEntity = CardStateEntity(
            cardStateName = it[PreferencesKeys.CardStatesPreferences.CARD_STATE_NAME]
                ?: CardStates.Start.name,
            playerId = it[PreferencesKeys.CardStatesPreferences.PLAYER_ID] ?: "",
            gameCardId = it[PreferencesKeys.CardStatesPreferences.GAME_CARD_ID] ?: "",
            canPick = it[PreferencesKeys.CardStatesPreferences.CAN_PICK] ?: false,
            canPlay = it[PreferencesKeys.CardStatesPreferences.CAN_PLAY] ?: false,
            cardsToPick = it[PreferencesKeys.CardStatesPreferences.CARDS_TO_PICK] ?: 0,
            cardsPicked = it[PreferencesKeys.CardStatesPreferences.CARDS_PICKED] ?: 0,
            message = it[PreferencesKeys.CardStatesPreferences.MESSAGE] ?: ""
        )
        toCardState(stateEntity)
    }

    override val userCardsSortBy: Flow<MainViewModel.UserCardsSortBy> = context.dataStore.data.map {
        val sortBy =
            it[PreferencesKeys.USER_CARDS_SORT_BY] ?: MainViewModel.UserCardsSortBy.CardType.name

        when (sortBy) {
            MainViewModel.UserCardsSortBy.CardName.name -> MainViewModel.UserCardsSortBy.CardName
            MainViewModel.UserCardsSortBy.TimePicked.name -> MainViewModel.UserCardsSortBy.TimePicked
            else -> MainViewModel.UserCardsSortBy.CardType
        }

    }

    override val userId: Flow<String?> = context.dataStore.data.map {
        it[PreferencesKeys.USER_ID]
    }

    override val user: Flow<Player> = context.dataStore.data.map {
        Player(
            uniqueId = it[PreferencesKeys.Player.UNIQUE_ID] ?: "",
            email = it[PreferencesKeys.Player.EMAIL] ?: "",
            phone = it[PreferencesKeys.Player.PHONE] ?: "",
            imageUrl = it[PreferencesKeys.Player.IMAGE_URL] ?: "",
            profileName = it[PreferencesKeys.Player.PROFILE_NAME] ?: ""
        )
    }

    override val AndroidUser: Flow<Player> = context.dataStore.data.map {
        Player(
            uniqueId = it[PreferencesKeys.AndroidPlayer.UNIQUE_ID]
                ?: "7iooVf8Vu9T8NR2JcwgqLbgr0OC3",
            email = it[PreferencesKeys.AndroidPlayer.EMAIL] ?: "",
            phone = it[PreferencesKeys.AndroidPlayer.PHONE] ?: "",
            imageUrl = it[PreferencesKeys.AndroidPlayer.IMAGE_URL] ?: "",
            profileName = it[PreferencesKeys.AndroidPlayer.PROFILE_NAME] ?: "Computer"
        )
    }

    override suspend fun updatePlayer(player: Player): Response<String> {
        return try {
            context.dataStore.edit {
                it[PreferencesKeys.Player.UNIQUE_ID] = player.uniqueId
                it[PreferencesKeys.Player.EMAIL] = player.email
                it[PreferencesKeys.Player.PHONE] = player.phone
                it[PreferencesKeys.Player.IMAGE_URL] = player.imageUrl
                it[PreferencesKeys.Player.PROFILE_NAME] = player.profileName
            }
            Response.Success(player.profileName, "Successfully saved login info")
        } catch (exception: Exception) {
            Response.Error(
                player.profileName,
                "Failed to save login info: ${exception.localizedMessage}"
            )
        }
    }

    override suspend fun updateAndroidPlayer(player: Player): Response<String> {
        return try {
            context.dataStore.edit {
                it[PreferencesKeys.AndroidPlayer.UNIQUE_ID] = player.uniqueId
                it[PreferencesKeys.AndroidPlayer.EMAIL] = player.email
                it[PreferencesKeys.AndroidPlayer.PHONE] = player.phone
                it[PreferencesKeys.AndroidPlayer.IMAGE_URL] = player.imageUrl
                it[PreferencesKeys.AndroidPlayer.PROFILE_NAME] = player.profileName
            }
            Response.Success(player.profileName, "Successfully saved login info")
        } catch (exception: Exception) {
            Response.Error(
                player.profileName,
                "Failed to save login info: ${exception.localizedMessage}"
            )
        }
    }

    override suspend fun updateRecentCardState(stateEntity: CardStateEntity) {
        context.dataStore.edit {
            it[PreferencesKeys.CardStatesPreferences.CARD_STATE_NAME] = stateEntity.cardStateName
            it[PreferencesKeys.CardStatesPreferences.PLAYER_ID] = stateEntity.playerId
            it[PreferencesKeys.CardStatesPreferences.GAME_CARD_ID] = stateEntity.gameCardId
            it[PreferencesKeys.CardStatesPreferences.CAN_PICK] = stateEntity.canPick
            it[PreferencesKeys.CardStatesPreferences.CAN_PLAY] = stateEntity.canPlay
            it[PreferencesKeys.CardStatesPreferences.CARDS_TO_PICK] = stateEntity.cardsToPick
            it[PreferencesKeys.CardStatesPreferences.CARDS_PICKED] = stateEntity.cardsPicked
            it[PreferencesKeys.CardStatesPreferences.MESSAGE] = stateEntity.message
        }
    }

    override suspend fun updateUserId(playerId: String): Response<String> {
        return try {
            context.dataStore.edit {
                it[PreferencesKeys.USER_ID] = playerId
            }
            Response.Success(playerId, "Successfully updated user info")
        } catch (exception: Exception) {
            Response.Error(
                playerId,
                "Failed to update user info with this error: ${exception.localizedMessage}"
            )
        }
    }

    override suspend fun updateUserCardsSortBy(sortBy: String): Response<String> {
        return try {
            context.dataStore.edit {
                it[PreferencesKeys.USER_CARDS_SORT_BY] = sortBy
            }
            Response.Success(sortBy, "Successfully updated")
        } catch (exception: Exception) {
            Response.Error(
                data = sortBy,
                message = "Failed to change sort cards by with this error: ${exception.localizedMessage}"
            )
        }
    }

    override suspend fun updateRecentCardType(recentType: String): Response<String> {
        return try {
            context.dataStore.edit {
                it[PreferencesKeys.RECENT_CARD_TYPE] = recentType
            }
            Response.Success(recentType, "Successfully changed type")
        } catch (exception: Exception) {
            Response.Error(
                recentType,
                "Failed to change type with this error: ${exception.localizedMessage}"
            )
        }
    }

    override suspend fun updateRecentGameId(gameId: String): Response<String> {
        return try {
            context.dataStore.edit {
                it[PreferencesKeys.RECENT_GAME_ID] = gameId
            }
            Response.Success(gameId, "Successfully changed type")
        } catch (exception: Exception) {
            Response.Error(
                gameId,
                "Failed to change type with this error: ${exception.localizedMessage}"
            )
        }
    }

    object PreferencesKeys {
        val RECENT_GAME_ID = stringPreferencesKey("curr-game-id")
        val RECENT_CARD_TYPE = stringPreferencesKey("recent-card-type")

        val USER_CARDS_SORT_BY = stringPreferencesKey("user-cards-sort-by")

        val USER_ID = stringPreferencesKey("user-id")

        object CardStatesPreferences {
            val CARD_STATE_NAME = stringPreferencesKey("card-state-name")
            val PLAYER_ID = stringPreferencesKey("card-state-player-id")
            val GAME_CARD_ID = stringPreferencesKey("card-state-game-card-id")
            val CAN_PICK = booleanPreferencesKey("card-state-can-pick")
            val CAN_PLAY = booleanPreferencesKey("card-state-can-play")
            val CARDS_PICKED = intPreferencesKey("card-state-cards-picked")
            val CARDS_TO_PICK = intPreferencesKey("card-state-cards-to-pick")
            val MESSAGE = stringPreferencesKey("card-state-message")
        }

        object Player {
            val UNIQUE_ID = stringPreferencesKey("unique-id")
            val EMAIL = stringPreferencesKey("email")
            val PHONE = stringPreferencesKey("phone")
            val IMAGE_URL = stringPreferencesKey("image-url")
            val PROFILE_NAME = stringPreferencesKey("profile-name")
        }

        object AndroidPlayer {
            val UNIQUE_ID = stringPreferencesKey("android-unique-id")
            val EMAIL = stringPreferencesKey("android-email")
            val PHONE = stringPreferencesKey("android-phone")
            val IMAGE_URL = stringPreferencesKey("android-image-url")
            val PROFILE_NAME = stringPreferencesKey("android-profile-name")
        }
    }

}