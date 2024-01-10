package com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.use_case

import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.CardRepository
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.GameUserCrossRef
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.GameCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
class CreateGameUseCase @Inject constructor(
    val repository: DefaultCardRepository,
    private val gameRepository: GameRepository,
    private val userRepository: UserRepository,
    private val cardRepository: CardRepository,
    private val gameCardRepository: GameCardRepository
){

    /**
     * create a new game
     * assign players to game
     * and create gameCards out of available cards
     * the number of gameCards should be the same as that of cards
     * return type is a gameId
     */
    suspend fun createGame(gameName: String, participants : List<Player>): String {

        return try {

            val gameId = System.currentTimeMillis().toString()
            // make gamaCardId unique to each game created
            val gameCardId = System.currentTimeMillis().toString()
            val gameCards = cardRepository.getCards().mapIndexed { index, card ->

                GameCard(
                    id = gameCardId + index,
                    gameId = gameId,
                    cardId = card.id,
                )
            }
            val game = Game(id = gameId, name = gameName)

            if(participants.isEmpty()){
                throw Exception("No enough participants for this game")
            }

            if(participants.any { it == Player() }){
                throw Exception("No enough participants for this game")
            }

            if(gameCards.isEmpty()){
                throw Exception("No cards available yet for the game")
            }

            participants.forEach {
                // associate player with game
                repository.insertGameUserCrossRef(
                    GameUserCrossRef(
                        gameId = gameId,
                        userId = it.uniqueId
                    )
                )
            }
            gameCards.forEach { gameCard ->
                gameCardRepository.insertGameCard(gameCard)
            }
            gameRepository.insertGame(game)
            gameId

        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}