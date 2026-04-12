package com.stafeewa.photocalorie.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stafeewa.photocalorie.app.domain.entity.MealType

@Entity(tableName = "products")
data class ProductDbModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val mealType: MealType,
    val defaultPortion: Double,
    val proteinPer100g: Double,
    val fatPer100g: Double,
    val carbsPer100g: Double,
    val caloriesPer100g: Double
)