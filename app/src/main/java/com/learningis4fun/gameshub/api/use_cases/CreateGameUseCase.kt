package com.learningis4fun.gameshub.api.use_cases

import com.learningis4fun.gameshub.commons.domain.model.Game
import com.learningis4fun.gameshub.commons.domain.model.GameStage
import com.learningis4fun.gameshub.commons.domain.model.GameUserCrossRef
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.model.PlayerIdAndPosition
import com.learningis4fun.gameshub.commons.domain.repository.CardRepository
import com.learningis4fun.gameshub.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameStageRepository
import com.learningis4fun.gameshub.domain.model.GameCard
import com.learningis4fun.gameshub.util.getDataOrThrowException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
class CreateGameUseCase @Inject constructor(
    val repository: DefaultCardRepository,
    private val gameRepository: GameRepository,
    private val cardRepository: CardRepository,
    private val gameCardRepository: GameCardRepository,
    private val gameStageRepository: GameStageRepository
) {

    /**
     * create a new game
     * assign players to game
     * and create gameCards out of available cards
     * the number of gameCards should be the same as that of cards
     * return type is a gameId
     */
    suspend operator fun invoke(
        gameName: String,
        participants: List<Pair<Player, Int>>,
        createdGameId: String = ""
    ): String {

        return try {

            println("requested to create game")

            val gameId = createdGameId.ifEmpty {
                System.currentTimeMillis()
                    .toString()
            }

            // make gamaCardId unique to each game created
            val gameCardId = System.currentTimeMillis().toString()
            val gameCards = cardRepository.getCards().mapIndexed { index, card ->

                GameCard(
                    id = gameCardId + index,
                    gameId = gameId,
                    cardId = card.id,
                )
            }

            val name = gameName.ifEmpty { "game ${(gameRepository.getNumOfCompletedGames() + 1)}" }

            val game = Game(id = gameId, name = name)

            if (participants.isEmpty()) {
                throw Exception("No enough participants for this game")
            }

            if (participants.any { it.first == Player() }) {
                throw Exception("No enough participants for this game")
            }

            if (gameCards.isEmpty()) {
                throw Exception("No cards available yet for the game")
            }

            participants.forEach {
                // associate player with game
                repository.insertGameUserCrossRef(
                    GameUserCrossRef(
                        gameId = gameId,
                        userId = it.first.uniqueId
                    )
                ).getDataOrThrowException()
            }

            val gameStage = GameStage(
                gameId = gameId,
                stageNumber = 1,
                playerIds = participants.map {
                    PlayerIdAndPosition(
                        position = it.second,
                        playerId = it.first.uniqueId
                    )
                }
            )

            gameCards.forEach { gameCard ->
                gameCardRepository.insertGameCard(gameCard.copy(stageNumber = gameStage.stageNumber))
                    .getDataOrThrowException()
            }

            gameRepository.insertGame(game).getDataOrThrowException()
            gameStageRepository.saveGameStage(gameStage = gameStage).getDataOrThrowException()
            gameId

        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}