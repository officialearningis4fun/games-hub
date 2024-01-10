package com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util

sealed class JokerActions(val action: String){
    object Hit: JokerActions("hit")
    object ChangeType: JokerActions("change type")
}
