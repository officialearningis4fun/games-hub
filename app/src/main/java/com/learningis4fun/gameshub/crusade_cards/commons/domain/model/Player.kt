package com.learningis4fun.gameshub.crusade_cards.commons.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.PlayerScreenInfo
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

    fun toPlayerScreenInfo(): PlayerScreenInfo = PlayerScreenInfo(
        playerId = uniqueId,
        playerName = profileName,
        imageUrl = imageUrl,
        defaultAvatar = R.drawable.avatar_man1,
        numOfCardsLeft = ""
    )
}
