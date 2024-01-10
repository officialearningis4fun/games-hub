package com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.data.data_source.relations.GameCardWithCard
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.UserCard
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Triple

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

fun Int.toHiddenCardsCountAboveThree() : String{
   return if(this > 3){
        "3+"
    }else {
        "$this"
    }
}

fun Long.toTimeFormatted(): String{
    val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
    return timeFormat.format(this)
}

fun showKeyboard(view: View) {
    view.post {
        view.requestFocus()
        if (view.requestFocus()) {
            val inputMethodManager: InputMethodManager =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(
                view,
                InputMethodManager.SHOW_IMPLICIT
            )
        }
    }
}

fun hideKeyBoard(activity: Activity?, view: View) {
    if (activity == null) {
        return
    }
    val inputMethodManager: InputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (inputMethodManager.isActive) {
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

public data class FiveFold<out A, out B, out C, out D, out E>(
    public val first: A,
    public val second: B,
    public val third: C,
    public val fourth: D,
    public val fifth: E
) : Serializable {

    /**
     * Returns string representation of the [Triple] including its [first], [second], [third], [fourth] and [firth] values.
     */
    public override fun toString(): String = "($first, $second, $third, $fourth, $fifth)"
}

public fun <T> FiveFold<T, T, T, T, T>.toList() : List<T> = listOf(first, second, third, fourth, fifth)

public data class SixFold<out A, out B, out C, out D, out E, out F>(
    public val first: A,
    public val second: B,
    public val third: C,
    public val fourth: D,
    public val fifth: E,
    public val sixth: F
) : Serializable {

    /**
     * Returns string representation of the [Triple] including its [first], [second], [third], [fourth], [fifth] and [sixth] values.
     */
    public override fun toString(): String = "($first, $second, $third, $fourth, $fifth, $sixth)"
}

public fun <T> SixFold<T, T, T, T, T, T>.toList() : List<T> = listOf(first, second, third, fourth, fifth, sixth)