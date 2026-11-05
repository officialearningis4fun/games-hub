package com.learningis4fun.gameshub.commons.domain.util

import com.google.firebase.Timestamp
import com.learningis4fun.gameshub.commons.domain.model.Card
import com.learningis4fun.gameshub.commons.domain.model.Player
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun Int.toHiddenCardsCountAboveThree(): String {
    return if (this > 3) {
        "3+"
    } else {
        "$this"
    }
}

fun String.isProfileNameCountLimitReached(): Boolean {
    return this.trim().length > Constants.PROFILE_NAME_COUNT_LIMIT
}

fun Int.toDoubleDigits(): String {
    return if (this < 10) "0${this}" else "$this"
}

fun Long.toUIDuration(): String = this.toDuration(DurationUnit.MILLISECONDS).toString()

fun Long.toTimeFormatted(): String {

    val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())

    return timeFormat.format(this)
}

fun Player.playerName(loggedInUserId: String = ""): String {
    return if (this.uniqueId == loggedInUserId) "You" else this.profileName.take(4)
}

fun String?.toWinnerNames(player: Player?): String {
    return if (player?.uniqueId == this) "You" else player?.profileName ?: ""
}

fun Card.numberRepresentation(): Int {
    return when (this.name) {
        Constants.CardNames.ACE -> 11
        Constants.CardNames.TWO -> 2
        Constants.CardNames.THREE -> 3
        Constants.CardNames.FOUR -> 4
        Constants.CardNames.FIVE -> 5
        Constants.CardNames.SIX -> 6
        Constants.CardNames.SEVEN -> 7
        Constants.CardNames.EIGHT -> 20
        Constants.CardNames.NINE -> 9
        Constants.CardNames.TEN -> 10
        Constants.CardNames.JACK -> 12
        Constants.CardNames.QUEEN -> 13
        Constants.CardNames.KING -> 14
        Constants.CardNames.JOKER -> 40
        else -> 0
    }
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

object FirebaseTimestampSerializer : KSerializer<Timestamp> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FirebaseTimestamp", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Timestamp) {
        encoder.encodeLong(value.seconds)
    }

    override fun deserialize(decoder: Decoder): Timestamp {
        val seconds = decoder.decodeLong()
        return Timestamp(seconds, 0)
    }
}