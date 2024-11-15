package com.learningis4fun.gameshub.crusade_cards.commons.domain.util

import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.User
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.UserCard


object Constants {

    val TEST_BASE_URL = "http://192.168.43.237:8080"

    object CardTypes {
        const val SPADE = "spade"
        const val CLUBS = "club"
        const val DIAMOND = "diamond"
        const val HEART = "heart"
        const val BLACK_JOKER = "black"
        const val RED_JOKER = "red"
    }

    object CardNames {
        const val ACE = "ace"
        const val TWO = "two"
        const val THREE = "three"
        const val FOUR = "four"
        const val FIVE = "five"
        const val SIX = "six"
        const val SEVEN = "seven"
        const val EIGHT = "eight"
        const val NINE = "nine"
        const val TEN = "ten"

        const val KING = "king"
        const val QUEEN = "queen"
        const val JACK = "jack"
        const val JOKER = "joker"
    }

    object CardJokerActions {
        val hit = "hit"
        val changeType = "change-type"
    }

    val HITTING_CARD_NAMES = listOf(
        CardNames.TWO,
        CardNames.JOKER,
    )

    object CrusadePlayingDirection {
        const val CLOCKWISE = "clockwise"
        const val ANTICLOCKWISE = "anticlockwise"
    }

    const val GAME_DURATION_INTERVAL = 1000L

    object Players {
        val ANDROID_PLAYER = User("1", "Android")
        val GUEST_PLAYER = User("2", "Guest")

        val USER = Player(
            uniqueId = "uniqueId",
            imageUrl = R.drawable.avatar_man1.toString(),
            profileName = "user"
        )

        val DEVICE1 = Player(
            uniqueId = "7iooVf8Vu9T8NR2JcwgqLbgr0OC3dev1",
            email = "",
            phone = "",
            imageUrl = R.drawable.ai_platform.toString(),
            profileName = "Dev1"
        )

        val DEVICE2 = Player(
            uniqueId = "7iooVf8Vu9T8NR2JcwgqLbgr0OC3dev2",
            email = "",
            phone = "",
            imageUrl = R.drawable.ai_platform.toString(),
            profileName = "Dev2"
        )

        val DEVICE3 = Player(
            uniqueId = "7iooVf8Vu9T8NR2JcwgqLbgr0OC3dev3",
            email = "",
            phone = "",
            imageUrl = R.drawable.ai_platform.toString(),
            profileName = "Dev3"
        )

    }

    const val PROFILE_NAME_COUNT_LIMIT = 5

    const val DEFAULT_CARDS_PER_USER = 5

    const val DEFAULT_PARTICIPANTS_IN_CRUSADE_GAME = 2
    const val MAX_PARTICIPANTS_IN_CRUSADE_GAME = 4

    object NumberOfCardsOptions {
        val DEFAULT_CARDS_PER_PLAYER = 5
        val MIDDLE_CARDS_PER_PLAYER = 8
        val HIGHEST_CARDS_PER_PLAYER = 10
    }

    const val DEFAULT_CRUSADE_ELIMINATION_LIMIT = 24
    const val MAX_CRUSADE_ELIMINATION_LIMIT = 40

    object TestData {
        val playerId = Players.GUEST_PLAYER.id
        val gameId = "gameId"
        val game = Game(
            id = "gameId",
            name = "fun game"
        )

        val absentPlayerId = "playerId-not-available"
        val absentGameId = "gameId-not-available"

        val card1 = Card(
            id = "cardId",
            name = CardNames.THREE,
            type = CardTypes.HEART,
            image = ""
        )

        val userCard = UserCard(
            gameCardId = "gameCardId",
            playerId = playerId,
            cardId = "cardId",
            name = "",
            gameId = "gameId",
            type = CardTypes.HEART,
            isPlayed = false,
            image = "",
            playTime = 0L
        )
    }

