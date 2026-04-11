package com.stafeewa.photocalorie.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RecipeDbModel::class, SubscriptionDbModel::class, User::class, FoodEntryDbModel::class],
    version = 7,
    exportSchema = false
)

abstract class PhotoCalorieDatabase : RoomDatabase() {
    abstract fun caloryAiDao(): PhotoCalorieDao
}