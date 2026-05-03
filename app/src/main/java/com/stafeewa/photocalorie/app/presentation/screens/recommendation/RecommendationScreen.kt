package com.stafeewa.photocalorie.app.presentation.screens.recommendation

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.stafeewa.photocalorie.app.R
import com.stafeewa.photocalorie.app.domain.entity.MealType
import com.stafeewa.photocalorie.app.domain.entity.NutritionInfo
import com.stafeewa.photocalorie.app.domain.entity.Product
import com.stafeewa.photocalorie.app.domain.entity.Recipe
import com.stafeewa.photocalorie.app.presentation.screens.foodmain.FoodIntakeViewModel
import com.stafeewa.photocalorie.app.presentation.screens.recipes.RecipesViewModel
import com.stafeewa.photocalorie.app.presentation.screens.recipes.SubscriptionsCommand
import com.stafeewa.photocalorie.app.presentation.ui.theme.CustomIcons
import com.stafeewa.photocalorie.app.utils.toUserVisibleFoodName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(
    onNavigateBack: (() -> Unit)? = null,
    onAddProduct: (Product, MealType, Double) -> Unit,
    viewModel: RecommendationViewModel = hiltViewModel(),
    foodIntakeViewModel: FoodIntakeViewModel = hiltViewModel(),
    recipesViewModel: RecipesViewModel = hiltViewModel()
) {
    val recipeState by recipesViewModel.state.collectAsState()
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
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { recipesViewModel.processCommand(SubscriptionsCommand.RefreshData) }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.update_recipes)
                        )
                    }
                    IconButton(onClick = { recipesViewModel.processCommand(SubscriptionsCommand.ClearRecipes) }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = stringResource(R.string.clear_recipes)
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is RecommendationUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is RecommendationUiState.EmptyDiary -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    Text(
                        text = stringResource(R.string.add_food_for_recommendations),
                        modifier = Modifier.padding(start = 32.dp)
                    )
                }
            }

            is RecommendationUiState.Success -> {
                val data = (uiState as RecommendationUiState.Success).data
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
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
                    item {
                        Button(
                            onClick = { viewModel.refreshRecommendations() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(stringResource(R.string.other_recommendations))
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
                        HorizontalDivider()
                        Text(
                            stringResource(R.string.subscriptions_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    item {
                        SubscriptionsBlock(
                            subscriptions = recipeState.subscriptions,
                            query = recipeState.query,
                            isSubscribeButtonEnabled = recipeState.subscribeButtonEnable,
                            onQueryChanged = {
                                recipesViewModel.processCommand(
                                    SubscriptionsCommand.InputTopic(
                                        it
                                    )
                                )
                            },
                            onTopicClick = {
                                recipesViewModel.processCommand(
                                    SubscriptionsCommand.ToggleTopicSelection(
                                        it
                                    )
                                )
                            },
                            onDeleteSubscription = {
                                recipesViewModel.processCommand(
                                    SubscriptionsCommand.RemoveSubscription(it)
                                )
                            },
                            onSubscribeButtonClick = {
                                recipesViewModel.processCommand(
                                    SubscriptionsCommand.ClickSubscribe
                                )
                            }
                        )
                    }
                    if (recipeState.recipes.isNotEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.Recipes, recipeState.recipes.size),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(recipeState.recipes, key = { it.sourceUrl }) { RecipeCard(it) }
                    }
                    item { Spacer(modifier = Modifier.height(56.dp)) }
                }
            }

            is RecommendationUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
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

@Composable
private fun SubscriptionsBlock(
    subscriptions: Map<String, Boolean>,
    query: String,
    isSubscribeButtonEnabled: Boolean,
    onQueryChanged: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    onDeleteSubscription: (String) -> Unit,
    onSubscribeButtonClick: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = query, onValueChange = onQueryChanged,
            modifier = Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.Search)) })
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onSubscribeButtonClick, enabled = isSubscribeButtonEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, null);
            Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.add_subscription_button))
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            subscriptions.forEach { (topic, selected) ->
                item(topic) {
                    FilterChip(
                        selected = selected, onClick = { onTopicClick(topic) },
                        label = { Text(topic) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Clear, contentDescription = null,
                                modifier = Modifier.clickable { onDeleteSubscription(topic) })
                        })
                }
            }
        }
    }
}

@Composable
private fun RecipeCard(recipe: Recipe) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(recipe.title, fontWeight = FontWeight.Bold)
            AsyncImage(
                model = recipe.image,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            NutritionInfoDisplay(recipe.nutrition)

            // Кнопка "Читать"
            Button(
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, recipe.sourceUrl.toUri()))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(CustomIcons.OpenInNew, null)
                Text(stringResource(R.string.read))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Кнопка "Поделиться"
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "${recipe.title}\n\n${recipe.sourceUrl}")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share_recipe))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.share))
            }
        }
    }
}

@Composable
private fun NutritionInfoDisplay(nutrition: NutritionInfo) {
    Column {
        listOf(
            stringResource(R.string.Calories) to nutrition.calories,
            stringResource(R.string.Protein) to nutrition.protein,
            stringResource(R.string.Fat) to nutrition.fat,
            stringResource(R.string.Carbs) to nutrition.carbs
        )
            .forEach { (l, v) ->
                if (v.isNotEmpty())
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) { Text(l); Text(v) }
            }
    }
}