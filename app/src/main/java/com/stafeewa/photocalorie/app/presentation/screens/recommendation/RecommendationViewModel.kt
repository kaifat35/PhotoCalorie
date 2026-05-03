package com.stafeewa.photocalorie.app.presentation.screens.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stafeewa.photocalorie.app.domain.entity.FoodEntry
import com.stafeewa.photocalorie.app.domain.repository.RecommendationFeedbackRepository
import com.stafeewa.photocalorie.app.domain.usecase.recommendation.GetRecommendationsUseCase
import com.stafeewa.photocalorie.app.domain.usecase.recommendation.RecommendationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val getRecommendationsUseCase: GetRecommendationsUseCase,
    private val feedbackRepository: RecommendationFeedbackRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<RecommendationUiState>(RecommendationUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private var lastTdee: Double? = null
    private var lastEntries: List<FoodEntry>? = null
    private val recentlyRecommendedProducts = ArrayDeque<String>()

    fun refreshRecommendations() { lastTdee?.let { t -> lastEntries?.let { e -> loadRecommendations(t, e) } } }
    fun setEmptyDiaryState() { _uiState.value = RecommendationUiState.EmptyDiary }

    fun loadRecommendations(tdee: Double, entries: List<FoodEntry>) {
        lastTdee = tdee; lastEntries = entries
        viewModelScope.launch {
            _uiState.value = RecommendationUiState.Loading
            runCatching {
                getRecommendationsUseCase(tdee = tdee, entries = entries, currentHour = LocalTime.now().hour, recentlyRecommendedProducts = recentlyRecommendedProducts.toList())
            }.onSuccess { result ->
                result.suggestedProducts.forEach { pushRecent(it.product.name) }
                _uiState.value = RecommendationUiState.Success(result)
            }.onFailure { _uiState.value = RecommendationUiState.Error(it.message ?: "Ошибка") }
        }
    }

    fun sendFeedback(productName: String, isLiked: Boolean) {
        viewModelScope.launch {
            feedbackRepository.addFeedback(userId = 1, productName = productName, isLiked = isLiked)
            if (!isLiked) pushRecent(productName)
        }
    }

    private fun pushRecent(name: String) {
        if (recentlyRecommendedProducts.contains(name)) recentlyRecommendedProducts.remove(name)
        recentlyRecommendedProducts.addFirst(name)
        while (recentlyRecommendedProducts.size > 10) recentlyRecommendedProducts.removeLast()
    }
}

sealed class RecommendationUiState {
    object Loading : RecommendationUiState()
    object EmptyDiary : RecommendationUiState()
    data class Success(val data: RecommendationResult) : RecommendationUiState()
    data class Error(val message: String) : RecommendationUiState()
}
