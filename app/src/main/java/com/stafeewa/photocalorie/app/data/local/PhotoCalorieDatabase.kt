package com.stafeewa.photocalorie.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [User::class,
        FoodEntryDbModel::class, ProductDbModel::class,
        UserFoodPreferencesEntity::class, RecommendationFeedbackEntity::class],
    version = 9,
    exportSchema = false
)

abstract class PhotoCalorieDatabase : RoomDatabase() {
    abstract fun photoCalorieDao(): PhotoCalorieDao

    abstract fun productDao(): ProductDao

    abstract fun userFoodPreferencesDao(): UserFoodPreferencesDao
    abstract fun recommendationFeedbackDao(): RecommendationFeedbackDao

}