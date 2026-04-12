package com.stafeewa.photocalorie.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RecipeDbModel::class, SubscriptionDbModel::class, User::class,
        FoodEntryDbModel::class, ProductDbModel::class],
    version = 3,
    exportSchema = false
)

abstract class PhotoCalorieDatabase : RoomDatabase() {
    abstract fun photoCalorieDao(): PhotoCalorieDao

    abstract fun productDao(): ProductDao
}