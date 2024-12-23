package com.learningis4fun.gameshub.util

import com.google.gson.reflect.TypeToken
import com.learningis4fun.gameshub.commons.domain.util.CardStateAndContent
import com.learningis4fun.gameshub.commons.domain.util.CardStates
import com.learningis4fun.gameshub.commons.domain.util.GamePlayerActions

class TypeConverters(
    private val jsonParser: JsonParser
) {

    fun toChangeType(json: String): CardStates.ChangeType? {
        return try {
            jsonParser.fromJson<CardStates.ChangeType>(
                json,
                object : TypeToken<CardStates.ChangeType>() {}.type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fromChangeType(changeType: CardStates.ChangeType): String {
        return try {
            jsonParser.toJson(
                changeType,
                object : TypeToken<CardStates.ChangeType>() {}.type
            ) ?: "{}"
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }

    fun toChance(json: String): CardStates.Chance? {
        return try {
            jsonParser.fromJson<CardStates.Chance>(
                json,
                object : TypeToken<CardStates.Chance>() {}.type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fromChance(chance: CardStates.Chance): String {
        return try {
            jsonParser.toJson(
                chance,
                object : TypeToken<CardStates.Chance>() {}.type
            ) ?: "{}"
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }

    fun toChangedType(json: String): CardStates.ChangedType? {
        return try {
            jsonParser.fromJson<CardStates.ChangedType>(
                json,
                object : TypeToken<CardStates.ChangedType>() {}.type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fromChangedType(changedType: CardStates.ChangedType): String {
        return try {
            jsonParser.toJson(
                changedType,
                object : TypeToken<CardStates.ChangedType>() {}.type
            ) ?: "{}"
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }

    fun toDefineCardAction(json: String): CardStates.DefineCardAction? {
        return try {
            jsonParser.fromJson<CardStates.DefineCardAction>(
                json,
                object : TypeToken<CardStates.DefineCardAction>() {}.type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fromDefineCardAction(defineCardAction: CardStates.DefineCardAction): String {
        return try {
            jsonParser.toJson(
                defineCardAction,
                object : TypeToken<CardStates.DefineCardAction>() {}.type
            ) ?: "{}"
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }

    fun toHit(json: String): CardStates.Hit? {
        return try {
            jsonParser.fromJson<CardStates.Hit>(
                json,
                object : TypeToken<CardStates.Hit>() {}.type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fromHit(hit: CardStates.Hit): String {
        return try {
            jsonParser.toJson(
                hit,
                object : TypeToken<CardStates.Hit>() {}.type
            ) ?: "{}"
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }

    fun toNoAvailableCard(json: String): CardStates.NoAvailableCard? {
        return try {
            jsonParser.fromJson<CardStates.NoAvailableCard>(
                json,
                object : TypeToken<CardStates.NoAvailableCard>() {}.type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fromNoAvailableCard(noAvailableCard: CardStates.NoAvailableCard): String {
        return try {
            jsonParser.toJson(
                noAvailableCard,
                object : TypeToken<CardStates.NoAvailableCard>() {}.type
            ) ?: "{}"
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }

    fun toPick(json: String): CardStates.Pick? {
        return try {
            jsonParser.fromJson<CardStates.Pick>(
                json,
                object : TypeToken<CardStates.Pick>() {}.type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fromPick(pick: CardStates.Pick): String {
        return try {
            jsonParser.toJson(
                pick,
                object : TypeToken<CardStates.Pick>() {}.type
            ) ?: "{}"
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }

    fun toPicked(json: String): CardStates.Picked? {
        return try {
            jsonParser.fromJson<CardStates.Picked>(
                json,
                object : TypeToken<CardStates.Picked>() {}.type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fromPicked(picked: CardStates.Picked): String {
        return try {
            jsonParser.toJson(
                picked,
                object : TypeToken<CardStates.Picked>() {}.type
            ) ?: "{}"
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }

    fun toPlay(json: String): CardStates.Play? {
        return try {
            jsonParser.fromJson<CardStates.Play>(
                json,
                object : TypeToken<CardStates.Play>() {}.type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fromPlay(play: CardStates.Play): String {
        return try {
            jsonParser.toJson(
                play,
                object : TypeToken<CardStates.Play>() {}.type
            ) ?: "{}"
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }

    fun toPlayed(json: String): CardStates.Played? {
        return try {
            jsonParser.fromJson<CardStates.Played>(
                json,
                object : TypeToken<CardStates.Played>() {}.type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fromPlayed(played: CardStates.Played): String {
        return try {
            jsonParser.toJson(
                played,
                object : TypeToken<CardStates.Played>() {}.type
            ) ?: "{}"
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }

    fun toError(json: String): CardStates.Error? {
        return try {
            jsonParser.fromJson<CardStates.Error>(
                json,
                object : TypeToken<CardStates.Error>() {}.type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fromError(error: CardStates.Error): String {
        return try {
            jsonParser.toJson(
                error,
                object : TypeToken<CardStates.Error>() {}.type
            ) ?: "{}"
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }

    fun toCardStateAndContent(json: String): CardStateAndContent? {
        return try {
            jsonParser.fromJson<CardStateAndContent>(
                json,
                object : TypeToken<CardStateAndContent>() {}.type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fromCardStateAndContent(cardStateAndContent: CardStateAndContent): String {
        return try {
            jsonParser.toJson(
                cardStateAndContent,
                object : TypeToken<CardStateAndContent>() {}.type
            ) ?: "{}"
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }

    fun toActionMessages(json: String): GamePlayerActions.ActionMessages? {
        return try {
            jsonParser.fromJson<GamePlayerActions.ActionMessages>(
                json,
                object : TypeToken<GamePlayerActions.ActionMessages>() {}.type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fromActionMessages(actionMessages: GamePlayerActions.ActionMessages): String {
        return try {
            jsonParser.toJson(
                actionMessages,
                object : TypeToken<GamePlayerActions.ActionMessages>() {}.type
            ) ?: "{}"
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }

    fun toPromptPlayerDefineJokerAction(json: String): GamePlayerActions.PromptPlayerDefineJokerCardAction? {
        return try {
            jsonParser.fromJson<GamePlayerActions.PromptPlayerDefineJokerCardAction>(
                json,
                object : TypeToken<GamePlayerActions.PromptPlayerDefineJokerCardAction>() {}.type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fromPromptPlayerDefineJokerAction(promptPlayerDefineJokerCardAction: GamePlayerActions.PromptPlayerDefineJokerCardAction): String {
        return try {
            jsonParser.toJson(
                promptPlayerDefineJokerCardAction,
                object : TypeToken<GamePlayerActions.PromptPlayerDefineJokerCardAction>() {}.type
            ) ?: "{}"
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }

    fun toPromptPlayerHitBack(json: String): GamePlayerActions.PromptPlayerHitBackOrPick? {
        return try {
            jsonParser.fromJson<GamePlayerActions.PromptPlayerHitBackOrPick>(
                json,
                object : TypeToken<GamePlayerActions.PromptPlayerHitBackOrPick>() {}.type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fromPromptPlayerHitBack(promptPlayerHitBackOrPick: GamePlayerActions.PromptPlayerHitBackOrPick): String {
        return try {
            jsonParser.toJson(
                promptPlayerHitBackOrPick,
                object : TypeToken<GamePlayerActions.PromptPlayerHitBackOrPick>() {}.type
            ) ?: "{}"
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }

    fun toPromptPlayerPlayOrPick(json: String): GamePlayerActions.PromptPlayerPlayOrPick? {
        return try {
            jsonParser.fromJson<GamePlayerActions.PromptPlayerPlayOrPick>(
                json,
                object : TypeToken<GamePlayerActions.PromptPlayerPlayOrPick>() {}.type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fromPromptPlayerPlayOrPick(promptPlayerPlayOrPick: GamePlayerActions.PromptPlayerPlayOrPick): String {
        return try {
            jsonParser.toJson(
                promptPlayerPlayOrPick,
                object : TypeToken<GamePlayerActions.PromptPlayerPlayOrPick>() {}.type
            ) ?: "{}"
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }

    fun toPromptPlayerChangeCardType(json: String): GamePlayerActions.PromptPlayerChangeCardType? {
        return try {
            jsonParser.fromJson<GamePlayerActions.PromptPlayerChangeCardType>(
                json,
                object : TypeToken<GamePlayerActions.PromptPlayerChangeCardType>() {}.type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fromPromptPlayerChangeCardType(promptPlayerChangeCardType: GamePlayerActions.PromptPlayerChangeCardType): String {
        return try {
            jsonParser.toJson(
                promptPlayerChangeCardType,
                object : TypeToken<GamePlayerActions.PromptPlayerChangeCardType>() {}.type
            ) ?: "{}"
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }
}