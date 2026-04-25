package com.stafeewa.photocalorie.app.presentation.screens.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stafeewa.photocalorie.app.domain.entity.FoodEntry
import com.stafeewa.photocalorie.app.domain.repository.ProductRepository
import com.stafeewa.photocalorie.app.domain.usecase.userprofile.GetRecommendationsUseCase
import com.stafeewa.photocalorie.app.domain.usecase.userprofile.RecommendationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val getRecommendationsUseCase: GetRecommendationsUseCase,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecommendationUiState>(RecommendationUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadRecommendations(tdee: Double, entries: List<FoodEntry>) {
        viewModelScope.launch {
            _uiState.value = RecommendationUiState.Loading
            try {
                val result = getRecommendationsUseCase(tdee, entries)
                _uiState.value = RecommendationUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = RecommendationUiState.Error(e.message ?: "Ошибка")
            }
        }
    }
}

sealed class RecommendationUiState {
    object Loading : RecommendationUiState()
    data class Success(val data: RecommendationResult) : RecommendationUiState()
    data class Error(val message: String) : RecommendationUiState()
}