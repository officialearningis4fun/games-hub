package com.learningis4fun.gameshub.api.use_cases

import android.util.Log
import com.learningis4fun.gameshub.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameStageRepository
import com.learningis4fun.gameshub.domain.model.UserCard
import com.learningis4fun.gameshub.util.GamesHubLogTags
import com.learningis4fun.gameshub.util.getDataOrThrowException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class GetLatestUnPlayedCards @Inject constructor(
    private val gameStageRepository: GameStageRepository,
    private val gameCardRepository: GameCardRepository
) {

    suspend operator fun invoke(
        gameId: String,
        playerIds: List<String>,
        stagesToIgnore: List<Int> = emptyList()
    ): List<UserCard> {

        return try {
            val gameStages = gameStageRepository.getGameStages(gameId).getDataOrThrowException()
            Log.d(GamesHubLogTags.GET_LATEST_UN_PLAYED_CARDS, "stages: $gameStages")

            val unPlayedCards: List<UserCard> = playerIds.mapNotNull { playerId ->

                val latestStageOfPlayer = gameStages
                    .filterNot { it.stageNumber in stagesToIgnore }
                    .firstOrNull { gameStage -> playerId in gameStage.playerIds.map { it.playerId } }
                    ?: return@mapNotNull null

                Log.d(
                    GamesHubLogTags.GET_LATEST_UN_PLAYED_CARDS,
                    "ID: $playerId latestStage$latestStageOfPlayer"
                )

                gameCardRepository.getUnPlayedCardsForPlayer(
                    playerId = playerId,
                    gameId = gameId,
                    stageNumber = latestStageOfPlayer.stageNumber
                ).first()

            }.flatten()

            unPlayedCards
        } catch (e: Exception) {
            emptyList()
        }
    }
}