package com.learningis4fun.gameshub.crusade_cards.commons.domain.util

import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameCardWithCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.UserCard
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun GameCardWithCard.toUserCard() = UserCard(
    gameCardId = this.gameCard.id,
    playerId = this.gameCard.playerId,
    cardId = this.card.id,
    name = this.card.name,
    type = this.card.type,
    gameId = this.gameCard.gameId,
    isPlayed = this.gameCard.isPlayed,
    image = this.card.image,
    playTime = this.gameCard.playTime,
    pickedTime = this.gameCard.pickedTime
)

fun Int.toHiddenCardsCountAboveThree(): String {
    return if (this > 3) {
        "3+"
    } else {
        "$this"
    }
}

fun Int.toDoubleDigits(): String {
    return if (this < 10) "0${this}" else "$this"
}

fun Long.toUIDuration(): String = this.toDuration(DurationUnit.MILLISECONDS).toString()

fun Long.toTimeFormatted(): String {

    val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())

    return timeFormat.format(this)
}

fun String?.toWinnerNames(player: Player?): String {
    return if (player?.uniqueId == this) "You" else player?.profileName ?: ""
}

data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
) : Serializable {

    /**
     * Returns string representation of the [Quadruple] including its [first], [second], [third] and [fourth] values.
     */
    override fun toString(): String = "($first, $second, $third, $fourth)"
}

data class FiveFold<out A, out B, out C, out D, out E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
) : Serializable {

    /**
     * Returns string representation of the [Triple] including its [first], [second], [third], [fourth] and values.
     */
    override fun toString(): String = "($first, $second, $third, $fourth, $fifth)"
}

fun <T> FiveFold<T, T, T, T, T>.toList(): List<T> =
    listOf(first, second, third, fourth, fifth)

data class SixFold<out A, out B, out C, out D, out E, out F>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F
) : Serializable {

    /**
     * Returns string representation of the [Triple] including its [first], [second], [third], [fourth], [fifth] and [sixth] values.
     */
    override fun toString(): String = "($first, $second, $third, $fourth, $fifth, $sixth)"
}

fun <T> SixFold<T, T, T, T, T, T>.toList(): List<T> =
    listOf(first, second, third, fourth, fifth, sixth)