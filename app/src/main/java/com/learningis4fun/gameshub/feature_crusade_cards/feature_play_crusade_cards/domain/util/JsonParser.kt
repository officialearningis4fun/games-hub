package com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.util
import java.lang.reflect.Type

interface JsonParser {

    fun <T>fromJson(json: String, type: Type): T?

    fun <T> toJson(obj: T, type: Type): String?
}