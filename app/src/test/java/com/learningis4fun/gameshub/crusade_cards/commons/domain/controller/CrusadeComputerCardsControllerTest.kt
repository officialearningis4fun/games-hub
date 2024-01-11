package com.learningis4fun.gameshub.crusade_cards.commons.domain.controller

import com.google.common.truth.Truth.assertThat
import com.learningis4fun.gameshub.crusade_cards.common.domain.repository.*
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toUserCard
import com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.data.data_source.relations.GameCardWithCard
import com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.domain.model.GameCard
import com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.domain.use_case.CreateGameUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class CrusadeComputerCardsControllerTest {

    private lateinit var computerCardsController: CrusadeComputerCardsController
    private lateinit var fakeGameRepositoryImpl: FakeGameRepositoryImpl
    private lateinit var fakeCardRepositoryImpl: FakeCardRepositoryImpl
    private lateinit var fakeUserRepositoryImpl: FakeUserRepositoryImpl
    private lateinit var fakeDefaultRepositoryImpl: FakeDefaultRepositoryImpl
    private lateinit var fakeGameCardRepositoryImpl: FakeGameCardRepositoryImpl
    private lateinit var createGameUseCase: CreateGameUseCase

    @Before
    fun setup() {
        fakeDefaultRepositoryImpl = FakeDefaultRepositoryImpl()
        fakeUserRepositoryImpl = FakeUserRepositoryImpl()
        fakeCardRepositoryImpl = FakeCardRepositoryImpl()
        fakeGameRepositoryImpl = FakeGameRepositoryImpl(
            userRepositoryImpl = fakeUserRepositoryImpl,
            defaultRepositoryImpl = fakeDefaultRepositoryImpl
        )
        fakeGameCardRepositoryImpl = FakeGameCardRepositoryImpl(
            cardsRepositoryImpl = fakeCardRepositoryImpl
        )
        computerCardsController = CrusadeComputerCardsController(
            gameRepository = fakeGameRepositoryImpl,
            cardRepository = fakeCardRepositoryImpl,
            gameCardRepository = fakeGameCardRepositoryImpl
        )

        createGameUseCase = CreateGameUseCase(
            repository = fakeDefaultRepositoryImpl,
            gameRepository = fakeGameRepositoryImpl,
            cardRepository = fakeCardRepositoryImpl,
            gameCardRepository = fakeGameCardRepositoryImpl
        )
    }

    @Test
    fun `play card with no restrictions, returns valid card`() = runBlockingTest {

        Constants.Cards.cards.forEachIndexed {index, card ->
            fakeCardRepositoryImpl.insertCard(card.copy(id = index.toString()))
        }
        fakeUserRepositoryImpl.insertDefaultPlayers()
        val participants = fakeUserRepositoryImpl.getPlayers().first()
        val playerId = fakeUserRepositoryImpl.getAndroidUserId()

        val gameId = createGameUseCase.createGame(gameName = "game", participants = participants)
        val card = fakeCardRepositoryImpl.getCards()
            .first {
                it.name == Constants.CardNames.THREE }

        val gameCard = GameCard(
            id = "game_card",
            gameId = gameId,
            cardId = card.id,
            playerId = playerId
        )

        val userCard = GameCardWithCard(
            card = card,
            gameCard = gameCard
        ).toUserCard()

        fakeGameCardRepositoryImpl.insertGameCard(gameCard = gameCard)

        val actual = computerCardsController.playCardWithNoRestrictions(
            gameId = gameId,
            playerId = playerId)
        val expect = Response.Success(userCard, message = "")
        assertThat(actual).isInstanceOf(expect.javaClass)
        assertThat(actual.data).isEqualTo(expect.data)
    }
}