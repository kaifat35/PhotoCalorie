package com.stafeewa.photocalorie.app.presentation.screens.foodmain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stafeewa.photocalorie.app.domain.entity.FoodEntry
import com.stafeewa.photocalorie.app.domain.entity.MealType
import com.stafeewa.photocalorie.app.domain.entity.Product
import com.stafeewa.photocalorie.app.domain.repository.ProductRepository
import com.stafeewa.photocalorie.app.domain.usecase.foodintake.AddFoodEntryUseCase
import com.stafeewa.photocalorie.app.domain.usecase.foodintake.AddFoodEntryWithValidationUseCase
import com.stafeewa.photocalorie.app.domain.usecase.foodintake.GetDailyIntakeUseCase
import com.stafeewa.photocalorie.app.domain.usecase.foodintake.GetTodayEntriesUseCase
import com.stafeewa.photocalorie.app.domain.usecase.foodintake.RemoveFoodEntryUseCase
import com.stafeewa.photocalorie.app.domain.usecase.foodintake.UpdateFoodEntryUseCase
import com.stafeewa.photocalorie.app.domain.usecase.userprofile.ObserveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodIntakeViewModel @Inject constructor(
    private val observeUserProfileUseCase: ObserveUserProfileUseCase,
    private val addFoodEntryUseCase: AddFoodEntryUseCase,
    private val addFoodEntryWithValidationUseCase: AddFoodEntryWithValidationUseCase,
    private val removeFoodEntryUseCase: RemoveFoodEntryUseCase,
    private val updateFoodEntryUseCase: UpdateFoodEntryUseCase,
    private val getTodayEntriesUseCase: GetTodayEntriesUseCase,
    private val getDailyIntakeUseCase: GetDailyIntakeUseCase,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val enToRuKeywordMap = mapOf(
        "oatmeal" to "овсян",
        "porridge" to "каша",
        "buckwheat" to "греч",
        "rice" to "рис",
        "egg" to "яич",
        "omelette" to "омлет",
        "omelet" to "омлет",
        "chicken" to "кур",
        "fish" to "рыб",
        "salad" to "салат",
        "soup" to "суп",
        "borscht" to "борщ",
        "pasta" to "макарон",
        "noodle" to "лапш",
        "potato" to "карто",
        "cutlet" to "котлет",
        "pilaf" to "плов",
        "cottage cheese" to "творог",
        "yogurt" to "йогурт",
        "pancake" to "блин"
    )

    private val _calorieGoal = MutableStateFlow(2000.0)
    val calorieGoal: StateFlow<Double> = _calorieGoal.asStateFlow()

    private val _totalCalories = MutableStateFlow(0.0)
    val totalCalories: StateFlow<Double> = _totalCalories.asStateFlow()

    private val _remainingCalories = MutableStateFlow(2000.0)
    val remainingCalories: StateFlow<Double> = _remainingCalories.asStateFlow()

    private val _breakfastEntries = MutableStateFlow<List<FoodEntry>>(emptyList())
    val breakfastEntries: StateFlow<List<FoodEntry>> = _breakfastEntries.asStateFlow()

    private val _lunchEntries = MutableStateFlow<List<FoodEntry>>(emptyList())
    val lunchEntries: StateFlow<List<FoodEntry>> = _lunchEntries.asStateFlow()

    private val _dinnerEntries = MutableStateFlow<List<FoodEntry>>(emptyList())
    val dinnerEntries: StateFlow<List<FoodEntry>> = _dinnerEntries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _productSearchResults = MutableStateFlow<List<Product>>(emptyList())
    val productSearchResults: StateFlow<List<Product>> = _productSearchResults.asStateFlow()
    private var searchJob: Job? = null

    fun searchProducts(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            productRepository.searchProducts(query).collect { products ->
                _productSearchResults.value = products
            }
        }
    }

    init {
        loadData()
    }

    private fun loadData() {
        observeUserProfileUseCase()
            .onEach { profile ->
                val goal = profile.dailyCalories ?: 2000.0
                _calorieGoal.value = goal
                updateRemainingCalories()
            }
            .launchIn(viewModelScope)

        getDailyIntakeUseCase()
            .onEach { dailyIntake ->
                _totalCalories.value = dailyIntake.totalCalories
                updateRemainingCalories()
            }
            .launchIn(viewModelScope)

        getTodayEntriesUseCase()
            .onEach { entries ->
                _breakfastEntries.value = entries.filter { it.mealType == MealType.BREAKFAST }
                _lunchEntries.value = entries.filter { it.mealType == MealType.LUNCH }
                _dinnerEntries.value = entries.filter { it.mealType == MealType.DINNER }
            }
            .launchIn(viewModelScope)
    }

    private fun updateRemainingCalories() {
        _remainingCalories.value = _calorieGoal.value - _totalCalories.value
    }

    fun addFoodEntry(
        name: String,
        mealType: MealType,
        portion: Double,
        protein: Double,
        fat: Double,
        carbs: Double
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = runCatching {
                val resolvedProduct = resolveMlKitProduct(name)
                if (resolvedProduct != null && protein == 0.0 && fat == 0.0 && carbs == 0.0) {
                    val kbju = resolvedProduct.calculateKbjuForPortion(portion)
                    addFoodEntryWithValidationUseCase(
                        name = resolvedProduct.name,
                        mealType = mealType,
                        portion = portion,
                        protein = kbju.protein,
                        fat = kbju.fat,
                        carbs = kbju.carbs
                    )
                } else {
                    addFoodEntryWithValidationUseCase(
                        name = name,
                        mealType = mealType,
                        portion = portion,
                        protein = protein,
                        fat = fat,
                        carbs = carbs
                    )
                }
            }
            _isLoading.value = false

            result
                .onSuccess { response ->
                    when (response) {
                        is AddFoodEntryWithValidationUseCase.Result.Success -> {
                            _successMessage.value = "${response.entry.name} добавлен"
                            clearSuccessMessageAfterDelay()
                        }

                        is AddFoodEntryWithValidationUseCase.Result.Error -> {
                            _errorMessage.value = response.message
                            clearErrorMessageAfterDelay()
                        }
                    }
                }
                .onFailure {
                    _errorMessage.value = "Не удалось добавить блюдо. Попробуйте ещё раз."
                    clearErrorMessageAfterDelay()
                }
        }
    }

    suspend fun resolveMlKitProduct(label: String): Product? {
        if (label.isBlank()) return null

        val variants = buildSearchVariants(label)
        val candidates = variants.flatMap { query ->
            productRepository.searchProducts(query).first()
        }.distinctBy { it.id }

        if (candidates.isEmpty()) return null

        val normalizedVariants = variants.map { normalizeLabel(it) }
        return candidates.maxByOrNull { product ->
            val productTokens = listOf(product.name) + product.keywords
            val normalizedProductTokens = productTokens.map(::normalizeLabel)

            normalizedVariants.maxOf { variant ->
                normalizedProductTokens.maxOf { token ->
                    calculateMatchScore(variant, token)
                }
            }
        }
    }

    private fun buildSearchVariants(label: String): Set<String> {
        val normalized = normalizeLabel(label)
        val variants = mutableSetOf(normalized)

        enToRuKeywordMap.forEach { (en, ru) ->
            if (normalized.contains(en)) {
                variants += normalized.replace(en, ru)
                variants += ru
            }
        }

        normalized.split(" ")
            .filter { it.length >= 3 }
            .forEach { token ->
                variants += token
                enToRuKeywordMap[token]?.let { variants += it }
            }

        return variants.filter { it.isNotBlank() }.toSet()
    }

    private fun normalizeLabel(value: String): String {
        return value.lowercase()
            .replace(Regex("[^a-zа-я0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun calculateMatchScore(recognized: String, dbName: String): Int {
        return when {
            recognized == dbName -> 100
            dbName.contains(recognized) -> 80
            recognized.contains(dbName) -> 70
            recognized.split(" ").any { it.length > 2 && dbName.contains(it) } -> 50
            else -> 10
        }
    }

    fun removeFoodEntry(entryId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            removeFoodEntryUseCase(entryId)
            _isLoading.value = false
            _successMessage.value = "Запись удалена"
            clearSuccessMessageAfterDelay()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }

    private fun clearErrorMessageAfterDelay() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _errorMessage.value = null
        }
    }

    private fun clearSuccessMessageAfterDelay() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _successMessage.value = null
        }
    }
}
