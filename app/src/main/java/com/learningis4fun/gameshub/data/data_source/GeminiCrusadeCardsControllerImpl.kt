@file:OptIn(ExperimentalCoroutinesApi::class)

package com.learningis4fun.gameshub.data.data_source

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.learningis4fun.gameshub.BuildConfig
import com.learningis4fun.gameshub.commons.domain.controller.CrusadeComputerCardsController
import com.learningis4fun.gameshub.commons.domain.repository.CardRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameStageRepository
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.data.data_source.remote.RULES
import com.learningis4fun.gameshub.domain.model.UserCard
import com.learningis4fun.gameshub.domain.use_cases.GetLatestStageNumber
import com.learningis4fun.gameshub.util.GamesHubLogTags
import com.learningis4fun.gameshub.util.getDataOrThrowException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GeminiCrusadeCardsControllerImpl @Inject constructor(
    private val gameRepository: GameRepository,
    private val cardRepository: CardRepository,
    private val gameCardRepository: GameCardRepository,
    private val gameStageRepository: GameStageRepository,
    private val getLatestStageNumber: GetLatestStageNumber
) : CrusadeComputerCardsController {

    val analytics = Firebase.analytics

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

    private suspend fun modelTextResponse(requestGeminiPlayCardMsg: String): String? {
        return try {
            Log.d(GamesHubLogTags.GEMINI_CARDS_CONTROLLER, "generating text")
            model.generateContent(requestGeminiPlayCardMsg).text?.trim()
        } catch (e: Exception) {
            analytics.logEvent("send_gemini_prompt") {
                param("error", e.message.toString())
            }
            Log.d(GamesHubLogTags.GEMINI_CARDS_CONTROLLER, e.message.toString())
            null
        }
    }

    override suspend fun getCurrentGameId() = gameRepository.getCurrentGameId().first()

    override suspend fun getCurrentCardType() = cardRepository.getCurrentCardType()

    override suspend fun getCurrentCardName() = cardRepository.getCurrCardName()

    override suspend fun getCurrentStageNumber(gameId: String): Int =
        gameStageRepository.getGameStages(gameId).getDataOrThrowException().first().stageNumber

    private suspend fun playCardWithNoRestrictions(
        gameId: String,
        playerId: String
    ): Response<UserCard?> {

        return try {
            if (gameId.isEmpty()) {
                throw Exception("Game is not ready, create a game")
            }

            if (playerId.isEmpty()) {
                throw Exception("Player is not ready, make sure the player is created")
            }

            // You in a game with :
            // *** has 5 cards left
            // Your cards are:
            // current card type is: current card name is:
            // play message
            val requestCardMsg =
                "Which card would you like to play? Only reply with a card or response of \"pick\". Do not explain anything"

            val requestGeminiPlayCardMsg = buildString {
                appendLine(RULES)
                appendLine(otherPlayersMsg(gameId))
                appendLine(leftCardsForOtherPlayersMsg(gameId))
                appendLine(unPlayedCardsMsg(gameId, playerId))
                appendLine("Can play any card")
                appendLine(requestCardMsg)
            }

            val card = modelTextResponse(requestGeminiPlayCardMsg)

            val cards =
                gameCardRepository.getUnPlayedCardsForPlayer(
                    gameId = gameId,
                    playerId = playerId,
                    getCurrentStageNumber(gameId)
                )
                    .first()
                    .filterNot {
                        it.name == Constants.CardNames.EIGHT
                    }

            Log.d("Gemini", "cards: ${cards.map { it.card }}")
            Log.d("Gemini", "card with no restrictions: $card")

            if (card == null) {
                throw Exception("User card not found: unable to make a decision")
            }
            val userCard = cards.find { it.card == card } ?: throw Exception("User card not found")

            Response.Success(data = userCard)
        } catch (exception: Exception) {
            Response.Error(message = exception.message.toString())
        }
    }

    private suspend fun otherPlayersMsg(
        gameId: String
    ): String {
        // You in a game with
        val otherPlayersNames = gameRepository
            .getGameWithPlayers(gameId = gameId)
            .first()
            .players
            .map { it.profileName }

        return "You in a game with $otherPlayersNames"
    }

    private suspend fun leftCardsForOtherPlayersMsg(
        gameId: String
    ): String {

        return buildString {
            gameRepository
                .getGameWithPlayers(gameId)
                .first()
                .players
                .forEach { player ->
                    val countOfCardsLeft = gameCardRepository.numOfUnPlayedCardsForPlayer(
                        gameId = gameId,
                        playerId = player.uniqueId,
                        stageNumber = getLatestStageNumber(gameId)
                    )
                    appendLine("${player.profileName} has $countOfCardsLeft cards")
                }
        }
    }

    private suspend fun unPlayedCardsMsg(
        gameId: String,
        playerId: String
    ): String {
        val unPlayedCards = gameCardRepository.getUnPlayedCardsForPlayer(
            gameId = gameId,
            playerId = playerId,
            getLatestStageNumber(gameId)
        ).first()
            .map {
                it.card
            }

        return "Your cards are: $unPlayedCards"
    }

    override suspend fun cardForComputerToHit(
        playerId: String,
        gameId: String,
        cardsToPick: Int?
    ): UserCard? {
        // You in a game with :
        // *** has 5 cards left
        // Your cards are:
        // current card type is: current card name is:
        // play message
        val requestCardMsg =
            "You have a chance to pick $cardsToPick cards or hit back. Which card would you like to play? Only reply with a card or response of \"pick\". Do not explain anything"

        val requestGeminiPlayCardMsg = buildString {
            appendLine(RULES)
            appendLine(otherPlayersMsg(gameId))
            appendLine(leftCardsForOtherPlayersMsg(gameId))
            appendLine(unPlayedCardsMsg(gameId, playerId))
            appendLine("The card you play must have a name: ${Constants.CardNames.TWO} or ${Constants.CardNames.JOKER}")
            appendLine(requestCardMsg)
        }

        val card = modelTextResponse(requestGeminiPlayCardMsg)

        Log.d("Gemini", "card to hit: $card")

        val cards =
            gameCardRepository.getUnPlayedCardsForPlayer(
                gameId = gameId,
                playerId = playerId,
                getCurrentStageNumber(gameId)
            )
                .first()

        val chosenCard = cards.find { it.card == card }

        Log.d("Gemini", "cards: ${cards.map { it.card }}")
        Log.d("Gemini", "chosen card: $chosenCard")

        return chosenCard
    }

    override suspend fun newCardType(
        playerId: String,
        gameId: String
    ): String {
        // You in a game with :
        // *** has 5 cards left
        // Your cards are:
        // current card type is: current card name is:
        // play message
        val requestCardMsg =
            "You must choose a type cards should ba changed to. Only reply with a card type. Do not explain anything"

        val requestGeminiPlayCardMsg = buildString {
            appendLine(RULES)
            appendLine(otherPlayersMsg(gameId))
            appendLine(leftCardsForOtherPlayersMsg(gameId))
            appendLine(unPlayedCardsMsg(gameId, playerId))
            appendLine("The card type must be one of the following: ${Constants.CardTypes.CLUBS}, ${Constants.CardTypes.DIAMOND}, ${Constants.CardTypes.HEART}, ${Constants.CardTypes.SPADE}")
            appendLine(requestCardMsg)
        }

        val cardType = modelTextResponse(requestGeminiPlayCardMsg)

        val cardTypes = listOf(
            Constants.CardTypes.CLUBS,
            Constants.CardTypes.DIAMOND,
            Constants.CardTypes.HEART,
            Constants.CardTypes.SPADE
        )

        val chosenCardType = cardTypes.find { it == cardType }

        Log.d("Gemini", "card type chosen: $cardType")

        if (chosenCardType == null) {
            Log.d("Gemini", "Failed to decide on a type")
        }
        return chosenCardType ?: cardTypes.random()
    }

    override fun cardTypeButJoker(cardsTypes: List<String>): String {
        val defaultCardTypes = listOf(
            Constants.CardTypes.CLUBS,
            Constants.CardTypes.SPADE,
            Constants.CardTypes.DIAMOND,
            Constants.CardTypes.HEART,
        )

        return cardsTypes.randomOrNull() ?: defaultCardTypes.random()
    }

    override suspend fun jokerCardAction(gameId: String, playerId: String): String {
        // You in a game with :
        // *** has 5 cards left
        // Your cards are:
        // current card type is: current card name is:
        // play message
        val requestCardMsg =
            "You must choose whether you want to hit or change the type of cards. Do not explain anything"

        val requestGeminiPlayCardMsg = buildString {
            appendLine(RULES)
            appendLine(otherPlayersMsg(gameId))
            appendLine(leftCardsForOtherPlayersMsg(gameId))
            appendLine(unPlayedCardsMsg(gameId, playerId))
            appendLine("The option is one of the following: ${Constants.CardJokerActions.hit}, ${Constants.CardJokerActions.changeType}, ${Constants.CardTypes.HEART}, ${Constants.CardTypes.SPADE}")
            appendLine(requestCardMsg)
        }

        val action = modelTextResponse(requestGeminiPlayCardMsg)

        val actions = listOf(
            Constants.CardJokerActions.hit,
            Constants.CardJokerActions.changeType
        )

        val chosenCardType = actions.find { it == action }

        Log.d("Gemini", "card type chosen: $action")

        if (chosenCardType == null) {
            Log.d("Gemini", "Failed to decide on a type")
        }
        return chosenCardType ?: actions.random()
    }

    override suspend fun playCardForComputer(
        playerId: String,
        gameId: String
    ): UserCard? {

        val currentCardType = getCurrentCardType()
        val currentCardName = getCurrentCardName()

        return if (currentCardType.isEmpty()) {
            playCardWithNoRestrictions(gameId, playerId).data
        } else {

            if (currentCardName.isEmpty()) {
                playCardWithCardTypeRestrictions(gameId, playerId, currentCardType)
            } else {
                playCardWithCardTypeAndNameRestrictions(
                    gameId,
                    playerId,
                    currentCardType,
                    currentCardName
                )
            }

        }

    }

    private suspend fun playCardWithCardTypeAndNameRestrictions(
        gameId: String,
        playerId: String,
        currentCardType: String,
        currentCardName: String
    ): UserCard? {
        // You in a game with :
        // *** has 5 cards left
        // Your cards are:
        // current card type is: current card name is:
        // play message
        val requestCardMsg =
            "Which card would you like to play? Only reply with a card or response of \"pick\". Do not explain anything"

        val requestGeminiPlayCardMsg = buildString {
            appendLine(RULES)
            appendLine(otherPlayersMsg(gameId))
            appendLine(leftCardsForOtherPlayersMsg(gameId))
            appendLine(unPlayedCardsMsg(gameId, playerId))
            appendLine("The card you play must have a type: $currentCardType OR have a name: $currentCardName")
            appendLine(requestCardMsg)
        }

        val card = modelTextResponse(requestGeminiPlayCardMsg)
        Log.d("Gemini", "card with TypeAndNameRestrictions: $card")
        val cards =
            gameCardRepository.getUnPlayedCardsForPlayer(
                gameId = gameId,
                playerId = playerId,
                getCurrentStageNumber(gameId)
            )
                .first()
        val chosenCard = cards.find { it.card == card?.trim() }
        val takenCards = cards.filter { it.card == card }

        Log.d("Gemini", "taken cards: ${takenCards.map { it.card }}")
        Log.d("Gemini", "cards: ${cards.map { it.card }}")
        Log.d("Gemini", "chosen card: $chosenCard")

        return chosenCard

    }

    private suspend fun playCardWithCardTypeRestrictions(
        gameId: String,
        playerId: String,
        currentCardType: String
    ): UserCard? {

        // You in a game with :
        // *** has 5 cards left
        // Your cards are:
        // current card type is: current card name is:
        // play message
        val requestCardMsg =
            "Which card would you like to play? Only reply with a card or response of \"pick\". Do not explain anything"

        val requestGeminiPlayCardMsg = buildString {
            appendLine(RULES)
            appendLine(otherPlayersMsg(gameId))
            appendLine(leftCardsForOtherPlayersMsg(gameId))
            appendLine(unPlayedCardsMsg(gameId, playerId))
            appendLine("The card you play must have a type: $currentCardType")
            appendLine(requestCardMsg)
        }

        val card = model.generateContent(requestGeminiPlayCardMsg).text
        Log.d("Gemini", "card with TypeRestrictions: $card")

        val cards =
            gameCardRepository.getUnPlayedCardsForPlayer(
                gameId = gameId,
                playerId = playerId,
                getCurrentStageNumber(gameId)
            )
                .first()
        val chosenCard = cards.find { it.card == card }

        Log.d("Gemini", "cards: ${cards.map { it.card }}")
        Log.d("Gemini", "chosen card: $chosenCard")

        return chosenCard
    }

}