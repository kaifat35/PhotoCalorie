package com.stafeewa.photocalorie.app.domain.usecase.userprofile

import com.stafeewa.photocalorie.app.domain.entity.FoodEntry
import com.stafeewa.photocalorie.app.domain.entity.Product
import com.stafeewa.photocalorie.app.domain.repository.ProductRepository
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class RecommendationResult(
    val remainingCalories: Double,
    val totalCaloriesConsumed: Double,
    val proteinConsumed: Double,
    val fatConsumed: Double,
    val carbsConsumed: Double,
    val targetProtein: Double,
    val targetFat: Double,
    val targetCarbs: Double,
    val suggestedProducts: List<Product>
)

class GetRecommendationsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    // Временное хранилище для исключения повторов подряд
    private val lastShownCategories = mutableListOf<String>()

    suspend operator fun invoke(tdee: Double, entries: List<FoodEntry>): RecommendationResult {
        // 1. Расчёт потреблённых нутриентов
        val consumedCalories = entries.sumOf { it.calories }
        val consumedProtein = entries.sumOf { it.protein }
        val consumedFat = entries.sumOf { it.fat }
        val consumedCarbs = entries.sumOf { it.carbs }

        val remainingCalories = tdee - consumedCalories

        // 2. Целевые макронутриенты (30% белок, 30% жир, 40% углеводы – можно настраивать)
        val targetProtein = tdee * 0.3 / 4
        val targetFat = tdee * 0.3 / 9
        val targetCarbs = tdee * 0.4 / 4

        val remainingProtein = max(0.0, targetProtein - consumedProtein)
        val remainingFat = max(0.0, targetFat - consumedFat)
        val remainingCarbs = max(0.0, targetCarbs - consumedCarbs)

        val suggestedProducts = if (remainingCalories <= 50) {
            // Если калорий почти не осталось – только лёгкие перекусы
            listOf(createLightSnack())
        } else {
            // 3. Получаем все продукты из базы
            val allProducts = productRepository.getAllProducts() // Добавить этот метод в ProductRepository
            // 4. Группировка по категориям
            val grouped = groupProductsByCategory(allProducts)
            // 5. Выбор кандидатов с учётом остатков и случайности
            selectDiversifiedRecommendations(
                grouped,
                remainingCalories,
                remainingProtein,
                remainingFat,
                remainingCarbs
            )
        }

        return RecommendationResult(
            remainingCalories = remainingCalories,
            totalCaloriesConsumed = consumedCalories,
            proteinConsumed = consumedProtein,
            fatConsumed = consumedFat,
            carbsConsumed = consumedCarbs,
            targetProtein = targetProtein,
            targetFat = targetFat,
            targetCarbs = targetCarbs,
            suggestedProducts = suggestedProducts
        )
    }

    private fun groupProductsByCategory(products: List<Product>): Map<String, List<Product>> {
        return products.groupBy { product ->
            // Простая группировка по первому слову или специальному ключу
            val name = product.name.lowercase()
            when {
                name.contains("борщ") -> "Борщ"
                name.contains("гречк") || name.contains("греча") -> "Гречка"
                name.contains("яичница") || name.contains("омлет") -> "Яйца"
                name.contains("каша") -> "Каша"
                name.contains("суп") -> "Суп"
                name.contains("салат") -> "Салат"
                name.contains("пицца") -> "Пицца"
                name.contains("картофель") || name.contains("картошка") -> "Картофель"
                name.contains("рыба") -> "Рыба"
                name.contains("мясо") || name.contains("говядина") || name.contains("курица") -> "Мясо"
                name.contains("творог") -> "Творог"
                name.contains("йогурт") -> "Йогурт"
                name.contains("фрукт") || name.contains("яблоко") || name.contains("банан") -> "Фрукты"
                else -> "Прочее"
            }
        }
    }

    private fun selectDiversifiedRecommendations(
        grouped: Map<String, List<Product>>,
        remainingCalories: Double,
        remainingProtein: Double,
        remainingFat: Double,
        remainingCarbs: Double
    ): List<Product> {
        val candidates = mutableListOf<Product>()

        // Для каждой категории выбираем один продукт, который лучше всего закрывает дефициты
        grouped.forEach { (category, products) ->
            if (category in lastShownCategories) return@forEach // избегаем повторов подряд
            val bestInCategory = products.maxByOrNull { product ->
                scoreProduct(product, remainingCalories, remainingProtein, remainingFat, remainingCarbs)
            }
            if (bestInCategory != null) candidates.add(bestInCategory)
        }

        // Если кандидатов слишком мало, добавляем из уже показанных категорий
        if (candidates.size < 4) {
            grouped.forEach { (category, products) ->
                if (category !in lastShownCategories && candidates.size < 6) {
                    val secondBest = products.maxByOrNull { product ->
                        scoreProduct(product, remainingCalories, remainingProtein, remainingFat, remainingCarbs)
                    }
                    if (secondBest != null && !candidates.contains(secondBest)) candidates.add(secondBest)
                }
            }
        }

        // Сортировка по полезности и добавление случайного шума
        val shuffled = candidates.shuffled(Random(System.currentTimeMillis()))
        val scored = shuffled.map { product ->
            val score = scoreProduct(product, remainingCalories, remainingProtein, remainingFat, remainingCarbs)
            // Шум +-20%
            val noisy = score * (0.8 + Random.nextDouble() * 0.4)
            product to noisy
        }.sortedByDescending { it.second }.take(5).map { it.first }

        // Запоминаем показанные категории для следующего вызова
        lastShownCategories.clear()
        lastShownCategories.addAll(scored.map { product ->
            groupProductsByCategory(listOf(product)).keys.first()
        })

        return scored
    }

    private fun scoreProduct(
        product: Product,
        remainingCalories: Double,
        remainingProtein: Double,
        remainingFat: Double,
        remainingCarbs: Double
    ): Double {
        // Если порция 100г, то за раз человек съест примерно defaultPortion
        val portion = product.defaultPortion
        val productCalories = product.caloriesPer100g * (portion / 100.0)
        if (productCalories > remainingCalories * 1.2) return 0.0 // слишком калорийно

        val productProtein = product.proteinPer100g * (portion / 100.0)
        val productFat = product.fatPer100g * (portion / 100.0)
        val productCarbs = product.carbsPer100g * (portion / 100.0)

        // Чем лучше покрывает дефициты, тем выше оценка
        val proteinScore = if (remainingProtein > 0) min(1.0, productProtein / remainingProtein) else 0.0
        val fatScore = if (remainingFat > 0) min(1.0, productFat / remainingFat) else 0.0
        val carbsScore = if (remainingCarbs > 0) min(1.0, productCarbs / remainingCarbs) else 0.0

        // Баланс: не перекрывать один макроэлемент слишком сильно
        val balance = 1.0 - abs(proteinScore - carbsScore) / 2.0

        return (proteinScore + fatScore + carbsScore) * balance / 3.0
    }

    private fun createLightSnack(): Product {
        return Product(
            name = "Вода с лимоном",
            mealType = com.stafeewa.photocalorie.app.domain.entity.MealType.SNACK,
            defaultPortion = 200.0,
            proteinPer100g = 0.0,
            fatPer100g = 0.0,
            carbsPer100g = 0.0,
            caloriesPer100g = 0.0,
            keywords = listOf("вода", "напиток")
        )
    }
}