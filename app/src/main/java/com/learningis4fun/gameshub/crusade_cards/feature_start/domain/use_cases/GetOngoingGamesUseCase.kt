package com.learningis4fun.gameshub.crusade_cards.feature_start.domain.use_cases

import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameStageRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.GameState
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.playerName
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.OnGoingGame
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.OngoingGamesOrNewGameState
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util.getDataOrThrowException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

class GetOngoingGamesUseCase @OptIn(ExperimentalCoroutinesApi::class)
@Inject constructor(
    val gameRepository: GameRepository,
    val gameCardRepository: GameCardRepository,
    private val preferences: CrusadeCardsPreferencesManager,
    private val gameStageRepository: GameStageRepository
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<OngoingGamesOrNewGameState.OngoingGamesState> {

        return gameRepository.getCurrentGameId().flatMapLatest {
            gameRepository.getGameWithLatestStagePlayers(it)
        }.flatMapLatest { gameWithPlayers ->

            val showOngoing = try {
                gameWithPlayers.game.gameState != GameState.Completed.name
            } catch (e: Exception) {
                false
            }

            if (!showOngoing) {
                return@flatMapLatest flowOf(OngoingGamesOrNewGameState.OngoingGamesState(isLoading = false))
            }

            val stageNumber = gameStageRepository.getGameStages(gameId = gameWithPlayers.game.id)
                .getDataOrThrowException().first().stageNumber

            gameCardRepository.getThreeTopPlayedCards(
                gameId = gameWithPlayers.game.id,
                stageNumber = stageNumber,
                playerIds = gameWithPlayers.players.map { it.uniqueId }
            ).mapLatest {
                val cards = it.map { userCard ->
                    Card(
                        id = userCard.cardId,
                        name = userCard.name,
                        type = userCard.type,
                        image = userCard.image
                    )
                }

                val loggedInUserId = preferences.user.first().uniqueId

                val players = gameWithPlayers
                    .players
                    .map { player ->
                        player.copy(
                            profileName = player.playerName(
                                loggedInUserId = loggedInUserId
                            )
                        )
                    }

                OngoingGamesOrNewGameState.OngoingGamesState(
                    games = listOf(
                        OnGoingGame(
                            threeCards = cards,
                            gameWithPlayers = gameWithPlayers.copy(
                                players = players
                            ),
                        )
                    ),
                    isLoading = false
                )
            }
        }
    }
}