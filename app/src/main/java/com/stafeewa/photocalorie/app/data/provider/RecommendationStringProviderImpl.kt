package com.stafeewa.photocalorie.app.data.provider

import android.content.Context
import com.stafeewa.photocalorie.app.R
import com.stafeewa.photocalorie.app.domain.entity.MealType
import com.stafeewa.photocalorie.app.domain.provider.RecommendationStringProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendationStringProviderImpl @Inject constructor(
    private val context: Context
) : RecommendationStringProvider {

    override fun getLightSnackName(): String = context.getString(R.string.light_snack_name)
    override fun getLightSnackReason(): String = context.getString(R.string.light_snack_reason)
    override fun getProteinDeficitReason(): String = context.getString(R.string.reason_protein_deficit)
    override fun getBalanceReason(): String = context.getString(R.string.reason_balance)
    override fun getLikedReason(): String = context.getString(R.string.reason_liked)
    override fun getUniversalReason(): String = context.getString(R.string.reason_universal)

    override fun getFallbackProductNames(): List<String> {
        val raw = context.getString(R.string.fallback_products_list)
        return raw.split(",").map { it.trim() }
    }

    override fun getCategoryName(category: String): String {
        return when (category) {
            "Каши" -> context.getString(R.string.category_porridges)
            "Супы" -> context.getString(R.string.category_soups)
            "Салаты" -> context.getString(R.string.category_salads)
            "Мясные блюда" -> context.getString(R.string.category_meat_dishes)
            "Рыбные блюда" -> context.getString(R.string.category_fish_dishes)
            "Напитки" -> context.getString(R.string.category_drinks)
            "Десерты" -> context.getString(R.string.category_desserts)
            else -> context.getString(R.string.category_other)
        }
    }

    override fun getMealTypeName(mealType: MealType): String {
        return when (mealType) {
            MealType.BREAKFAST -> context.getString(R.string.meal_breakfast)
            MealType.LUNCH -> context.getString(R.string.meal_lunch)
            MealType.DINNER -> context.getString(R.string.meal_dinner)
            MealType.SNACK -> context.getString(R.string.meal_snack)
        }
    }
}