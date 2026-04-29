package com.stafeewa.photocalorie.app.presentation.screens.recommendation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.stafeewa.photocalorie.app.R
import com.stafeewa.photocalorie.app.domain.entity.MealType
import com.stafeewa.photocalorie.app.domain.entity.Product
import com.stafeewa.photocalorie.app.presentation.screens.foodmain.FoodIntakeViewModel
import com.stafeewa.photocalorie.app.utils.toUserVisibleFoodName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(
    onNavigateBack: () -> Unit,
    onAddProduct: (Product, MealType, Double) -> Unit,
    viewModel: RecommendationViewModel = hiltViewModel(),
    foodIntakeViewModel: FoodIntakeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tdee by foodIntakeViewModel.calorieGoal.collectAsState()
    val entries by foodIntakeViewModel.allEntries.collectAsState()

    LaunchedEffect(tdee, entries) {
        when {
            tdee == null -> Unit
            entries.isEmpty() -> viewModel.setEmptyDiaryState()
            else -> viewModel.loadRecommendations(tdee!!, entries)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recommendations)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is RecommendationUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is RecommendationUiState.EmptyDiary -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                ) {
                    Text(text = stringResource(R.string.add_food_for_recommendations),
                        modifier = Modifier.padding(start = 32.dp)
                    )
                }
            }
            is RecommendationUiState.Success -> {
                val data = (uiState as RecommendationUiState.Success).data
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${stringResource(R.string.remaining_calories)}: ${data.remainingCalories.toInt()}",
                                    fontWeight = FontWeight.Bold
                                )
                                LinearProgressIndicator(
                                    progress = (data.totalCaloriesConsumed / (data.totalCaloriesConsumed + data.remainingCalories)).toFloat(),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    stringResource(
                                        R.string.nutrient_progress_proteins,
                                        data.proteinConsumed.toInt(),
                                        data.targetProtein.toInt()
                                    )
                                )
                                Text(
                                    stringResource(
                                        R.string.nutrient_progress_fats,
                                        data.fatConsumed.toInt(),
                                        data.targetFat.toInt()
                                    )
                                )
                                Text(
                                    stringResource(
                                        R.string.nutrient_progress_carbs,
                                        data.carbsConsumed.toInt(),
                                        data.targetCarbs.toInt()
                                    )
                                )
                            }
                        }
                    }

                    if (data.suggestedProducts.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.recommended_dishes),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        items(data.suggestedProducts) { product ->
                            RecommendationProductCard(
                                product = product,
                                onAdd = { mealType, portion ->
                                    onAddProduct(product, mealType, portion)
                                }
                            )
                        }
                    } else {
                        item {
                            Text(stringResource(R.string.no_recommendations_available))
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(56.dp))
                    }
                }
            }
            is RecommendationUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(
                            R.string.recommendations_error,
                            (uiState as RecommendationUiState.Error).message
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

        }
    }
}

@Composable
fun RecommendationProductCard(
    product: Product,
    onAdd: (MealType, Double) -> Unit
) {
    var selectedMealType by remember { mutableStateOf(MealType.LUNCH) }
    var portion by remember { mutableStateOf(product.defaultPortion) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = product.name.toUserVisibleFoodName(),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.calories_per_100g,
                        product.caloriesPer100g.toInt()
                    ),
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = portion.toInt().toString(),
                    onValueChange = { portion = it.toDoubleOrNull() ?: 100.0 },
                    label = { Text(stringResource(R.string.grams_short)) },
                    modifier = Modifier.width(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            MealTypeSelector(
                selectedMealType = selectedMealType,
                onMealTypeSelected = { selectedMealType = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onAdd(selectedMealType, portion) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.add))
            }
        }
    }
}

@Composable
fun MealTypeSelector(
    selectedMealType: MealType,
    onMealTypeSelected: (MealType) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MealTypeButton(
                title = stringResource(R.string.breakfast),
                isSelected = selectedMealType == MealType.BREAKFAST,
                onClick = { onMealTypeSelected(MealType.BREAKFAST) },
                modifier = Modifier.weight(1f)
            )
            MealTypeButton(
                title = stringResource(R.string.lunch),
                isSelected = selectedMealType == MealType.LUNCH,
                onClick = { onMealTypeSelected(MealType.LUNCH) },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MealTypeButton(
                title = stringResource(R.string.dinner),
                isSelected = selectedMealType == MealType.DINNER,
                onClick = { onMealTypeSelected(MealType.DINNER) },
                modifier = Modifier.weight(1f)
            )
            MealTypeButton(
                title = stringResource(R.string.snack),
                isSelected = selectedMealType == MealType.SNACK,
                onClick = { onMealTypeSelected(MealType.SNACK) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MealTypeButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}