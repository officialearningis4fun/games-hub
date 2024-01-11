package com.learningis4fun.gameshub.crusade_cards.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.learningis4fun.gameshub.FakeRepository
import com.learningis4fun.gameshub.MainCoroutineRule
import com.learningis4fun.gameshub.MainViewModel
import com.learningis4fun.gameshub.crusade_cards.common.domain.repository.FakeCardRepositoryImpl
import com.learningis4fun.gameshub.crusade_cards.common.domain.repository.FakeGameCardRepositoryImpl
import com.learningis4fun.gameshub.crusade_cards.common.domain.repository.FakeGameRepositoryImpl
import com.learningis4fun.gameshub.crusade_cards.common.domain.repository.FakeUserRepositoryImpl
import com.learningis4fun.gameshub.crusade_cards.commons.domain.controller.CardsController
import com.learningis4fun.gameshub.crusade_cards.commons.domain.controller.CrusadeComputerCardsController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule

@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var mainViewModel: MainViewModel
    private lateinit var repository: FakeRepository
    private lateinit var cardsController: CardsController
    private lateinit var crusadeComputerCardsController: CrusadeComputerCardsController
    private lateinit var fakeGameCardRepositoryImpl: FakeGameCardRepositoryImpl
    private lateinit var fakeCardRepositoryImpl: FakeCardRepositoryImpl
    private lateinit var fakeGameRepositoryImpl: FakeGameRepositoryImpl
    private lateinit var fakeUserRepositoryImpl: FakeUserRepositoryImpl
