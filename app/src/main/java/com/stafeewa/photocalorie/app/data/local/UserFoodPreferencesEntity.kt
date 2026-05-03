package com.stafeewa.photocalorie.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_food_preferences")
data class UserFoodPreferencesEntity(
    @PrimaryKey val userId: Int,
    val preferredProducts: String = "",
    val excludedProducts: String = "",
    val preferredCategories: String = "",
    val excludedCategories: String = ""
)
