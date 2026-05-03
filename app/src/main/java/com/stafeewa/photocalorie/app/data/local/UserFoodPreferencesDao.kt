package com.stafeewa.photocalorie.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserFoodPreferencesDao {
    @Query("SELECT * FROM user_food_preferences WHERE userId = :userId")
    fun observePreferences(userId: Int): Flow<UserFoodPreferencesEntity?>

    @Query("SELECT * FROM user_food_preferences WHERE userId = :userId")
    suspend fun getPreferences(userId: Int): UserFoodPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPreferences(entity: UserFoodPreferencesEntity)
}