/*
    @Before
    fun setup() {
        fakeCardRepositoryImpl = FakeCardRepositoryImpl()
        fakeUserRepositoryImpl = FakeUserRepositoryImpl()
        fakeGameCardRepositoryImpl = FakeGameCardRepositoryImpl(fakeCardRepositoryImpl)
        repository = FakeRepository(
            gameCardRepositoryImpl = fakeGameCardRepositoryImpl,
            cardRepositoryImpl = fakeCardRepositoryImpl
        )
        crusadeComputerCardsController = CrusadeComputerCardsController(
            repository = repository,
            gameRepository = fakeGameRepositoryImpl,
            cardRepository = fakeCardRepositoryImpl
        )

        cardsController = CardsController(
            repository = repository,
            gameRepository = fakeGameRepositoryImpl,
            cardRepository = fakeCardRepositoryImpl,
            crusadeComputerCardsController = crusadeComputerCardsController,
            gameCardRepository = fakeGameCardRepositoryImpl,
            userRepository = fakeUserRepositoryImpl
        )
        mainViewModel = MainViewModel(repository, cardsController)
    }*/


    /**
     * FAKING USER ACTIONS
     */

    /* @Test
     fun `faking user reaction when changing type of cards when requested to, update recentState to changedType with user player's id`() =
         runBlockingTest {

             val userPlayerId = Constants.Players.GUEST_PLAYER.id

             val expected = CardStates.ChangedType(
                 playerId = userPlayerId
             )

             val initialState = CardStates.ChangeType(
                 playerId = userPlayerId
             )

             val job0 = launch {
                 mainViewModel.handleCardState(initialState)
             }

             var event: MainViewModel.PlayCardsEvents =
                 MainViewModel.PlayCardsEvents.ShowErrorMessage("")

             val job1 = launch {
                 event = mainViewModel.playCardsEvents.first()
             }

             val type = Constants.CardTypes.CLUBS

             // user should hit back
             mainViewModel.fakeUsersActionsForTesting(
                 event = event,
                 type = type
             )

             var actual: CardStates = CardStates.Error("error")
             val job2 = launch {
                 actual = mainViewModel.currCardState.first()
             }

             assertThat(actual).isEqualTo(expected)
             job0.cancel()
             job1.cancel()
             job2.cancel()

         }

     @Test
     fun `faking user reaction when defining joker action when requested to, update recentState to hit with other player's id`() =
         runBlockingTest {

             val userPlayerId = Constants.Players.GUEST_PLAYER.id

             val expected = CardStates.Hit(
                 playerId = Constants.Players.ANDROID_PLAYER.id,
                 cardsToPick = 4
             )

             val userCard = repository.createGameAndCard(
                 playerId = userPlayerId,
                 cardName = Constants.CardNames.JOKER,
                 cardType = Constants.CardTypes.BLACK_JOKER
             )

             val initialState = CardStates.DefineCardAction(
                 playerId = userCard.playerId,
                 gameCardId = userCard.gameCardId
             )

             val job0 = launch {
                 mainViewModel.handleCardState(initialState)
             }

             var event: MainViewModel.PlayCardsEvents =
                 MainViewModel.PlayCardsEvents.ShowErrorMessage("")

             val job1 = launch {
                 event = mainViewModel.playCardsEvents.first()
             }

             // user should hit back
             mainViewModel.fakeUsersActionsForTesting(
                 event = event,
                 action = JokerActions.Hit
             )

             var actual: CardStates = CardStates.Error("error")

             val job2 = launch {
                 actual = mainViewModel.currCardState.first()
             }

             assertThat(actual).isEqualTo(expected)
             job0.cancel()
             job1.cancel()
             job2.cancel()

         }

     @Test
     fun `faking user reaction when defining joker action when requested to, update recentState to changeType with user player's id`() =
         runBlockingTest {

             val userPlayerId = Constants.Players.GUEST_PLAYER.id
             val expected = CardStates.ChangeType(
                 playerId = userPlayerId
             )

             val userCard = repository.createGameAndCard(
                 playerId = userPlayerId,
                 cardName = Constants.CardNames.JOKER,
                 cardType = Constants.CardTypes.BLACK_JOKER
             )

             val initialState = CardStates.DefineCardAction(
                 playerId = userCard.playerId,
                 gameCardId = userCard.gameCardId
             )
             val job0 = launch {
                 mainViewModel.handleCardState(initialState)
             }

             var event: MainViewModel.PlayCardsEvents =
                 MainViewModel.PlayCardsEvents.ShowErrorMessage("")

             val job1 = launch {
                 event = mainViewModel.playCardsEvents.first()
             }

             // user should hit back
             mainViewModel.fakeUsersActionsForTesting(
                 event = event,
                 action = JokerActions.ChangeType
             )

             var actual: CardStates = CardStates.Error("error")

             val job2 = launch {
                 actual = mainViewModel.currCardState.first()
             }

             assertThat(actual).isEqualTo(expected)
             job0.cancel()
             job1.cancel()
             job2.cancel()

         }

     @Test
     fun `faking user reaction when hitting back, update recentState to hit with other player's id`() =
         runBlockingTest {

             val expected = CardStates.Hit(
                 playerId = Constants.Players.ANDROID_PLAYER.id,
                 cardsToPick = 4
             )

             val userPlayerId = Constants.Players.GUEST_PLAYER.id

             val initialState = CardStates.Hit(
                 playerId = userPlayerId,
                 cardsToPick = 2,
                 message = ""
             )
             val job0 = launch {
                 mainViewModel.handleCardState(initialState)
             }

             var event: MainViewModel.PlayCardsEvents =
                 MainViewModel.PlayCardsEvents.ShowErrorMessage("")

             val job1 = launch {
                 event = mainViewModel.playCardsEvents.first()
             }

             val userCard = repository.createGameAndCard(
                 playerId = userPlayerId,
                 cardName = Constants.CardNames.TWO
             )

             // user should hit back
             mainViewModel.fakeUsersActionsForTesting(
                 event = event,
                 userCard = userCard
             )

             var actual: CardStates = CardStates.Error("error")

             val job2 = launch {
                 actual = mainViewModel.currCardState.first()
             }

             assertThat(actual).isEqualTo(expected)
             job0.cancel()
             job1.cancel()
             job2.cancel()

         }

     @Test
     fun `faking user reaction when picking cards after being hit, update recentState to pick with user player's id`() =
         runBlockingTest {

             val userPlayerId = Constants.Players.GUEST_PLAYER.id
             val cardsToPick = 2

             val expected = CardStates.Pick(
                 playerId = userPlayerId,
                 numOfCardsToPick = 2,
                 canPlay = false
             )

             val initialState = CardStates.Hit(
                 playerId = userPlayerId,
                 cardsToPick = cardsToPick
             )

             val job0 = launch {
                 mainViewModel.handleCardState(initialState)
             }

             var event: MainViewModel.PlayCardsEvents =
                 MainViewModel.PlayCardsEvents.ShowErrorMessage("")

             val job1 = launch {
                 event = mainViewModel.playCardsEvents.first()
             }

             // user should hit back
             mainViewModel.fakeUsersActionsForTesting(
                 event = event,
                 hitBack = false
             )

             var actual: CardStates = CardStates.Error("error")

             val job2 = launch {
                 actual = mainViewModel.currCardState.first()
             }

             assertThat(actual).isEqualTo(expected)
             job0.cancel()
             job1.cancel()
             job2.cancel()

         }

     @Test
     fun `faking user reaction when picking cards if has no card to play, update recentState to pick with user player's id`() =
         runBlockingTest {
             val userPlayerId = Constants.Players.GUEST_PLAYER.id

             val initialState = CardStates.Play(
                 playerId = userPlayerId,
                 canPick = true
             )

             val job0 = launch {
                 mainViewModel.handleCardState(initialState)
             }

             var event: MainViewModel.PlayCardsEvents =
                 MainViewModel.PlayCardsEvents.ShowErrorMessage("")

             val job1 = launch {
                 event = mainViewModel.playCardsEvents.first()
             }

             // user should hit back
             mainViewModel.fakeUsersActionsForTesting(
                 event = event,
                 pickInsteadOfPlay = true,
             )

             var actual: CardStates = CardStates.Error("error")

             val job2 = launch {
                 actual = mainViewModel.currCardState.first()
             }

             val expected = CardStates.Pick(
                 playerId = userPlayerId,
                 numOfCardsToPick = 1,
                 canPlay = true
             )

             assertThat(actual).isEqualTo(expected)
             job0.cancel()
             job1.cancel()
             job2.cancel()

         }

     */
    /**
     * There are many results here... test if the cardState change
     *//*
    @Test
    fun `faking user reaction when playing a card, update recentState to a diff state`() =
        runBlockingTest {
            val userPlayerId = Constants.Players.GUEST_PLAYER.id

            val unexpectedState = CardStates.Play::class.java

            val initialState = CardStates.Play(
                playerId = userPlayerId,
                canPick = true
            )

            val job0 = launch {
                mainViewModel.handleCardState(initialState)
            }

            var event: MainViewModel.PlayCardsEvents =
                MainViewModel.PlayCardsEvents.ShowErrorMessage("")

            val job1 = launch {
                event = mainViewModel.playCardsEvents.first()
            }

            // user should hit back
            mainViewModel.fakeUsersActionsForTesting(
                event = event,
                pickInsteadOfPlay = false,
            )

            var actual: CardStates = CardStates.Error("error")

            val job2 = launch {
                actual = mainViewModel.currCardState.first()
            }

            assertThat(actual).isNotInstanceOf(unexpectedState)
            job0.cancel()
            job1.cancel()
            job2.cancel()

        }*/