    object Cards {
        val cards = listOf(
            //Ace
            Card(
                name = CardNames.ACE,
                type = CardTypes.CLUBS,
                image = R.drawable.club_1.toString()
            ),
            Card(
                name = CardNames.ACE,
                type = CardTypes.SPADE,
                image = R.drawable.spade_1.toString()
            ),
            Card(
                name = CardNames.ACE,
                type = CardTypes.HEART,
                image = R.drawable.heart_1.toString()
            ),
            Card(
                name = CardNames.ACE,
                type = CardTypes.DIAMOND,
                image = R.drawable.diamond_1.toString()
            ),

            //Two
            Card(
                name = CardNames.TWO,
                type = CardTypes.CLUBS,
                image = R.drawable.club_2.toString()
            ),
            Card(
                name = CardNames.TWO,
                type = CardTypes.SPADE,
                image = R.drawable.spade_2.toString()
            ),
            Card(
                name = CardNames.TWO,
                type = CardTypes.HEART,
                image = R.drawable.heart_2.toString()
            ),
            Card(
                name = CardNames.TWO,
                type = CardTypes.DIAMOND,
                image = R.drawable.diamond_2.toString()
            ),

            //Three
            Card(
                name = CardNames.THREE,
                type = CardTypes.CLUBS,
                image = R.drawable.club_3.toString()
            ),
            Card(
                name = CardNames.THREE,
                type = CardTypes.SPADE,
                image = R.drawable.spade_3.toString()
            ),
            Card(
                name = CardNames.THREE,
                type = CardTypes.HEART,
                image = R.drawable.heart_3.toString()
            ),
            Card(
                name = CardNames.THREE,
                type = CardTypes.DIAMOND,
                image = R.drawable.diamond_3.toString()
            ),

            //Four
            Card(
                name = CardNames.FOUR,
                type = CardTypes.CLUBS,
                image = R.drawable.club_4.toString()
            ),
            Card(
                name = CardNames.FOUR,
                type = CardTypes.SPADE,
                image = R.drawable.spade_4.toString()
            ),
            Card(
                name = CardNames.FOUR,
                type = CardTypes.HEART,
                image = R.drawable.heart_4.toString()
            ),
            Card(
                name = CardNames.FOUR,
                type = CardTypes.DIAMOND,
                image = R.drawable.diamond_4.toString()
            ),

            //Five
            Card(
                name = CardNames.FIVE,
                type = CardTypes.CLUBS,
                image = R.drawable.club_5.toString()
            ),
            Card(
                name = CardNames.FIVE,
                type = CardTypes.SPADE,
                image = R.drawable.spade_5.toString()
            ),
            Card(
                name = CardNames.FIVE,
                type = CardTypes.HEART,
                image = R.drawable.heart_5.toString()
            ),
            Card(
                name = CardNames.FIVE,
                type = CardTypes.DIAMOND,
                image = R.drawable.diamond_5.toString()
            ),

            //Six
            Card(
                name = CardNames.SIX,
                type = CardTypes.CLUBS,
                image = R.drawable.club_6.toString()
            ),
            Card(
                name = CardNames.SIX,
                type = CardTypes.SPADE,
                image = R.drawable.spade_6.toString()
            ),
            Card(
                name = CardNames.SIX,
                type = CardTypes.HEART,
                image = R.drawable.heart_6.toString()
            ),
            Card(
                name = CardNames.SIX,
                type = CardTypes.DIAMOND,
                image = R.drawable.diamond_6.toString()
            ),

            //Seven
            Card(
                name = CardNames.SEVEN,
                type = CardTypes.CLUBS,
                image = R.drawable.club_7.toString()
            ),
            Card(
                name = CardNames.SEVEN,
                type = CardTypes.SPADE,
                image = R.drawable.spade_7.toString()
            ),
            Card(
                name = CardNames.SEVEN,
                type = CardTypes.HEART,
                image = R.drawable.heart_7.toString()
            ),
            Card(
                name = CardNames.SEVEN,
                type = CardTypes.DIAMOND,
                image = R.drawable.diamond_7.toString()
            ),

            //Eight
            Card(
                name = CardNames.EIGHT,
                type = CardTypes.CLUBS,
                image = R.drawable.club_8.toString()
            ),
            Card(
                name = CardNames.EIGHT,
                type = CardTypes.SPADE,
                image = R.drawable.spade_8.toString()
            ),
            Card(
                name = CardNames.EIGHT,
                type = CardTypes.HEART,
                image = R.drawable.heart_8.toString()
            ),
            Card(
                name = CardNames.EIGHT,
                type = CardTypes.DIAMOND,
                image = R.drawable.diamond_8.toString()
            ),

            //Nine
            Card(
                name = CardNames.NINE,
                type = CardTypes.CLUBS,
                image = R.drawable.club_9.toString()
            ),
            Card(
                name = CardNames.NINE,
                type = CardTypes.SPADE,
                image = R.drawable.spade_9.toString()
            ),
            Card(
                name = CardNames.NINE,
                type = CardTypes.HEART,
                image = R.drawable.heart_9.toString()
            ),
            Card(
                name = CardNames.NINE,
                type = CardTypes.DIAMOND,
                image = R.drawable.diamond_9.toString()
            ),

            //Ten
            Card(
                name = CardNames.TEN,
                type = CardTypes.CLUBS,
                image = R.drawable.club_10.toString()
            ),
            Card(
                name = CardNames.TEN,
                type = CardTypes.SPADE,
                image = R.drawable.spade_10.toString()
            ),
            Card(
                name = CardNames.TEN,
                type = CardTypes.HEART,
                image = R.drawable.heart_10.toString()
            ),
            Card(
                name = CardNames.TEN,
                type = CardTypes.DIAMOND,
                image = R.drawable.diamond_10.toString()
            ),

            //Jack
            Card(
                name = CardNames.JACK,
                type = CardTypes.CLUBS,
                image = R.drawable.club_jack.toString()
            ),
            Card(
                name = CardNames.JACK,
                type = CardTypes.SPADE,
                image = R.drawable.spade_jack.toString()
            ),
            Card(
                name = CardNames.JACK,
                type = CardTypes.HEART,
                image = R.drawable.heart_jack.toString()
            ),
            Card(
                name = CardNames.JACK,
                type = CardTypes.DIAMOND,
                image = R.drawable.diamond_jack.toString()
            ),

            //Queen
            Card(
                name = CardNames.QUEEN,
                type = CardTypes.CLUBS,
                image = R.drawable.club_queen.toString()
            ),
            Card(
                name = CardNames.QUEEN,
                type = CardTypes.SPADE,
                image = R.drawable.spade_queen.toString()
            ),
            Card(
                name = CardNames.QUEEN,
                type = CardTypes.HEART,
                image = R.drawable.heart_queen.toString()
            ),
            Card(
                name = CardNames.QUEEN,
                type = CardTypes.DIAMOND,
                image = R.drawable.diamond_queen.toString()
            ),

            //King
            Card(
                name = CardNames.KING,
                type = CardTypes.CLUBS,
                image = R.drawable.club_king.toString()
            ),
            Card(
                name = CardNames.KING,
                type = CardTypes.SPADE,
                image = R.drawable.spade_king.toString()
            ),
            Card(
                name = CardNames.KING,
                type = CardTypes.HEART,
                image = R.drawable.heart_king.toString()
            ),
            Card(
                name = CardNames.KING,
                type = CardTypes.DIAMOND,
                image = R.drawable.diamond_king.toString()
            ),

            //Joker
            Card(
                name = CardNames.JOKER,
                type = CardTypes.BLACK_JOKER,
                image = R.drawable.joker_black.toString()
            ),
            Card(
                name = CardNames.JOKER,
                type = CardTypes.RED_JOKER,
                image = R.drawable.joker_red.toString()
            )

        )
    }

