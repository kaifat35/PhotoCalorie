package com.stafeewa.photocalorie.app.data.mapper

import com.stafeewa.photocalorie.app.data.local.FoodEntryDbModel
import com.stafeewa.photocalorie.app.domain.entity.FoodEntry


fun FoodEntry.toDbModel(): FoodEntryDbModel {
    return FoodEntryDbModel(
        id = this.id,
        name = this.name,
        mealType = this.mealType,
        portion = this.portion,
        protein = this.protein,
        fat = this.fat,
        carbs = this.carbs,
        timestamp = this.timestamp
    )
}

fun FoodEntryDbModel.toEntity(): FoodEntry {
    return FoodEntry(
        id = this.id,
        name = this.name,
        mealType = this.mealType,
        portion = this.portion,
        protein = this.protein,
        fat = this.fat,
        carbs = this.carbs,
        timestamp = this.timestamp
    )
}