package com.stafeewa.photocalorie.app.data.mapper

import com.stafeewa.photocalorie.app.data.local.ProductDbModel
import com.stafeewa.photocalorie.app.domain.entity.Product

private fun String.toKeywordList(): List<String> {
    return split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
}

private fun List<String>.toKeywordString(): String {
    return map { it.trim().lowercase() }
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(",")
}

fun ProductDbModel.toDomain(): Product {
    return Product(
        id = this.id,
        name = this.name,
        mealType = this.mealType,
        defaultPortion = this.defaultPortion,
        proteinPer100g = this.proteinPer100g,
        fatPer100g = this.fatPer100g,
        carbsPer100g = this.carbsPer100g,
        caloriesPer100g = this.caloriesPer100g,
        keywords = this.searchKeywords.toKeywordList()
    )
}

fun Product.toDbModel(): ProductDbModel {
    return ProductDbModel(
        id = this.id,
        name = this.name,
        mealType = this.mealType,
        defaultPortion = this.defaultPortion,
        proteinPer100g = this.proteinPer100g,
        fatPer100g = this.fatPer100g,
        carbsPer100g = this.carbsPer100g,
        caloriesPer100g = this.caloriesPer100g,
        searchKeywords = this.keywords.toKeywordString()
    )
}
