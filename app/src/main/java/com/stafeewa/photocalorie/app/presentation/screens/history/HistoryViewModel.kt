package com.stafeewa.photocalorie.app.presentation.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stafeewa.photocalorie.app.domain.entity.FoodEntry
import com.stafeewa.photocalorie.app.domain.entity.MealType
import com.stafeewa.photocalorie.app.domain.entity.NutritionStatistics
import com.stafeewa.photocalorie.app.domain.repository.FoodIntakeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val foodIntakeRepository: FoodIntakeRepository
) : ViewModel() {
    private val _selectedPeriod = MutableStateFlow(PeriodType.WEEK)
    val selectedPeriod: StateFlow<PeriodType> = _selectedPeriod.asStateFlow()

    private val _customStartDate = MutableStateFlow<LocalDate?>(null)
    private val _customEndDate = MutableStateFlow<LocalDate?>(null)

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun setPeriod(period: PeriodType) {
        _selectedPeriod.value = period
        if (period != PeriodType.CUSTOM) {
            _customStartDate.value = null
            _customEndDate.value = null
        }
        loadData()
    }

    fun setCustomDateRange(start: LocalDate, end: LocalDate) {
        _customStartDate.value = start
        _customEndDate.value = end
        _selectedPeriod.value = PeriodType.CUSTOM
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading
            val now = LocalDate.now()
            val (startDate, endDate) = when (_selectedPeriod.value) {
                PeriodType.DAY -> now to now
                PeriodType.WEEK -> now.minusWeeks(1) to now
                PeriodType.MONTH -> now.minusMonths(1) to now
                PeriodType.CUSTOM -> (_customStartDate.value ?: now.minusWeeks(1)) to (_customEndDate.value ?: now)
            }
            val startTimestamp = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endTimestamp = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            foodIntakeRepository.getEntriesBetween(startTimestamp, endTimestamp)
                .catch { e -> _uiState.value = HistoryUiState.Error(e.message ?: "Unknown error") }
                .collect { entries ->
                    val grouped = groupEntriesByDay(entries)
                    _uiState.value = HistoryUiState.Success(
                        groupedEntries = grouped,
                        statistics = calculateStatistics(entries, grouped.size),
                        dateRange = "${formatDate(startDate)} - ${formatDate(endDate)}"
                    )
                }
        }
    }

    private fun groupEntriesByDay(entries: List<FoodEntry>): List<DayEntries> {
        return entries.groupBy { it.timestamp.toLocalDate() }.map { (date, dayEntries) ->
            val mealsByType = dayEntries.groupBy { it.mealType }
            val totalsByMealType = mealsByType.mapValues { (_, mealEntries) ->
                NutritionTotals(
                    calories = mealEntries.sumOf { it.calories },
                    protein = mealEntries.sumOf { it.protein },
                    fat = mealEntries.sumOf { it.fat },
                    carbs = mealEntries.sumOf { it.carbs }
                )
            }
            DayEntries(
                date = date,
                entries = dayEntries.sortedByDescending { it.timestamp },
                mealsByType = mealsByType,
                totalsByMealType = totalsByMealType,
                totalCalories = dayEntries.sumOf { it.calories },
                totalProtein = dayEntries.sumOf { it.protein },
                totalFat = dayEntries.sumOf { it.fat },
                totalCarbs = dayEntries.sumOf { it.carbs }
            )
        }.sortedByDescending { it.date }
    }

    private fun Long.toLocalDate(): LocalDate =
        java.time.Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

    private fun calculateStatistics(entries: List<FoodEntry>, numberOfDays: Int): NutritionStatistics {
        if (entries.isEmpty() || numberOfDays == 0) return NutritionStatistics(0.0, 0.0, 0.0, 0.0, 0)
        return NutritionStatistics(
            avgCalories = entries.sumOf { it.calories } / numberOfDays,
            avgProtein = entries.sumOf { it.protein } / numberOfDays,
            avgFat = entries.sumOf { it.fat } / numberOfDays,
            avgCarbs = entries.sumOf { it.carbs } / numberOfDays,
            totalDays = numberOfDays
        )
    }

    private fun formatDate(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
}

data class NutritionTotals(val calories: Double, val protein: Double, val fat: Double, val carbs: Double)

data class DayEntries(
    val date: LocalDate,
    val entries: List<FoodEntry>,
    val mealsByType: Map<MealType, List<FoodEntry>>,
    val totalsByMealType: Map<MealType, NutritionTotals>,
    val totalCalories: Double,
    val totalProtein: Double,
    val totalFat: Double,
    val totalCarbs: Double
)

sealed class HistoryUiState {
    data object Loading : HistoryUiState()
    data class Success(val groupedEntries: List<DayEntries>, val statistics: NutritionStatistics, val dateRange: String) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

enum class PeriodType { DAY, WEEK, MONTH, CUSTOM }
