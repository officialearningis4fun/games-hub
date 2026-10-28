@file:OptIn(ExperimentalCoroutinesApi::class)

package com.learningis4fun.gameshub.data.data_source.remote

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.learningis4fun.gameshub.BuildConfig
import com.learningis4fun.gameshub.api.use_cases.GetLatestStageNumber
import com.learningis4fun.gameshub.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.commons.domain.util.CardStates
import com.learningis4fun.gameshub.commons.domain.util.CardStatesDefaultMsg
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.util.getDataOrThrowException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

const val RULES =
    "The game consists of 50 cards with four suits plus two jokers (Red and Black Joker).\n" +
            "\n" +
            "Other cards suits are labelled: diamond, club, heart, and spade\n" +
            "Jokers are red and black\n" +
            "\n" +
            "Cards are labelled:\n" +
            "A-diamond,A-club,A-heart,A-spade,\n" +
            "2-diamond,2-club,2-heart,2-spade,\n" +
            "3-diamond,3-club,3-heart,3-spade,\n" +
            "4-club,4-heart,4-spade,4-diamond,\n" +
            "5-club,5-heart,5-spade,5-diamond,\n" +
            "6-club,6-heart,6-spade,6-diamond,\n" +
            "7-club,7-heart,7-spade,7-diamond,\n" +
            "8-club,8-heart,8-spade,8-diamond,\n" +
            "9-club,9-heart,9-spade,9-diamond,\n" +
            "10-club,10-heart,10-spade,10-diamond,\n" +
            "Jack-club,Jack-heart,Jack-spade,Jack-diamond,\n" +
            "Queen-club,Queen-heart,Queen-spade,Queen-diamond,\n" +
            "King-club,King-heart,King-spadeKing-diamond,\n" +
            "Joker-Red,Joker-Black,\n" +
            "\n" +
            "How to play:\n" +
            "Game rules\n" +
            "One type of suit can sit atop of the other card or the same card name; e.g. If A-spade is atop,a card with A or spade can be played.\n" +
            "8 can be played atop of any card except if the user is hit and is used to change the suit.\n" +
            "Joker can be played atop of any card and can be used to change the suit or hit.\n" +
            "In a beginning of a game, any card can be played\n" +
            "Joker or 7 must be followed by another card\n" +
            "If a user is hit by 2, they should pick 2 cards from the dustbin\n" +
            "if a user is hit by joker, they should pik 4 cards\n" +
            "If the first user is hit by 2, and then they hit back by joker, the other user will have to pick (2 + 4) cards from the dustbin or hit back.\n" +
            "\n" +
            "\n" +
            "Definition of key words:\n" +
            "\"Dustbin\" - cards that can be picked,\n" +
            "\"Pick\" - take a card from the dustbin\n" +
            "\"Requested to play or pick\" - Should play any card based on the game rules or pick a card from the dustbin\n" +
            "\"Hit\" - If hit, should hit back (if they want to) with a 2 or Joker, or request to pick the specified number of cards from the dustbin.\n" +
            "\n" +
            "\n" +
            "aim of the game: To play all the provided cards and finish before everyone else."

class GeminiCardController @OptIn(ExperimentalCoroutinesApi::class)
@Inject constructor(
    private val gameCardRepository: GameCardRepository,
    private val latestStageNumber: GetLatestStageNumber,
    private val gameRepository: GameRepository
) {

    val model = GenerativeModel(
        modelName = "gemini-2.0-flash-exp",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 1f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 8192
            responseMimeType = "text/plain"
        }
    )

    suspend fun invoke(
        state: CardStates,
        playerId: String,
        gameId: String
    ): Response<String> {
        return try {

            var stateMessage: String? = null

            val gameWithPlayers = gameRepository.getGameWithPlayers(gameId).first() ?: return Response.Error("Game not found")

            val otherPlayers = gameWithPlayers
                .players
                .filterNot { it.uniqueId == playerId }

            // You in a game with
            val otherPlayersNames = otherPlayers
                .map { it.profileName }

            val participantsMsg = "You in a game with $otherPlayersNames"

            val otherPlayersLeftCardsMsg = buildString {
                otherPlayers
                    .forEach {
                        val numOfUnPlayedCards = gameCardRepository.numOfUnPlayedCardsForPlayer(
                            gameId = gameId,
                            playerId = it.uniqueId,
                            stageNumber = latestStageNumber(gameId)
                        )
                        append("${it.profileName} has $numOfUnPlayedCards cards")

                    }
            }

            val unPlayedCards = gameCardRepository.getUnPlayedCardsForPlayer(
                gameId = gameId,
                playerId = playerId,
                latestStageNumber(gameId)
            ).first()
                .map {
                    it.card
                }

            val unPlayedCardsMsg = "Your cards are: $unPlayedCards"

            val topPlayedCard = gameCardRepository.getTopPlayedCard(
                gameId = gameId,
                gameWithPlayers.players.map { it.uniqueId },
                latestStageNumber(gameId = gameId)
            ).first()
                ?.toUserCard()
                ?.card

            val topCardMsg = topPlayedCard?.let { "\"Top card: $it" }

            when (state) {
                is CardStates.Chance -> {
                    if (state.playerId == playerId) {
                        stateMessage = buildString {
                            appendLine(state.message)
                            appendLine("Only reply with a card or response of \"${CardStatesDefaultMsg.NoAvailableCard}\". Do not explain anything")
                        }
                    }
                }

                is CardStates.ChangeType -> {
                    if (state.playerId == playerId) {
                        stateMessage = buildString {
                            appendLine(state.message)
                            appendLine("Only reply with a card type you need to change to. Do not explain anything")
                        }
                    }
                }

                is CardStates.ChangedType -> {}
                is CardStates.DefineCardAction -> {
                    if (state.playerId == playerId) {
                        stateMessage = buildString {
                            appendLine(state.message)
                            appendLine("Only reply with Hit or ChangeType to hit the next player or change the type of cards respectively. Do not explain anything")
                        }
                    }
                }

                is CardStates.Pick,
                is CardStates.Error,
                is CardStates.NoAvailableCard,
                is CardStates.Played,
                is CardStates.Picked -> {
                }

                is CardStates.Hit -> {
                    if (state.playerId == playerId) {
                        stateMessage = buildString {
                            appendLine(state.message)
                            appendLine("Which card would you like to play? Only reply with a card or response of \"pick\". Do not explain anything")
                        }
                    }
                }

                is CardStates.Play -> {
                    if (state.playerId == playerId) {
                        stateMessage = buildString {
                            appendLine(state.message)
                            appendLine("Which card would you like to play? Only reply with a card or response of \"pick\". Do not explain anything")
                        }
                    }
                }

                CardStates.Start -> {}
            }

            val requestPlayMessage = if (stateMessage != null) {
                buildString {
                    appendLine(RULES)
                    appendLine(participantsMsg)
                    appendLine(otherPlayersLeftCardsMsg)
                    appendLine(unPlayedCardsMsg)
                    if (topCardMsg.isNullOrBlank().not()) {
                        appendLine(topCardMsg)
                    }
                    appendLine(stateMessage)
                }
            } else throw Exception("No input text configured: state: $state")

            val response = model.generateContent(requestPlayMessage)

            Response.Success(response.text ?: throw Exception("Failed to generate text"))
        } catch (e: Exception) {
            Response.Error(e.message.toString())
        }
    }

}