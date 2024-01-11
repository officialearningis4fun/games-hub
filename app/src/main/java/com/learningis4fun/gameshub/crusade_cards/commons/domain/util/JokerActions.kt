package com.learningis4fun.gameshub.crusade_cards.commons.domain.util

sealed class JokerActions(val action: String){
    object Hit: JokerActions("hit")
    object ChangeType: JokerActions("change type")
}
