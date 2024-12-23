package com.learningis4fun.gameshub.commons.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "userId")
    val id: String = "",
    @ColumnInfo(name = "userName")
    val name : String = ""
)
