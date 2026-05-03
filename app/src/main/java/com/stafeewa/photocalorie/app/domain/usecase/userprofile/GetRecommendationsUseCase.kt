package com.stafeewa.photocalorie.app.domain.usecase.userprofile

import com.stafeewa.photocalorie.app.domain.entity.FoodEntry
import com.stafeewa.photocalorie.app.domain.entity.MealType
import com.stafeewa.photocalorie.app.domain.entity.Product
import com.stafeewa.photocalorie.app.domain.repository.RecommendationFeedbackRepository
import com.stafeewa.photocalorie.app.domain.repository.ProductRepository
import com.stafeewa.photocalorie.app.domain.repository.UserFoodPreferences
import com.stafeewa.photocalorie.app.domain.repository.UserFoodPreferencesRepository
import java.time.LocalTime
import javax.inject.Inject
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class RecommendationItem(val product: Product, val reason: String)
data class RecommendationResult(
    val remainingCalories: Double,
    val totalCaloriesConsumed: Double,
    val proteinConsumed: Double,
    val fatConsumed: Double,
    val carbsConsumed: Double,
    val targetProtein: Double,
    val targetFat: Double,
    val targetCarbs: Double,
    val suggestedProducts: List<RecommendationItem>,
    val personalized: Boolean
)

class GetRecommendationsUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val preferencesRepository: UserFoodPreferencesRepository,
    private val feedbackRepository: RecommendationFeedbackRepository
) {
    suspend operator fun invoke(tdee: Double, entries: List<FoodEntry>, userId: Int = 1, currentHour: Int = LocalTime.now().hour, recentlyRecommendedProducts: List<String> = emptyList()): RecommendationResult {
        val consumedCalories = entries.sumOf { it.calories }
        val consumedProtein = entries.sumOf { it.protein }
        val consumedFat = entries.sumOf { it.fat }
        val consumedCarbs = entries.sumOf { it.carbs }
        val remainingCalories = tdee - consumedCalories
        val targetProtein = tdee * 0.3 / 4
        val targetFat = tdee * 0.3 / 9
        val targetCarbs = tdee * 0.4 / 4
        val remainingProtein = max(0.0, targetProtein - consumedProtein)
        val remainingFat = max(0.0, targetFat - consumedFat)
        val remainingCarbs = max(0.0, targetCarbs - consumedCarbs)

        val preferences = preferencesRepository.getPreferences(userId)
        val feedback = feedbackRepository.getFeedback(userId)
        val allProducts = productRepository.getAllProducts()
        val result = if (remainingCalories <= 50) listOf(RecommendationItem(createLightSnack(), "Лёгкий перекус при минимальном остатке калорий"))
        else selectRecommendations(allProducts, entries, remainingCalories, remainingProtein, remainingFat, remainingCarbs, currentHour, preferences, feedback, recentlyRecommendedProducts)

        return RecommendationResult(remainingCalories, consumedCalories, consumedProtein, consumedFat, consumedCarbs, targetProtein, targetFat, targetCarbs, result, preferences != null)
    }

    private fun selectRecommendations(products: List<Product>, entries: List<FoodEntry>, remainingCalories: Double, remainingProtein: Double, remainingFat: Double, remainingCarbs: Double, hour: Int, preferences: UserFoodPreferences?, feedback: List<com.stafeewa.photocalorie.app.domain.repository.RecommendationFeedback>, recentlyRecommendedProducts: List<String>): List<RecommendationItem> {
        val grouped = groupProductsByCategory(products)
        val feedbackMap = feedback.groupBy { it.productName.lowercase() }
        val scored = grouped.flatMap { (_, list) -> list }.map { product ->
            val score = scoreProduct(product, entries, remainingCalories, remainingProtein, remainingFat, remainingCarbs, hour, preferences, feedbackMap, recentlyRecommendedProducts)
            product to score
        }.filter { it.second > 0.0 }.sortedByDescending { it.second }

        val top10 = scored.take(10)
        val selectedPool = if (top10.isNotEmpty() && Random.nextDouble() < 0.2) top10.shuffled().take(6) else scored.take(6)
        val byCategoryCounter = mutableMapOf<String, Int>()
        val selected = mutableListOf<RecommendationItem>()
        for ((product, _) in selectedPool) {
            val category = detectCategory(product)
            if ((byCategoryCounter[category] ?: 0) >= 2) continue
            byCategoryCounter[category] = (byCategoryCounter[category] ?: 0) + 1
            selected += RecommendationItem(product, buildReason(product, remainingProtein, remainingFat, feedbackMap))
            if (selected.size >= 5) break
        }

        if (selected.size < 3) {
            listOf("Куриная грудка", "Овсяная каша", "Яблоко").forEach { fallback ->
                products.firstOrNull { it.name.contains(fallback, true) }?.let { selected += RecommendationItem(it, "Универсальный полезный вариант") }
            }
        }
        return selected.distinctBy { it.product.id }
    }

    private fun scoreProduct(product: Product, entries: List<FoodEntry>, remainingCalories: Double, remainingProtein: Double, remainingFat: Double, remainingCarbs: Double, hour: Int, preferences: UserFoodPreferences?, feedbackMap: Map<String, List<com.stafeewa.photocalorie.app.domain.repository.RecommendationFeedback>>, recentlyRecommendedProducts: List<String>): Double {
        val name = product.name.lowercase()
        val category = detectCategory(product).lowercase()
        if (preferences?.excludedProducts?.contains(name) == true || preferences?.excludedCategories?.contains(category) == true) return 0.0
        val factor = product.defaultPortion / 100.0
        val cals = product.caloriesPer100g * factor
        if (cals > remainingCalories * 1.25) return 0.0
        val p = product.proteinPer100g * factor
        val f = product.fatPer100g * factor
        val c = product.carbsPer100g * factor
        fun sat(x: Double, need: Double): Double = if (need <= 0.0) 0.3 else 1 - exp(-x / need)
        var score = sat(p, remainingProtein) * 0.35 + sat(f, remainingFat) * 0.25 + sat(c, remainingCarbs) * 0.25 + sat(min(remainingCalories, cals), remainingCalories) * 0.15
        score *= mealTimeBoost(product.mealType, hour)
        if (recentlyRecommendedProducts.any { it.equals(product.name, true) }) score *= 0.6
        if (preferences?.preferredProducts?.contains(name) == true || preferences?.preferredCategories?.contains(category) == true) score *= 1.2
        val fb = feedbackMap[name].orEmpty()
        if (fb.any { it.isLiked }) score *= 1.15
        if (fb.any { !it.isLiked }) score *= 0.55
        val diversity = 1.0 + diversityBonus(product, entries)
        return max(0.0, score * diversity)
    }

    private fun diversityBonus(product: Product, entries: List<FoodEntry>): Double {
        val usedNames = entries.map { it.productName.lowercase() }
        val isNameRepeat = usedNames.any { it == product.name.lowercase() }
        val sameCategory = entries.count { detectCategoryByName(it.productName) == detectCategory(product) }
        val keywordOverlap = product.keywords.count { keyword -> usedNames.any { it.contains(keyword.lowercase()) } }
        return (if (isNameRepeat) -0.2 else 0.15) - sameCategory * 0.03 - keywordOverlap * 0.02
    }

    private fun mealTimeBoost(mealType: MealType, hour: Int): Double {
        val target = when (hour) { in 5..10 -> MealType.BREAKFAST; in 11..15 -> MealType.LUNCH; in 16..20 -> MealType.DINNER; else -> MealType.SNACK }
        return if (mealType == target) 1.25 else 0.9
    }

    private fun buildReason(product: Product, remainingProtein: Double, remainingFat: Double, feedback: Map<String, List<com.stafeewa.photocalorie.app.domain.repository.RecommendationFeedback>>): String {
        val n = product.name.lowercase()
        return when {
            feedback[n].orEmpty().any { it.isLiked } -> "Вам понравилось похожее блюдо"
            product.proteinPer100g > product.fatPer100g && remainingProtein > remainingFat -> "Чтобы закрыть дефицит белка"
            else -> "Поддерживает баланс нутриентов"
        }
    }

    private fun groupProductsByCategory(products: List<Product>): Map<String, List<Product>> = products.groupBy { detectCategory(it) }
    private fun detectCategory(product: Product): String = detectCategoryByName(product.name)
    private fun detectCategoryByName(nameRaw: String): String { val name = nameRaw.lowercase(); return when {
        name.contains("каша") -> "Каши"; name.contains("суп") || name.contains("борщ") -> "Супы"; name.contains("салат") -> "Салаты"; name.contains("кур") || name.contains("говяд") || name.contains("мяс") -> "Мясные блюда"; name.contains("рыб") || name.contains("лосос") || name.contains("треск") -> "Рыбные блюда"; name.contains("компот") || name.contains("чай") || name.contains("коф") -> "Напитки"; name.contains("торт") || name.contains("десерт") || name.contains("пирож") -> "Десерты"; else -> "Прочее" } }

    private fun createLightSnack() = Product(name = "Вода с лимоном", mealType = MealType.SNACK, defaultPortion = 200.0, proteinPer100g = 0.0, fatPer100g = 0.0, carbsPer100g = 0.0, caloriesPer100g = 0.0, keywords = listOf("вода", "напиток"))
}