    object EliminationLimitNumRepCard {
        val cards: List<Card> = listOf(
            //Ace

            Card(
                name = CardNames.ACE,
                type = CardTypes.CLUBS,
                image = R.drawable.club_1.toString()
            ),

            //Two

            Card(
                name = CardNames.TWO,
                type = CardTypes.CLUBS,
                image = R.drawable.club_2.toString()
            ),
            //Three

            Card(
                name = CardNames.THREE,
                type = CardTypes.CLUBS,
                image = R.drawable.club_3.toString()
            ),
            //Four
            Card(
                name = CardNames.FOUR,
                type = CardTypes.CLUBS,
                image = R.drawable.club_4.toString()
            ),

            //Five
            Card(
                name = CardNames.FIVE,
                type = CardTypes.CLUBS,
                image = R.drawable.club_5.toString()
            ),

            //Six
            Card(
                name = CardNames.SIX,
                type = CardTypes.CLUBS,
                image = R.drawable.club_6.toString()
            ),
            //Seven
            Card(
                name = CardNames.SEVEN,
                type = CardTypes.CLUBS,
                image = R.drawable.club_7.toString()
            ),
            //Eight
            Card(
                name = CardNames.EIGHT,
                type = CardTypes.CLUBS,
                image = R.drawable.club_8.toString()
            ),

            //Nine
            Card(
                name = CardNames.NINE,
                type = CardTypes.CLUBS,
                image = R.drawable.club_9.toString()
            ),
            //Ten
            Card(
                name = CardNames.TEN,
                type = CardTypes.CLUBS,
                image = R.drawable.club_10.toString()
            ),
            //Jack
            Card(
                name = CardNames.JACK,
                type = CardTypes.CLUBS,
                image = R.drawable.club_jack.toString()
            ),

            //Queen
            Card(
                name = CardNames.QUEEN,
                type = CardTypes.CLUBS,
                image = R.drawable.club_queen.toString()
            ),

            //King
            Card(
                name = CardNames.KING,
                type = CardTypes.CLUBS,
                image = R.drawable.club_king.toString()
            ),


            //Joker
            Card(
                name = CardNames.JOKER,
                type = CardTypes.BLACK_JOKER,
                image = R.drawable.joker_black.toString()
            )
        )
    }


}