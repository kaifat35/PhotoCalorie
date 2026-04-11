package com.stafeewa.photocalorie.app.data.mapper

import com.stafeewa.photocalorie.app.data.local.FoodEntryDbModel
import com.stafeewa.photocalorie.app.domain.entity.FoodEntry


fun FoodEntry.toDbModel(): FoodEntryDbModel {
    return FoodEntryDbModel(id, name, mealType, portion, protein, fat, carbs)
}

fun FoodEntryDbModel.toEntity(): FoodEntry {
    return FoodEntry(id, name, mealType, portion, protein, fat, carbs)
}