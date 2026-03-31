@file:OptIn(ExperimentalCoroutinesApi::class)

package com.learningis4fun.gameshub.data.data_source

import android.util.Log
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.Model
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
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
import com.learningis4fun.gameshub.domain.model.UserCard
import com.learningis4fun.gameshub.domain.use_cases.GetLatestStageNumber
import com.learningis4fun.gameshub.util.GamesHubLogTags
import com.learningis4fun.gameshub.util.getDataOrThrowException
import com.learningis4fun.gameshub.util.getNullableDataOrThrowException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi

class OpenAICrusadeCardsControllerImpl @Inject constructor(
    private val gameRepository: GameRepository,
    private val cardRepository: CardRepository,
    private val gameCardRepository: GameCardRepository,
    private val gameStageRepository: GameStageRepository,
    private val getLatestStageNumber: GetLatestStageNumber,
    private val preferencesManager: CrusadeCardsPreferencesManager
) : CrusadeComputerCardsController {

    private val analytics = Firebase.analytics

    private val openAIConfig = OpenAIConfig(
        token = BuildConfig.OPEN_API_KEY,
        timeout = Timeout(30.seconds)
    )

    private val openAI = OpenAI(config = openAIConfig)

    private suspend fun modelTextResponse(requestGeminiPlayCardMsg: String): String? {

        return try {
            // signIn anonymously: Firebase auth
            // think of the retry strategy

            val sessionID = preferencesManager.getGameSessionId()

            val model: Model =
                openAI.model(ModelId("ft:gpt-4o-mini-2024-07-18:learningis4fun:crusade-cards:B1Jzy39f"))

            Log.d(GamesHubLogTags.OPENAI_CARDS_CONTROLLER, requestGeminiPlayCardMsg)

            val chatCompletionRequest = ChatCompletionRequest(
                model = model.id,
                temperature = 1.0,
                maxTokens = 2048,
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.User,
                        content = requestGeminiPlayCardMsg
                    )
                ),
                user = sessionID
            )

            val completion: ChatCompletion = openAI.chatCompletion(chatCompletionRequest)

            val text = completion.choices[0].message.content

            Log.d(GamesHubLogTags.OPENAI_CARDS_CONTROLLER, "generated text: $text")
            analytics.logEvent("send_openai_prompt") {
                param("generated text", text.toString())
            }
            text?.trim()
        } catch (e: Exception) {

            if (e is CancellationException) {
                throw e
            }

            analytics.logEvent("send_openai_prompt") {
                param("error", e.message.toString())
            }
            Log.d(GamesHubLogTags.OPENAI_CARDS_CONTROLLER, e.message.toString())
            e.printStackTrace()
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
                appendLine(otherPlayersMsg(gameId = gameId, playerId = playerId))
                appendLine(leftCardsForOtherPlayersMsg(gameId = gameId, playerId = playerId))
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

            Log.d(GamesHubLogTags.OPENAI_CARDS_CONTROLLER, "cards: ${cards.map { it.card }}")
            Log.d(GamesHubLogTags.OPENAI_CARDS_CONTROLLER, "card with no restrictions: $card")

            if (card == null) {
                throw Exception("User card not found: unable to make a decision")
            }
            val userCard = cards.find { it.card == card } ?: throw Exception("User card not found")

            Response.Success(data = userCard)
        } catch (exception: Exception) {
            Log.e(GamesHubLogTags.OPENAI_CARDS_CONTROLLER, exception.toString())
            Response.Error(message = exception.message.toString())
        }
    }

    private suspend fun otherPlayersMsg(
        gameId: String,
        playerId: String
    ): String {
        // You in a game with
        val otherPlayersNames = gameRepository
            .getGameWithPlayers(gameId = gameId)
            .first()
            .players
            .filterNot { it.uniqueId == playerId }
            .map { it.uniqueId }

        return "You in a game with $otherPlayersNames"
    }

    private suspend fun leftCardsForOtherPlayersMsg(
        gameId: String,
        playerId: String
    ): String {

        return buildString {
            gameRepository
                .getGameWithPlayers(gameId)
                .first()
                .players
                .filterNot { it.uniqueId == playerId }
                .forEach { player ->
                    val countOfCardsLeft = gameCardRepository.numOfUnPlayedCardsForPlayer(
                        gameId = gameId,
                        playerId = player.uniqueId,
                        stageNumber = getLatestStageNumber(gameId)
                    ).first()
                    appendLine("${player.uniqueId} has $countOfCardsLeft cards")
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
            appendLine(otherPlayersMsg(gameId = gameId, playerId = playerId))
            appendLine(leftCardsForOtherPlayersMsg(gameId = gameId, playerId = playerId))
            appendLine(unPlayedCardsMsg(gameId = gameId, playerId = playerId))
            appendLine("The card you play must have a name: ${Constants.CardNames.TWO} or ${Constants.CardNames.JOKER}")
            appendLine(requestCardMsg)
        }

        val card = modelTextResponse(requestGeminiPlayCardMsg)

        Log.d(GamesHubLogTags.OPENAI_CARDS_CONTROLLER, "card to hit: $card")

        val cards =
            gameCardRepository.getUnPlayedCardsForPlayer(
                gameId = gameId,
                playerId = playerId,
                getCurrentStageNumber(gameId)
            )
                .first()

        val chosenCard = cards.find { it.card == card }

        Log.d(GamesHubLogTags.OPENAI_CARDS_CONTROLLER, "cards: ${cards.map { it.card }}")
        Log.d(GamesHubLogTags.OPENAI_CARDS_CONTROLLER, "chosen card: $chosenCard")

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
            "You must choose a new suit of cards. Only reply with a card suit. Do not explain anything"

        val requestGeminiPlayCardMsg = buildString {
            appendLine(otherPlayersMsg(gameId = gameId, playerId = playerId))
            appendLine(leftCardsForOtherPlayersMsg(gameId = gameId, playerId = playerId))
            appendLine(unPlayedCardsMsg(gameId, playerId))
            appendLine("The card suit must be one of the following: ${Constants.CardTypes.CLUBS}, ${Constants.CardTypes.DIAMOND}, ${Constants.CardTypes.HEART}, ${Constants.CardTypes.SPADE}")
            appendLine(requestCardMsg)
        }

        val cardType = modelTextResponse(requestGeminiPlayCardMsg)?.lowercase()

        val cardTypes = listOf(
            Constants.CardTypes.CLUBS,
            Constants.CardTypes.DIAMOND,
            Constants.CardTypes.HEART,
            Constants.CardTypes.SPADE
        )

        val chosenCardType = cardTypes.find { it == cardType }

        Log.d(GamesHubLogTags.OPENAI_CARDS_CONTROLLER, "card type chosen: $cardType")

        if (chosenCardType == null) {
            Log.d(GamesHubLogTags.OPENAI_CARDS_CONTROLLER, "Failed to decide on a type")
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
            "You must choose whether you want to hit or change the suit of cards. Do not explain anything"

        val requestGeminiPlayCardMsg = buildString {
            appendLine(otherPlayersMsg(gameId = gameId, playerId = playerId))
            appendLine(leftCardsForOtherPlayersMsg(gameId = gameId, playerId = playerId))
            appendLine(unPlayedCardsMsg(gameId, playerId))
            appendLine("The option is one of the following: ${Constants.CardJokerActions.hit}, ${Constants.CardJokerActions.changeType}")
            appendLine(requestCardMsg)
        }

        val action = modelTextResponse(requestGeminiPlayCardMsg)

        val actions = listOf(
            Constants.CardJokerActions.hit,
            Constants.CardJokerActions.changeType
        )

        val chosenCardType = actions.find { it == action }

        Log.d(GamesHubLogTags.OPENAI_CARDS_CONTROLLER, "card type chosen: $action")

        if (chosenCardType == null) {
            Log.d(GamesHubLogTags.OPENAI_CARDS_CONTROLLER, "Failed to decide on a type")
        }
        return chosenCardType ?: actions.random()
    }

    override suspend fun playCardForComputer(
        playerId: String,
        gameId: String
    ): Response<UserCard?> {

        return try {
            val currentCardType = getCurrentCardType()
            val currentCardName = getCurrentCardName()

            val card = if (currentCardType.isEmpty()) {
                playCardWithNoRestrictions(gameId, playerId).getNullableDataOrThrowException()
            } else {

                if (currentCardName.isEmpty()) {
                    playCardWithCardTypeRestrictions(
                        gameId,
                        playerId,
                        currentCardType
                    ).getNullableDataOrThrowException()
                } else {
                    playCardWithCardTypeAndNameRestrictions(
                        gameId,
                        playerId,
                        currentCardType,
                        currentCardName
                    ).getNullableDataOrThrowException()
                }

            }
            Response.Success(card)
        } catch (e: Exception) {
            Response.Error(e.message.toString())
        }

    }

    private suspend fun playCardWithCardTypeAndNameRestrictions(
        gameId: String,
        playerId: String,
        currentCardType: String,
        currentCardName: String
    ): Response<UserCard?> {

        return try {
            // You in a game with :
            // *** has 5 cards left
            // Your cards are:
            // current card type is: current card name is:
            // play message
            val requestCardMsg =
                "Which card would you like to play? Only reply with a card or response of \"pick\". Do not explain anything"

            val requestGeminiPlayCardMsg = buildString {
                appendLine(otherPlayersMsg(gameId = gameId, playerId = playerId))
                appendLine(leftCardsForOtherPlayersMsg(gameId = gameId, playerId = playerId))
                appendLine(unPlayedCardsMsg(gameId = gameId, playerId = playerId))
                appendLine("The card you play must have a card suit: $currentCardType OR have a name: $currentCardName or ${Constants.CardNames.EIGHT} or ${Constants.CardNames.JOKER}")
                appendLine(requestCardMsg)
            }

            val card = modelTextResponse(requestGeminiPlayCardMsg)




            Log.d(
                GamesHubLogTags.OPENAI_CARDS_CONTROLLER,
                "card with TypeAndNameRestrictions: $card"
            )
            val cards =
                gameCardRepository.getUnPlayedCardsForPlayer(
                    gameId = gameId,
                    playerId = playerId,
                    getCurrentStageNumber(gameId)
                )
                    .first()
            val chosenCard = cards.find { it.card == card?.trim() }
            val takenCards = cards.filter { it.card == card }

            Log.d(
                GamesHubLogTags.OPENAI_CARDS_CONTROLLER,
                "taken cards: ${takenCards.map { it.card }}"
            )
            Log.d(GamesHubLogTags.OPENAI_CARDS_CONTROLLER, "cards: ${cards.map { it.card }}")
            Log.d(GamesHubLogTags.OPENAI_CARDS_CONTROLLER, "chosen card: $chosenCard")
            Response.Success(chosenCard)
        } catch (e: Exception) {
            Response.Error(e.message.toString())
        }

    }

    private suspend fun playCardWithCardTypeRestrictions(
        gameId: String,
        playerId: String,
        currentCardType: String
    ): Response<UserCard> {

        return try {
            // You in a game with :
            // *** has 5 cards left
            // Your cards are:
            // current card type is: current card name is:
            // play message
            val requestCardMsg =
                "Which card would you like to play? Only reply with a card or response of \"pick\". Do not explain anything"

            val requestGeminiPlayCardMsg = buildString {
                appendLine(otherPlayersMsg(gameId = gameId, playerId = playerId))
                appendLine(leftCardsForOtherPlayersMsg(gameId = gameId, playerId = playerId))
                appendLine(unPlayedCardsMsg(gameId = gameId, playerId = playerId))
                appendLine("The card you play must have a suit: $currentCardType")
                appendLine(requestCardMsg)
            }

            val card = modelTextResponse(requestGeminiPlayCardMsg)
            Log.d(GamesHubLogTags.OPENAI_CARDS_CONTROLLER, "card with TypeRestrictions: $card")

            val cards =
                gameCardRepository.getUnPlayedCardsForPlayer(
                    gameId = gameId,
                    playerId = playerId,
                    getCurrentStageNumber(gameId)
                )
                    .first()
            val chosenCard =
                cards.find { it.card == card } ?: throw Exception("User card not found")

            Log.d(GamesHubLogTags.OPENAI_CARDS_CONTROLLER, "cards: ${cards.map { it.card }}")
            Log.d(GamesHubLogTags.OPENAI_CARDS_CONTROLLER, "chosen card: $chosenCard")
            Response.Success(chosenCard)
        } catch (e: Exception) {
            Response.Error(e.message.toString())
        }
    }

}