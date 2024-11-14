package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.local.GamesDatabase
import com.learningis4fun.gameshub.crusade_cards.commons.domain.controller.MultiplayerCrusadeCardsController
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.CardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameStageRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStatesDefaultMsg
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.GamePlayerActions
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.GameCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util.getDataOrThrowException
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class)
@MediumTest
@HiltAndroidTest
class HandleCardStateUseCaseTest {

    @get:Rule
    var hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named("test-db")
    lateinit var db: GamesDatabase

    @Before
    fun setup() {
        hiltAndroidRule.inject()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Inject
    lateinit var handleCardStateUseCase: HandleCardStateUseCase

    @Inject
    lateinit var multiplayerCrusadeCardsController: MultiplayerCrusadeCardsController

    @Inject
    lateinit var crusadeCardsPreferencesManager: CrusadeCardsPreferencesManager

    @Inject
    lateinit var gameRepository: GameRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var createGameUseCase: CreateGameUseCase

    @Inject
    lateinit var gameStageRepository: GameStageRepository

    @Inject
    lateinit var gameCardRepository: GameCardRepository

    @Inject
    lateinit var defaultCardRepository: DefaultCardRepository

    @Inject
    lateinit var cardRepository: CardRepository


    @Test
    fun handleCardStateWhenDevicePlaysJack() = runTest {


        // add players
        val players = listOf(
            Constants.Players.USER,
            Constants.Players.DEVICE1,
            Constants.Players.DEVICE2,
            Constants.Players.DEVICE3,
        )

        players.forEach { player ->
            userRepository.savePlayer(player)
        }

        // create game

        val gameId = createGameUseCase.createGame(
            gameName = "001",
            participants = players.mapIndexed { index, player -> Pair(player, index + 1) }
        )

        // go to next stage in a game
        val currentGameStage =
            gameStageRepository.getGameStages(gameId).getDataOrThrowException().first()

        val nextStage = currentGameStage.nextStage(
            playerIds = currentGameStage.playerIds.filter {
                it.playerId in listOf(
                    Constants.Players.DEVICE1.uniqueId,
                    Constants.Players.DEVICE2.uniqueId
                )
            }
        )

        gameStageRepository.saveGameStage(nextStage)

        val player = Constants.Players.DEVICE1

        val card = Constants.Cards.cards.first { it.name == Constants.CardNames.JACK }.copy(id = "cardId")

        val gameCard = GameCard(
            id = "id",
            gameId = gameId,
            cardId = card.id,
            playerId = player.uniqueId,
            pickedTime = 10L
        )

        cardRepository.insertCard(card)

        gameCardRepository.insertGameCard(
            gameCard = gameCard
        )

        gameCardRepository.updateGameCard(
            gameCard.copy(
                playerId = player.uniqueId,
                pickedTime = 10L
            )
        )

        val playerCard = gameCardRepository.getUnPlayedCardsForPlayer(
            gameId = gameId,
            playerId = player.uniqueId,
            stageNumber = 1
        ).first().first()


        /*  val cardState = multiplayerCrusadeCardsController.playCard(
              userCard = playerCard
          )

          val secondStageGameWithPlayers =
              gameRepository.getGameWithLatestStagePlayers(gameId).first()*/


        val cardStates = CardStates.Play(playerId = player.uniqueId)

       val playersCards = gameCardRepository.getUnPlayedCardsForPlayer(gameId, playerId = player.uniqueId, stageNumber = 1).first()

        println("player's cards: $playersCards")
        handleCardStateUseCase.saveCardStateToPreferences(CardStates.Play(player.uniqueId))
        crusadeCardsPreferencesManager.updateRecentCardType("")

        val gamePlayerActions = handleCardStateUseCase.handleCardState(
            state = cardStates,
            gameId = gameId
        )

        val recentCardState = crusadeCardsPreferencesManager.recentCardState.first()

        assertThat(recentCardState).isEqualTo(CardStates.Chance(player.uniqueId))
        assertThat(gamePlayerActions).isEqualTo(GamePlayerActions.ActionMessages(message = CardStatesDefaultMsg.Play))
    }
}