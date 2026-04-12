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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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

@Composable
fun FoodIntakeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,  // ← добавляем NavController как параметр
    viewModel: FoodIntakeViewModel = hiltViewModel()
) {
    val calorieGoal by viewModel.calorieGoal.collectAsStateWithLifecycle()
    val remainingCalories by viewModel.remainingCalories.collectAsStateWithLifecycle()
    val totalCalories by viewModel.totalCalories.collectAsStateWithLifecycle()
    val breakfastEntries by viewModel.breakfastEntries.collectAsStateWithLifecycle()
    val lunchEntries by viewModel.lunchEntries.collectAsStateWithLifecycle()
    val dinnerEntries by viewModel.dinnerEntries.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Получаем результат из камеры через NavController
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Bundle>("food_result")?.observeForever { bundle ->
            bundle?.let {
                val name = it.getString("name") ?: ""
                val mealTypeName = it.getString("mealType") ?: ""
                val portion = it.getDouble("portion", 0.0)
                val protein = it.getDouble("protein", 0.0)
                val fat = it.getDouble("fat", 0.0)
                val carbs = it.getDouble("carbs", 0.0)

                val mealType = try {
                    MealType.valueOf(mealTypeName)
                } catch (e: Exception) {
                    MealType.LUNCH
                }

                if (name.isNotBlank()) {
                    viewModel.addFoodEntry(name, mealType, portion, protein, fat, carbs)
                }
                navController.currentBackStackEntry?.savedStateHandle?.remove<Bundle>("food_result")
            }
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
        containerColor = Color(0xFF313131),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { contentPadding ->
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
                            text = "Калории на сегодня",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White
                            ),
                            fontFamily = FontFamily(Font(R.font.jura)),
                            fontSize = 30.sp
                        )
                    }

                    item {
                        Text(
                            text = "Ваша норма: ${calorieGoal?.toInt() ?: 0} ккал",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White
                            ),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.jura)),
                            fontSize = 24.sp
                        )
                    }

                    item {
                        // Потребленные калории
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xCC009E1D),
                                                Color.Transparent
                                            )
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
                                        color = Color.White
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily(Font(R.font.jura)),
                                    fontSize = 30.sp
                                )
                                Text(
                                    text = "Осталось: ${remainingCalories.toInt()} ккал",
                                    color = if (remainingCalories < 0) Color.Red else Color.White.copy(alpha = 0.7f),
                                    fontFamily = FontFamily(Font(R.font.jura)),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    // Завтрак
                    item {
                        MealSection(
                            title = "Завтрак",
                            mealType = MealType.BREAKFAST,
                            items = breakfastEntries,
                            onAddClick = {
                                selectedMealType = MealType.BREAKFAST
                                showAddDialog = true
                            },
                            onDeleteClick = { entry ->
                                viewModel.removeFoodEntry(entry.id)
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    // Обед
                    item {
                        MealSection(
                            title = "Обед",
                            mealType = MealType.LUNCH,
                            items = lunchEntries,
                            onAddClick = {
                                selectedMealType = MealType.LUNCH
                                showAddDialog = true
                            },
                            onDeleteClick = { entry ->
                                viewModel.removeFoodEntry(entry.id)
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    // Ужин
                    item {
                        MealSection(
                            title = "Ужин",
                            mealType = MealType.DINNER,
                            items = dinnerEntries,
                            onAddClick = {
                                selectedMealType = MealType.DINNER
                                showAddDialog = true
                            },
                            onDeleteClick = { entry ->
                                viewModel.removeFoodEntry(entry.id)
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    item {
                        Button(
                            onClick = {
                                navController.navigate("camera")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(30.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF009E1D)
                            )
                        ) {
                            Text(
                                "Распознать еду по фото",
                                fontFamily = FontFamily(Font(R.font.jura)),
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // Индикатор загрузки
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
    )

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
}

// Остальные функции (MealSection, FoodItemRow, NutrientBadge) остаются без изменений
@Composable
fun MealSection(
    title: String,
    mealType: MealType,
    items: List<FoodEntry>,
    onAddClick: () -> Unit,
    onDeleteClick: (FoodEntry) -> Unit,
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
                color = Color(0xFF474646),
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
                    color = Color.White
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
                tint = Color.White,
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
                    text = "🥩 Б: ${totalProtein.toInt()}г",
                    color = Color(0xFF4CAF50),
                    fontFamily = FontFamily(Font(R.font.jura)),
                    fontSize = 14.sp
                )
                Text(
                    text = "🧈 Ж: ${totalFat.toInt()}г",
                    color = Color(0xFFFF9800),
                    fontFamily = FontFamily(Font(R.font.jura)),
                    fontSize = 14.sp
                )
                Text(
                    text = "🍚 У: ${totalCarbs.toInt()}г",
                    color = Color(0xFF2196F3),
                    fontFamily = FontFamily(Font(R.font.jura)),
                    fontSize = 14.sp
                )
            }
        } else {
            Text(
                text = "Нет добавленных блюд",
                color = Color.White.copy(alpha = 0.5f),
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(8.dp))

            if (items.isEmpty()) {
                Text(
                    text = "Нет добавленных блюд",
                    color = Color.White.copy(alpha = 0.5f),
                    fontFamily = FontFamily(Font(R.font.jura)),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                items.forEach { item ->
                    FoodItemRow(
                        item = item,
                        onDelete = { onDeleteClick(item) }
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
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить еду",
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onAddClick() }
                )
            }
        }
    }
}

@Composable
fun FoodItemRow(
    item: FoodEntry,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(2f)) {
            Text(
                text = item.name,
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 18.sp,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White
                ),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${item.portion.toInt()} г • ${item.calories.toInt()} ккал",
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        Row(
            modifier = Modifier.weight(1.5f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NutrientBadge("Б", item.protein.toInt(), Color(0xFF4CAF50))
            NutrientBadge("Ж", item.fat.toInt(), Color(0xFFFF9800))
            NutrientBadge("У", item.carbs.toInt(), Color(0xFF2196F3))
        }

        Icon(
            painter = painterResource(id = R.drawable.bin),
            contentDescription = "Удалить",
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
            color = Color.White,
            fontFamily = FontFamily(Font(R.font.jura))
        )
    }
}