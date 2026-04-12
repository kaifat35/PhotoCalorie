package com.stafeewa.photocalorie.app.data.mapper

import com.stafeewa.photocalorie.app.data.local.ProductDbModel
import com.stafeewa.photocalorie.app.domain.entity.Product

fun ProductDbModel.toDomain(): Product {
    return Product(
        id = this.id,
        name = this.name,
        mealType = this.mealType,
        defaultPortion = this.defaultPortion,
        proteinPer100g = this.proteinPer100g,
        fatPer100g = this.fatPer100g,
        carbsPer100g = this.carbsPer100g,
        caloriesPer100g = this.caloriesPer100g
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
        caloriesPer100g = this.caloriesPer100g
    )
}