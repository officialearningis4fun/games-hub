package com.learningis4fun.gameshub.crusade_cards.commons.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class Player(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "uniqueId")
    val uniqueId: String = "",
    @ColumnInfo(name = "email")
    val email: String = "",
    @ColumnInfo(name = "phone")
    val phone: String = "",
    @ColumnInfo(name = "imageUrl")
    val imageUrl: String = "",
    @ColumnInfo(name = "profile")
    val profileName: String = ""
) {

    fun isPlayerEmpty(): Boolean {
        return uniqueId.isEmpty() || imageUrl.isEmpty()
    }
}
