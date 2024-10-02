package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util
import java.lang.reflect.Type

interface JsonParser {

    fun <T>fromJson(json: String, type: Type): T?

    fun <T> toJson(obj: T, type: Type): String?
}