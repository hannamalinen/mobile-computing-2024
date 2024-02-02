package com.example.hw1_composetutorial.ui.theme

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
@Dao
interface UserProfileDao {
    @Insert
    suspend fun insertUserProfile(userProfile: UserProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserProfile(userProfile: UserProfile): Long

    @Update
    suspend fun updateUserProfile(userProfile: UserProfile)

    @Query("SELECT * FROM UserProfile ORDER BY id DESC LIMIT 1")
    suspend fun getLastUserProfile(): UserProfile?
}