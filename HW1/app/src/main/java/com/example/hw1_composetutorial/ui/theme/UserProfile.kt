package com.example.hw1_composetutorial.ui.theme
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val imageUri: String
)