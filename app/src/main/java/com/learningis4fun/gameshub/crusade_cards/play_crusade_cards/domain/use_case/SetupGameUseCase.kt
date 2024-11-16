package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case

import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util.getDataOrThrowException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class SetupGameUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val createGameUseCase: CreateGameUseCase,
    private val assignCardsUseCase: AssignCardsUseCase,
    private val preferences: CrusadeCardsPreferencesManager,
    private val gameRepository: GameRepository
) {

    suspend operator fun invoke(
        players: List<Player>,
        gameName: String = "",
        createdGameId: String = ""
    ): Response<String> {
        return try {
            val participants = players
                .mapIndexed { index, player -> Pair(player, index + 1) }

            val numOfCardsPerPlayer = preferences.numberOfCardsPerPlayer.first()

            participants.forEach { (player, _) ->
                userRepository.savePlayer(player).getDataOrThrowException()
            }

            val gameId = createGameUseCase(
                gameName = gameName,
                participants = participants.map { (player, position) ->
                    Pair(player, position)
                },
                createdGameId = createdGameId
            )
            // assign cards
            if (gameId.isEmpty()) {
                throw Exception("Game identifier not available")
            }

            val assigned = assignCardsUseCase.assignCards(
                gameId = gameId,
                howManyCardsPerPlayer = numOfCardsPerPlayer
            )

            if (!assigned) {
                throw Exception("Cards not assigned yet")
            }
            gameRepository.updateRecentGameId(gameId = gameId)
            Response.Success(gameId)
        } catch (e: Exception) {
            Response.Error(e.message.toString())
        }
    }

}