package com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model

data class ObservableList<T>(val data : List<T> = emptyList(), val changed : Boolean = false)