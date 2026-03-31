package com.learningis4fun.gameshub.data.data_source

import com.learningis4fun.gameshub.domain.model.UserCard

fun List<List<UserCard>>.closeToWinning(numOfCardsLeft: Int): Boolean {
    val opponentMinCards = this.minOf { it.size }

    return opponentMinCards < numOfCardsLeft
}