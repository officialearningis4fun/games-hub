package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case

import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.GameStage
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameStageRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.CardStateEntity
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util.getDataOrThrowException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@ExperimentalCoroutinesApi
class SetupNextStageGame
@Inject
constructor(
    val repository: DefaultCardRepository,
    private val gameRepository: GameRepository,
    private val gameCardRepository: GameCardRepository,
    private val gameStageRepository: GameStageRepository,
    private val preferenceManager: CrusadeCardsPreferencesManager,
    private val assignCardsUseCase: AssignCardsUseCase

) {

    /**
     * create a new game
     * assign players to game
     * and create gameCards out of available cards
     * the number of gameCards should be the same as that of cards
     * return type is a gameId
     */
    suspend operator fun invoke(
        game: Game,
        gameStage: GameStage
    ): Response<Unit> {

        return try {

            gameCardRepository.getGameCards(gameId = game.id).forEach { gameCard ->
                gameCardRepository.updateGameCard(
                    gameCard.copy(
                        isPlayed = false,
                        playerId = "",
                        playTime = 0L,
                        pickedTime = 0L
                    )
                ).getDataOrThrowException()
            }

            gameStageRepository.saveGameStage(gameStage = gameStage).getDataOrThrowException()
            gameRepository.updateGame(game).getDataOrThrowException()
            repository.changeCurrentCardType("").getDataOrThrowException()
            preferenceManager.updateRecentCardState(CardStateEntity(cardStateName = CardStates.Start.name))

            val numOfCardsPerPlayer = preferenceManager.numberOfCardsPerPlayer.first()

            val assigned = assignCardsUseCase.assignCards(
                gameId = game.id,
                howManyCardsPerPlayer = numOfCardsPerPlayer
            )

            if (!assigned) {
                throw Exception("Cards not assigned yet")
            }

            Response.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Response.Error(e.message.toString())
        }
    }

}