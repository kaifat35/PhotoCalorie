package com.stafeewa.photocalorie.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.stafeewa.photocalorie.app.domain.entity.TrainingExample

@Database(
    entities = [RecipeDbModel::class, SubscriptionDbModel::class, User::class,
        FoodEntryDbModel::class, ProductDbModel::class, TrainingExample::class],
    version = 5,
    exportSchema = false
)

abstract class PhotoCalorieDatabase : RoomDatabase() {
    abstract fun photoCalorieDao(): PhotoCalorieDao

    abstract fun productDao(): ProductDao

    abstract fun trainingExampleDao(): TrainingExampleDao

}