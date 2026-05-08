package com.stafeewa.photocalorie.app.presentation.screens.foodmain

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.stafeewa.photocalorie.app.R
import com.stafeewa.photocalorie.app.domain.entity.FoodEntry
import com.stafeewa.photocalorie.app.domain.entity.MealType
import com.stafeewa.photocalorie.app.utils.toUserVisibleFoodName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodIntakeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: FoodIntakeViewModel = hiltViewModel()
) {
    val calorieGoal by viewModel.calorieGoal.collectAsStateWithLifecycle()
    val remainingCalories by viewModel.remainingCalories.collectAsStateWithLifecycle()
    val totalCalories by viewModel.totalCalories.collectAsStateWithLifecycle()
    val breakfastEntries by viewModel.breakfastEntries.collectAsStateWithLifecycle()
    val lunchEntries by viewModel.lunchEntries.collectAsStateWithLifecycle()
    val dinnerEntries by viewModel.dinnerEntries.collectAsStateWithLifecycle()
    val snackEntries by viewModel.snackEntries.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Состояние для диалога редактирования
    var editingEntry by remember { mutableStateOf<FoodEntry?>(null) }
    var newPortion by remember { mutableStateOf("") }

    // Получаем результат из камеры через NavController
    LaunchedEffect(navController) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect
        savedStateHandle.getStateFlow<Bundle?>("food_result", null).collect { bundle ->
            bundle ?: return@collect
            val name = bundle.getString("name") ?: ""
            val mealTypeName = bundle.getString("mealType") ?: ""
            val portion = bundle.getDouble("portion", 0.0)
            val protein = bundle.getDouble("protein", 0.0)
            val fat = bundle.getDouble("fat", 0.0)
            val carbs = bundle.getDouble("carbs", 0.0)
            val mealType = try {
                MealType.valueOf(mealTypeName)
            } catch (e: Exception) {
                MealType.LUNCH
            }
            if (name.isNotBlank()) {
                viewModel.addFoodEntry(name, mealType, portion, protein, fat, carbs)
            }
            savedStateHandle["food_result"] = null
        }
    }

    // Показываем сообщения об ошибках/успехе
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMealType by remember { mutableStateOf<MealType?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("recommendation") },
                icon = {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = stringResource(R.string.recommendations),
                        modifier = Modifier.size(24.dp)
                    )
                },
                text = { Text(stringResource(R.string.recommendations)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 80.dp, end = 16.dp)
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { contentPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(16.dp)
                    .padding(bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = stringResource(R.string.calories_for_today),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        fontFamily = FontFamily(Font(R.font.jura)),
                        fontSize = 30.sp
                    )
                }
                item {
                    Text(
                        text = stringResource(R.string.your_normal_kcal, calorieGoal?.toInt() ?: 0),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily(Font(R.font.jura)),
                        fontSize = 24.sp
                    )
                }
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0xCC009E1D), Color.Transparent)
                                    ),
                                    shape = CircleShape
                                )
                                .blur(30.dp)
                                .clip(CircleShape)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${totalCalories.toInt()} / ${calorieGoal.toInt()}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                ),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily(Font(R.font.jura)),
                                fontSize = 30.sp
                            )
                            Text(
                                text = stringResource(R.string.There_are_kcal, remainingCalories.toInt()),
                                color = if (remainingCalories < 0) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontFamily = FontFamily(Font(R.font.jura)),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                // Завтрак
                item {
                    MealSection(
                        title = stringResource(R.string.breakfast),
                        items = breakfastEntries,
                        onAddClick = {
                            selectedMealType = MealType.BREAKFAST
                            showAddDialog = true
                        },
                        onDeleteClick = { viewModel.removeFoodEntry(it.id) },
                        onEditClick = { entry ->
                            editingEntry = entry
                            newPortion = entry.portion.toInt().toString()
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }

                // Обед
                item {
                    MealSection(
                        title = stringResource(R.string.lunch),
                        items = lunchEntries,
                        onAddClick = {
                            selectedMealType = MealType.LUNCH
                            showAddDialog = true
                        },
                        onDeleteClick = { viewModel.removeFoodEntry(it.id) },
                        onEditClick = { entry ->
                            editingEntry = entry
                            newPortion = entry.portion.toInt().toString()
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }

                // Ужин
                item {
                    MealSection(
                        title = stringResource(R.string.dinner),
                        items = dinnerEntries,
                        onAddClick = {
                            selectedMealType = MealType.DINNER
                            showAddDialog = true
                        },
                        onDeleteClick = { viewModel.removeFoodEntry(it.id) },
                        onEditClick = { entry ->
                            editingEntry = entry
                            newPortion = entry.portion.toInt().toString()
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }

                // Перекус
                item {
                    MealSection(
                        title = stringResource(R.string.snack),
                        items = snackEntries,
                        onAddClick = {
                            selectedMealType = MealType.SNACK
                            showAddDialog = true
                        },
                        onDeleteClick = { viewModel.removeFoodEntry(it.id) },
                        onEditClick = { entry ->
                            editingEntry = entry
                            newPortion = entry.portion.toInt().toString()
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF009E1D))
                }
            }
        }
    }

    // Диалог добавления еды
    if (showAddDialog && selectedMealType != null) {
        AddFoodDialog(
            mealType = selectedMealType!!,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, mealType, portion, protein, fat, carbs ->
                viewModel.addFoodEntry(name, mealType, portion, protein, fat, carbs)
                showAddDialog = false
            }
        )
    }

    // Диалог редактирования порции
    if (editingEntry != null) {
        AlertDialog(
            onDismissRequest = { editingEntry = null },
            title = { Text(stringResource(R.string.edit_portion)) },
            text = {
                OutlinedTextField(
                    value = newPortion,
                    onValueChange = { newPortion = it },
                    label = { Text(stringResource(R.string.grams_short)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val portionValue = newPortion.toDoubleOrNull()
                        if (portionValue != null && portionValue > 0) {
                            viewModel.updateFoodEntry(editingEntry!!.id, portionValue)
                            editingEntry = null
                            newPortion = ""
                        }
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { editingEntry = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun MealSection(
    title: String,
    items: List<FoodEntry>,
    onAddClick: () -> Unit,
    onDeleteClick: (FoodEntry) -> Unit,
    onEditClick: (FoodEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val totalCalories = items.sumOf { it.calories }
    val totalProtein = items.sumOf { it.protein }
    val totalFat = items.sumOf { it.fat }
    val totalCarbs = items.sumOf { it.carbs }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(30.dp)
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth(Alignment.CenterHorizontally),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 24.sp
            )
            Icon(
                painter = painterResource(id = if (isExpanded) R.drawable.down else R.drawable.plus),
                contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { isExpanded = !isExpanded }
            )
        }

        if (items.isNotEmpty()) {
            Text(
                text = "🔥 ${totalCalories.toInt()} ккал",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color(0xFFFF9800)
                ),
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = stringResource(R.string.S_g, totalProtein.toInt()),
                    color = Color(0xFF4CAF50),
                    fontFamily = FontFamily(Font(R.font.jura)),
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.F_g, totalFat.toInt()),
                    color = Color(0xFFFF9800),
                    fontFamily = FontFamily(Font(R.font.jura)),
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.C_g, totalCarbs.toInt()),
                    color = Color(0xFF2196F3),
                    fontFamily = FontFamily(Font(R.font.jura)),
                    fontSize = 14.sp
                )
            }
        } else {
            Text(
                text = stringResource(R.string.There_are_no_added_dishes),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(8.dp))
            if (items.isNotEmpty()) {
                items.forEach { item ->
                    FoodItemRow(
                        item = item,
                        onDelete = { onDeleteClick(item) },
                        onEdit = { onEditClick(item) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = Color(0xFFEEEEEE)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.plus),
                    contentDescription = stringResource(R.string.add),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onAddClick() }
                )
            }
        }
    }
}

@Composable
fun FoodItemRow(
    item: FoodEntry,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(2f)) {
            Text(
                text = item.name.toUserVisibleFoodName(),
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 18.sp,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.g_ckal, item.portion.toInt(), item.calories.toInt()),
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Row(
            modifier = Modifier.weight(1.5f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NutrientBadge(stringResource(R.string.S), item.protein.toInt(), Color(0xFF4CAF50))
            NutrientBadge(stringResource(R.string.F), item.fat.toInt(), Color(0xFFFF9800))
            NutrientBadge(stringResource(R.string.C), item.carbs.toInt(), Color(0xFF2196F3))
        }

        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = stringResource(R.string.edit),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .clickable { onEdit() }
        )

        Icon(
            painter = painterResource(id = R.drawable.bin),
            contentDescription = stringResource(R.string.delete),
            tint = Color.Red.copy(alpha = 0.7f),
            modifier = Modifier
                .size(24.dp)
                .clickable { onDelete() }
        )
    }
}

@Composable
fun NutrientBadge(
    label: String,
    value: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value.toString(),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily(Font(R.font.jura))
        )
    }
}