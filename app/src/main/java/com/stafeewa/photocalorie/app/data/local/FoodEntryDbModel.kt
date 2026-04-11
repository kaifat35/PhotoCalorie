package com.stafeewa.photocalorie.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stafeewa.photocalorie.app.domain.entity.MealType

@Entity(tableName = "food_entry")
data class FoodEntryDbModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val mealType: MealType,
    val portion: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    val calories: Double
        get() = protein * 4 + fat * 9 + carbs * 4
}