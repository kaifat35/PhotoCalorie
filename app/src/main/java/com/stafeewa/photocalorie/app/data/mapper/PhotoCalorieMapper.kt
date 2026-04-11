package com.stafeewa.photocalorie.app.data.mapper


import com.stafeewa.photocalorie.app.data.local.RecipeDbModel
import com.stafeewa.photocalorie.app.data.remote.NutritionDto
import com.stafeewa.photocalorie.app.data.remote.RecipesResponseDto
import com.stafeewa.photocalorie.app.domain.entity.Interval
import com.stafeewa.photocalorie.app.domain.entity.Language
import com.stafeewa.photocalorie.app.domain.entity.NutritionInfo
import com.stafeewa.photocalorie.app.domain.entity.Recipe
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

private val json = Json { ignoreUnknownKeys = true }
fun RecipesResponseDto.toDbModel(topic: String): List<RecipeDbModel> {
    return results.map {
        val nutritionJson = json.encodeToString(it.nutrition)
        RecipeDbModel(
            title = it.title,
            id = it.id,
            image = it.image,
            nutrition = nutritionJson,
            sourceUrl = it.sourceUrl,
            topic = topic
        )
    }
}
fun List<RecipeDbModel>.toEntities(): List<Recipe> {
    return map {
        val nutritionInfo = parseNutritionFromJson(it.nutrition)
        Recipe(
            title = it.title,
            id = it.id,
            image = it.image,
            nutrition = nutritionInfo,
            sourceUrl = it.sourceUrl,
        )
    }.distinct()
}

private fun parseNutritionFromJson(nutritionJson: String): NutritionInfo {
    return try {
        val nutritionDto = json.decodeFromString<NutritionDto>(nutritionJson)

        var calories = ""
        var protein = ""
        var fat = ""
        var carbs = ""

        nutritionDto.nutrients.forEach { nutrient ->
            when (nutrient.name) {
                "Calories" -> calories = "${nutrient.amount.roundToInt()} ${nutrient.unit}"
                "Protein" -> protein = "${nutrient.amount.roundToInt()} ${nutrient.unit}"
                "Fat" -> fat = "${nutrient.amount.roundToInt()} ${nutrient.unit}"
                "Carbohydrates" -> carbs = "${nutrient.amount.roundToInt()} ${nutrient.unit}"
            }
        }

        NutritionInfo(
            calories = calories,
            protein = protein,
            fat = fat,
            carbs = carbs
        )
    } catch (e: Exception) {
        NutritionInfo.Empty
    }
}

fun Language.toQueryParam(): String {
    return when (this) {
        Language.ENGLISH -> "en"

        Language.RUSSIAN -> "ru"

        Language.FRENCH -> "fr"

        Language.GERMAN -> "de"
    }
}

fun Int.toInterval(): Interval {
    return Interval.entries.first { it.minutes == this }
}