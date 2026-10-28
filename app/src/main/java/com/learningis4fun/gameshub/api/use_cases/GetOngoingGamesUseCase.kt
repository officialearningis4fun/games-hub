package com.learningis4fun.gameshub.api.use_cases

import android.util.Log
import com.learningis4fun.gameshub.commons.domain.model.Card
import com.learningis4fun.gameshub.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.commons.domain.util.GameState
import com.learningis4fun.gameshub.commons.domain.util.playerName
import com.learningis4fun.gameshub.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.presentation.start.OnGoingGame
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject
import kotlin.collections.emptyList

class GetOngoingGamesUseCase @OptIn(ExperimentalCoroutinesApi::class)
@Inject constructor(
    val gameRepository: GameRepository,
    val gameCardRepository: GameCardRepository,
    private val preferences: CrusadeCardsPreferencesManager,
    private val getLatestStageNumber: GetLatestStageNumber
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<OnGoingGame>> {
        Log.d("GetOngoingGamesUseCase", "invoke: called")

        return gameRepository.getCurrentGameId()
            .flatMapLatest { gameId ->
                Log.d("GetOngoingGamesUseCase", "id: $gameId")

                gameRepository.getGameWithLatestStagePlayers(gameId)
                    .flatMapLatest { gameWithPlayers ->
                        Log.d(
                            "GetOngoingGamesUseCase",
                            "gameWithPlayers: $gameWithPlayers"
                        )

                        if (
                            gameWithPlayers.game.gameState ==
                            GameState.Completed.name
                        ) {
                            return@flatMapLatest flowOf(emptyList())
                        }

                        flow {
                            val stageNumber = getLatestStageNumber(
                                gameId = gameWithPlayers.game.id
                            )

                            emitAll(
                                gameCardRepository.getThreeTopPlayedCards(
                                    gameId = gameWithPlayers.game.id,
                                    stageNumber = stageNumber,
                                    playerIds = gameWithPlayers.players.map {
                                        it.uniqueId
                                    }
                                ).mapLatest { userCards ->

                                    val cards = userCards.map { userCard ->
                                        Card(
                                            id = userCard.cardId,
                                            name = userCard.name,
                                            type = userCard.type,
                                            image = userCard.image
                                        )
                                    }

                                    val loggedInUserId =
                                        preferences.user.first().uniqueId

                                    val players = gameWithPlayers.players.map {
                                            player ->
                                        player.copy(
                                            profileName = player.playerName(
                                                loggedInUserId = loggedInUserId
                                            )
                                        )
                                    }

                                    listOf(
                                        OnGoingGame(
                                            threeCards = cards,
                                            gameWithPlayers =
                                                gameWithPlayers.copy(
                                                    players = players
                                                )
                                        )
                                    )
                                }
                            )
                        }
                    }
                    .catch { e ->
                        Log.e(
                            "GetOngoingGamesUseCase",
                            "Failed processing game $gameId",
                            e
                        )
                        emit(emptyList())
                    }
            }
            .catch { e ->
                Log.e(
                    "GetOngoingGamesUseCase",
                    "Failed observing current game ID",
                    e
                )
                emit(emptyList())
            }
    }
}