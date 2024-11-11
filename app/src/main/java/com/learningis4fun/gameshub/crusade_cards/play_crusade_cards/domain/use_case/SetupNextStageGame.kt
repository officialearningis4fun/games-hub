package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case

import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.GameStage
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.CardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameStageRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.CardStateEntity
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.GameCard
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
    private val assignCardsUseCase: AssignCardsUseCase,
    private val cardRepository: CardRepository
) {

    suspend operator fun invoke(
        game: Game,
        gameStage: GameStage,
        players: List<Pair<Player, Boolean>>
    ): Response<List<Pair<Player, Boolean>>> {

        return try {
            println("requested to go to next stage")
            gameRepository.updateGame(game).getDataOrThrowException()
            gameStageRepository.saveGameStage(gameStage = gameStage).getDataOrThrowException()
            repository.changeCurrentCardType("").getDataOrThrowException()
            preferenceManager.updateRecentCardState(CardStateEntity(cardStateName = CardStates.Start.name))
            gameCardRepository.deleteAllGameCards().getDataOrThrowException()

            val gameCardId = System.currentTimeMillis().toString()
            val gameCards = cardRepository.getCards().mapIndexed { index, card ->
                GameCard(
                    id = gameCardId + index,
                    gameId = game.id,
                    cardId = card.id,
                )
            }

            gameCards.forEach { gameCard ->
                gameCardRepository.insertGameCard(gameCard).getDataOrThrowException()
            }

            val numOfCardsPerPlayer = preferenceManager.numberOfCardsPerPlayer.first()

            val assigned = assignCardsUseCase.assignCards(
                gameId = game.id,
                howManyCardsPerPlayer = numOfCardsPerPlayer
            )

            if (!assigned) {
                throw Exception("Cards not assigned yet")
            }

            Response.Success(players)
        } catch (e: Exception) {
            e.printStackTrace()
            Response.Error(e.message.toString())
        }
    }

}