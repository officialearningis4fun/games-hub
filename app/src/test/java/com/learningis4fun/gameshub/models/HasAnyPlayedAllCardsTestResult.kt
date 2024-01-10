package com.learningis4fun.gameshub.models

import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.User

data class HasAnyPlayedAllCardsTestResult(
    val user : User?,
    val cardsLeftEmpty : Boolean
)
