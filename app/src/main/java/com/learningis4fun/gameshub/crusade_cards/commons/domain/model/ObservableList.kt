package com.learningis4fun.gameshub.crusade_cards.commons.domain.model

data class ObservableList<T>(val data : List<T> = emptyList(), val changed : Boolean = false)