/*
    @Test
    fun `playCardsGameScopedToScreen from com start game, should request other user play`() =
        runBlocking {
            // insert cards
            val cards = Constants.Cards.cards
            cards.forEach { card ->
                repository.insertCard(card)
            }
            // create game
            mainViewModel.createGameOrResume()

            *//*val trackingPlays: MutableList<TrackingPlay> = ArrayList()
            mainViewModel.playingTest().toList(trackingPlays)*//*

            mainViewModel.playCardsGameWhenOnCardStateChange()
            mainViewModel.playCardsGameWhenOnCardStateChange()
            mainViewModel.playCardsGameWhenOnCardStateChange()
            mainViewModel.playCardsGameWhenOnCardStateChange()
            mainViewModel.playCardsGameWhenOnCardStateChange()
            val tracking = mainViewModel.playCardsGameWhenOnCardStateChange()

            val expect = PlayCardsScopedTestResult(
                tracking = TrackingPlay.ContinuePlaying,
                cardState = CardStates.Play(
                    playerId = Constants.Players.GUEST_PLAYER.id,
                    canPick = true,
                    message = CardStatesDefaultMsg.Play
                ),
                numOfPlayedCardsPerUser = 1
            )

            val numOfPlayedCards =
                repository.getGameCards(gameId = repository.getCurrentGameId()).count {
                    it.playerId == Constants.Players.ANDROID_PLAYER.id && it.isPlayed
                }
            //val tracking = trackingPlays.last()
            val actual = PlayCardsScopedTestResult(
                tracking = tracking,
                cardState = mainViewModel.currCardState.first(),
                numOfPlayedCardsPerUser = numOfPlayedCards
            )
            assertThat(actual).isEqualTo(expect)
        }

    @Test
    fun `playCardsGameScopedToScreen from played, should request other user play`() =
        runBlocking {
            // insert cards
            val cards = Constants.Cards.cards
            cards.forEach { card ->
                repository.insertCard(card)
            }
            // create game
            mainViewModel.createGameOrResume()
            val state = CardStates.Played(
                playerId = Constants.Players.ANDROID_PLAYER.id
            )
            mainViewModel.saveCardStateToPreferences(state)

            *//*val trackingPlays: MutableList<TrackingPlay> = ArrayList()
            mainViewModel.playingTest().toList(trackingPlays)*//*

            val tracking = mainViewModel.playCardsGameWhenOnCardStateChange()

            val expect = PlayCardsScopedTestResult(
                tracking = TrackingPlay.ContinuePlaying,
                cardState = CardStates.Play(
                    playerId = Constants.Players.GUEST_PLAYER.id,
                    canPick = true
                )
            )

            val numOfPlayedCards =
                repository.getGameCards(gameId = repository.getCurrentGameId()).count {
                    it.playerId == Constants.Players.ANDROID_PLAYER.id && it.isPlayed
                }
            //val tracking = trackingPlays.last()
            val actual = PlayCardsScopedTestResult(
                tracking = tracking,
                cardState = mainViewModel.currCardState.first()
            )
            assertThat(actual).isEqualTo(expect)
        }

    @Test
    fun `anyPlayedAllCards with com played all cards, should return com user object`() =
        runBlockingTest {
            // insert cards
            val card1 = Constants.TestData.card1
            val card2 = Constants.TestData.card2
            repository.insertCard(card1)
            repository.insertCard(card2)

            //insert game cards
            val gameName = "game"
            val gameId = mainViewModel.createGame(gameName = gameName)

            val expect = HasAnyPlayedAllCardsTestResult(
                user = User(
                    id = Constants.Players.ANDROID_PLAYER.id,
                    name = Constants.Players.ANDROID_PLAYER.name
                ),
                cardsLeftEmpty = true
            )

            // assign card to others player only

            val otherPlayersIds = repository.getGameWithPlayers(gameId).first().users
                .filter { it.id != expect.user!!.id }
                .map { it.id }

            val sizeOfOtherPlayers = otherPlayersIds.size

            val gameCards = repository.getGameCards(gameId).take(sizeOfOtherPlayers)

            val haveAssigned = otherPlayersIds.map { false }.toMutableList()
            otherPlayersIds.forEachIndexed { index, playerId ->
                if (!haveAssigned[index]) {
                    repository.updateGameCard(gameCards[index].copy(playerId = playerId))
                    haveAssigned[index] = true
                }
            }

            //mainViewModel.assignCards(gameId = gameId, howManyCardsPerPlayer = 1)
            repository.updateRecentGameId(gameId = gameId)

            val user = mainViewModel.anyPlayedAllCards(gameId = gameId)
            val numOfCardsLeft =
                repository.numOfUnPlayedCardsForPlayer(gameId = gameId, playerId = expect.user!!.id)
                    .first()

            val actual = HasAnyPlayedAllCardsTestResult(
                user = user,
                cardsLeftEmpty = numOfCardsLeft == 0
            )

            assertThat(actual).isEqualTo(expect)
        }

    @Test
    fun `anyPlayedAllCards with non played all cards, should return null`() = runBlockingTest {
        // insert cards
        val card1 = Constants.TestData.card1
        val card2 = Constants.TestData.card2
        repository.insertCard(card1)
        repository.insertCard(card2)

        //insert game cards
        val gameName = "game"
        val gameId = mainViewModel.createGame(gameName = gameName)

        val expect = HasAnyPlayedAllCardsTestResult(
            user = null,
            cardsLeftEmpty = false
        )

        mainViewModel.assignCards(gameId = gameId, howManyCardsPerPlayer = 1)
        repository.updateRecentGameId(gameId = gameId)

        val randomPlayerId = repository.getGameWithPlayers(gameId)
            .first().users
            .random().id

        val numOfLeftCards = repository.numOfUnPlayedCardsForPlayer(
            gameId = gameId,
            playerId = randomPlayerId
        ).first()

        val user = mainViewModel.anyPlayedAllCards(gameId = gameId)

        val actual = HasAnyPlayedAllCardsTestResult(
            user = user,
            cardsLeftEmpty = numOfLeftCards == 0
        )

        assertThat(actual).isEqualTo(expect)
    }


    @Test
    fun `playCardsGameScopedToScreen with com play all cards first, should end game with com a winner`() =
        runBlockingTest {
            // insert cards
            val card1 = Constants.TestData.card1
            val card2 = Constants.TestData.card2
            repository.insertCard(card1)
            repository.insertCard(card2)

            //insert game cards
            val gameName = "game"
            val gameId = mainViewModel.createGame(gameName = gameName)

            val winner = User(
                id = Constants.Players.ANDROID_PLAYER.id,
                name = Constants.Players.ANDROID_PLAYER.name
            )

            mainViewModel.assignCards(gameId = gameId, howManyCardsPerPlayer = 1)
            repository.updateRecentGameId(gameId = gameId)

            val recentGame = repository.getGame(gameId)

            repository.updateGame(game = recentGame.copy(gameState = GameState.Started.name))

            // request com play

            val initialState = CardStates.Play(
                playerId = winner.id,
                canPick = true
            )
            mainViewModel.saveCardStateToPreferences(initialState)
            var tracking: TrackingPlay = TrackingPlay.Error("test defaul error")
            val job0 = launch {
                mainViewModel.playCardsGameWhenOnCardStateChange()
                tracking = mainViewModel.playCardsGameWhenOnCardStateChange()
            }
            val duration = mainViewModel._gameDuration.value.second

            var cardState: CardStates = CardStates.Error("")

            val job1 = launch {
                cardState = mainViewModel.currCardState.first()
            }

            var numOfLeftCards = 10

            val job2 = launch {
                numOfLeftCards = repository.numOfUnPlayedCardsForPlayer(
                    gameId = gameId,
                    playerId = winner.id
                ).first()
            }

            val expect = PlayCardsScopedEndGameTestResult(
                numOfLeftCards = 0,
                deletedAllGameCards = true,
                game = Game(
                    id = gameId,
                    gameDuration = duration,
                    name = gameName,
                    gameState = GameState.Completed.name,
                    winnerId = winner.id
                ),
                cardState = CardStates.Start,
                trackingPlay = TrackingPlay.HasWinner,
                playCardsEvents = MainViewModel.PlayCardsEvents.NavigateToWinScreen(winner.name)
            )

            var playCardsEvents: MainViewModel.PlayCardsEvents =
                MainViewModel.PlayCardsEvents.ShowErrorMessage("")

            val job3 = launch {
                playCardsEvents = mainViewModel.playCardsEvents.first()
            }


            val game = repository.getGame(gameId = gameId)

            val gameCards = repository.getGameCards(gameId)

            val actual = PlayCardsScopedEndGameTestResult(
                numOfLeftCards = numOfLeftCards,
                deletedAllGameCards = gameCards.isEmpty(),
                game = game,
                cardState = cardState,
                trackingPlay = tracking,
                playCardsEvents = playCardsEvents
            )

            assertThat(actual).isEqualTo(expect)
            job0.cancel()
            job1.cancel()
            job2.cancel()
            job3.cancel()
        }

    @Test
    fun `playCardsGameScopedToScreen from com start game, should update cardState to Played and play one card if a card is not jack or seven`() =
        runBlockingTest {

            val comPlayerId = Constants.Players.ANDROID_PLAYER.id
            // insert cards
            val cards = Constants.Cards.cards
            cards.forEach { card ->
                repository.insertCard(card)
            }
            // create game
            mainViewModel.createGameOrResume()

            mainViewModel.playCardsGameWhenOnCardStateChange()
            val tracking = mainViewModel.playCardsGameWhenOnCardStateChange()

            val gameId = repository.getCurrentGameId()

            val numOfPlayedCardsForPlayer = repository.getGameCards(gameId)
                .count {
                    it.isPlayed &&
                            it.playerId == comPlayerId
                }

            val expect = PlayCardsScopedTestResult(
                tracking = TrackingPlay.ContinuePlaying,
                cardState = CardStates.Played(
                    playerId = Constants.Players.ANDROID_PLAYER.id
                ),
                numOfPlayedCardsPerUser = 1
            )

            val actual = PlayCardsScopedTestResult(
                tracking = tracking,
                cardState = mainViewModel.currCardState.first(),
                numOfPlayedCardsPerUser = numOfPlayedCardsForPlayer
            )

            assertThat(actual).isEqualTo(expect)
        }

    @Test
    fun `playCardsGameScopedToScreen with com played all cards, should end game with com a winner`() =
        runBlockingTest {
            // insert cards
            val card1 = Constants.TestData.card1
            val card2 = Constants.TestData.card2
            repository.insertCard(card1)
            repository.insertCard(card2)

            //insert game cards
            val gameName = "game"
            val gameId = mainViewModel.createGame(gameName = gameName)

            val winner = User(
                id = Constants.Players.ANDROID_PLAYER.id,
                name = Constants.Players.ANDROID_PLAYER.name
            )
            val randomGameCard = repository.getGameCards(gameId).first()

            repository.updateGameCard(randomGameCard.copy(playerId = Constants.Players.GUEST_PLAYER.id))
            repository.updateRecentGameId(gameId = gameId)

            val recentGame = repository.getGame(gameId)

            repository.updateGame(game = recentGame.copy(gameState = GameState.Started.name))

            // request com play

            val initialState = CardStates.Played(
                playerId = winner.id,
                message = "Played"
            )
            mainViewModel.saveCardStateToPreferences(initialState)

            val duration = mainViewModel._gameDuration.value.second

            val expect = PlayCardsScopedEndGameTestResult(
                numOfLeftCards = 0,
                deletedAllGameCards = true,
                game = Game(
                    id = gameId,
                    gameDuration = duration,
                    name = gameName,
                    gameState = GameState.Completed.name,
                    winnerId = winner.id
                ),
                cardState = CardStates.Start,
                trackingPlay = TrackingPlay.HasWinner,
                playCardsEvents = MainViewModel.PlayCardsEvents.NavigateToWinScreen(winner.name)
            )

            // com should be requested to play when this run
            val trackings: MutableList<TrackingPlay> = ArrayList()

            // now other user should be requested to play, but com played all cards, com should win
            var tracking: TrackingPlay = TrackingPlay.Error("test default error")
            val job0 = launch {
                tracking = mainViewModel.playCardsGameWhenOnCardStateChange()
            }

            var cardState: CardStates = CardStates.Error("error")

            val job1 = launch {
                cardState = mainViewModel.currCardState.first()
            }

            var numOfLeftCards = 10
            val job2 = launch {
                numOfLeftCards = repository.numOfUnPlayedCardsForPlayer(
                    gameId = gameId,
                    playerId = winner.id
                ).first()
            }

            var playCardsEvents: MainViewModel.PlayCardsEvents =
                MainViewModel.PlayCardsEvents.ShowErrorMessage("")

            val job3 = launch {
                playCardsEvents = mainViewModel.playCardsEvents.first()
            }

            val game = repository.getGame(gameId = gameId)

            val gameCards = repository.getGameCards(gameId)

            val actual = PlayCardsScopedEndGameTestResult(
                numOfLeftCards = numOfLeftCards,
                deletedAllGameCards = gameCards.isEmpty(),
                game = game,
                cardState = cardState,
                trackingPlay = tracking,
                playCardsEvents = playCardsEvents
            )
            assertThat(actual).isEqualTo(expect)

            job0.cancel()
            job1.cancel()
            job2.cancel()
            job3.cancel()
        }*/


}