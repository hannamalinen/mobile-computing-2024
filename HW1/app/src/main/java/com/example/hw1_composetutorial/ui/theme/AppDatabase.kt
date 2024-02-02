package com.example.hw1_composetutorial.ui.theme

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserProfile::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
}