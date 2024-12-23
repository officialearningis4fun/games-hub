package com.learningis4fun.gameshub.crusade_cards.common.domain.repository

import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.GameUserCrossRef
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.data.data_source.relations.GameCardWithCard
import com.learningis4fun.gameshub.data.data_source.relations.GamesOfPlayer
import com.learningis4fun.gameshub.crusade_cards.domain.model.GameCard
import com.learningis4fun.gameshub.crusade_cards.domain.model.UserCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

@ExperimentalCoroutinesApi
class FakeDefaultRepositoryImpl(
) : DefaultCardRepository {

    val _gameUserCrossRefs: MutableList<GameUserCrossRef> = ArrayList()
    var _curreneCardType = ""

    override suspend fun insertGameUserCrossRef(crossRef: GameUserCrossRef): Response<String> {
        return try {
            _gameUserCrossRefs.add(crossRef)
            Response.Success("", message = "Successfully added")
        } catch (exception: Exception) {
            Response.Error(
                "",
                exception.localizedMessage
                    ?: "Unknown error occurred while adding game and player id"
            )
        }
    }

    override suspend fun updateGameUserCrossRef(crossRef: GameUserCrossRef): Response<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteGameUserCrossRef(crossRef: GameUserCrossRef): Response<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getRecentTopCard(): UserCard {
        TODO("Not yet implemented")
    }

    override fun getGamesOfPlayer(playerId: String): Flow<GamesOfPlayer> {
        TODO("Not yet implemented")
    }

    override fun getCardsInDustbin(gameId: String): Flow<List<GameCardWithCard>> {
        TODO("Not yet implemented")
    }

    override suspend fun pickRandomCardFromDB(gameId: String): GameCard {
        TODO("Not yet implemented")
    }

    override suspend fun getGameCard(cardName: String): UserCard? {
        TODO("Not yet implemented")
    }

    override suspend fun changeCurrentCardType(type: String): Response<String> {
        return try {
            _curreneCardType = type
            Response.Success("", type)
        } catch (exception: Exception) {
            Response.Error(
                "",
                exception.localizedMessage ?: "unknown error occurred while changimg card type"
            )
        }
    }

    override suspend fun pickCardsFromLibrary(
        playerId: String,
        gameId: String,
        numOfCardsToPick: Int
    ): Response<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun requestCardsFromSceneToBePicked(
        gameId: String,
        numOfCardsToPick: Int,
        numOfCardsAlreadyAvailableToPick: Int
    ): Response<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun numOfCardsAvailableToPick(gameId: String): Int {
        TODO("Not yet implemented")
    }

    override suspend fun cardToBePlayedByComputer(gameId: String, playerId: String): UserCard? {
        TODO("Not yet implemented")
    }

    override suspend fun jokerCardActionByComputer(): String {
        val jokerActions = listOf(
            Constants.CardJokerActions.changeType,
            Constants.CardJokerActions.hit,
        )
        return jokerActions.random()
    }

    override suspend fun cardTypeForComputerToChangeTo(): String {
        TODO("Not yet implemented")
    }

    override suspend fun cardForComputerToHitBack(gameId: String, playerId: String): UserCard? {
        TODO("Not yet implemented")
    }

    override suspend fun availableCardsForComputer(
        gameId: String,
        playerId: String
    ): List<UserCard> {
        TODO("Not yet implemented")
    }
}