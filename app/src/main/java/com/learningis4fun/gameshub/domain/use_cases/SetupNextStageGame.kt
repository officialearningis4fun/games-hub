package com.learningis4fun.gameshub.domain.use_cases

import com.learningis4fun.gameshub.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.commons.domain.model.Game
import com.learningis4fun.gameshub.commons.domain.model.GameStage
import com.learningis4fun.gameshub.commons.domain.repository.CardRepository
import com.learningis4fun.gameshub.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameStageRepository
import com.learningis4fun.gameshub.commons.domain.util.CardStates
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.domain.model.GameCard
import com.learningis4fun.gameshub.util.getDataOrThrowException
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
    private val assignCardsUseCase: AssignCardsUseCase,
    private val cardRepository: CardRepository
) {

    suspend operator fun invoke(
        game: Game,
        nextStage: GameStage,
        currentStageNumber: Int
    ): Response<Unit> {

        return try {
            println("requested to go to next stage")
            gameRepository.updateGame(game).getDataOrThrowException()
            gameStageRepository.saveGameStage(gameStage = nextStage).getDataOrThrowException()
            repository.changeCurrentCardType("").getDataOrThrowException()
            preferenceManager.updateRecentCardState(
                com.learningis4fun.gameshub.domain.model.CardStateEntity(
                    cardStateName = CardStates.Start.name
                )
            )
            gameCardRepository.deleteAllGameCardsWithoutPlayer(
                gameId = game.id,
                stageNumber = currentStageNumber
            ).getDataOrThrowException()

            val gameCardId = System.currentTimeMillis().toString()
            val gameCards = cardRepository.getCards().mapIndexed { index, card ->
                GameCard(
                    id = gameCardId + index,
                    gameId = game.id,
                    cardId = card.id,
                    stageNumber = nextStage.stageNumber
                )
            }

            gameCards.forEach { gameCard ->
                gameCardRepository.insertGameCard(gameCard).getDataOrThrowException()
            }

            // assign cards

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