package com.stafeewa.photocalorie.app.presentation.screens.foodmain

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.stafeewa.photocalorie.app.R
import com.stafeewa.photocalorie.app.domain.entity.FoodEntry
import com.stafeewa.photocalorie.app.domain.entity.MealType


@Composable
fun FoodIntakeScreen(
    modifier: Modifier = Modifier,
    viewModel: FoodIntakeViewModel = hiltViewModel()
) {

    val dailyCalories by viewModel.dailyCalories.collectAsStateWithLifecycle()


    Scaffold(
        modifier = Modifier.fillMaxSize()
            .padding(bottom = 80.dp),
        containerColor = Color(0xFF313131),// Темно-серый фон
        content = { contentPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    // Заголовок
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
                    // Норма калорий
                    Text(
                        text = "Ваша норма: ${dailyCalories?.toInt() ?: 0}",
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
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Размытый зеленый круг (фон)
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xCC009E1D),   // Полупрозрачный зеленый
                                            Color.Transparent    // Края в ноль
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .blur(30.dp) // сильное размытие
                                .clip(CircleShape)
                        )

                        // Текст поверх круга
                        Text(
                            text = "1903 ККал",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White
                            ),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.jura)),
                            fontSize = 30.sp
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    MealSection(
                        "Завтрак", listOf(
                            FoodEntry(
                                name = "молоко",
                                protein = 12.0,
                                fat = 6.0,
                                carbs = 8.0,
                                id = 1,
                                mealType = MealType.BREAKFAST,
                                portion = 1.0,
                                timestamp = System.currentTimeMillis(),
                            ),
                            FoodEntry(
                                name = "хлопья",
                                protein = 8.0,
                                fat = 12.0,
                                carbs = 30.0,
                                id = 2,
                                mealType = MealType.LUNCH,
                                portion = 1.0,
                                timestamp = System.currentTimeMillis(),
                            )
                        ), total = "250к 20г 18г 38г"
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    MealSection("Обед", emptyList(), total = "250к 20г 18г 38г")
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    MealSection("Ужин", emptyList(), total = "250к 20г 18г 38г")
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    // Кнопка распознавания еды
                    Button(
                        onClick = { },
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
        }
    )
}

@Composable
fun MealSection(
    title: String,
    items: List<FoodEntry>,
    total: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

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
                painter = painterResource(id = if (isExpanded) R.drawable.plus else R.drawable.down),
                contentDescription = if (isExpanded) "Добавить" else "Свернуть",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { isExpanded = !isExpanded }
            )
        }

        // Итого (всегда видно)
        Text(
            text = "КБЖУ: $total",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color.White
            ),
            fontFamily = FontFamily(Font(R.font.jura)),
            fontSize = 24.sp
        )

        // Список продуктов (только в развернутом состоянии)
        if (isExpanded) {
            items.forEach { item ->
                FoodItemRow(item)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = Color(0xFFEEEEEE)
                )
            }

            // Кнопка добавления (только в развернутом состоянии)
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Добавить еду",
                tint = Color.White,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { /* Логика добавления еды */ }
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun FoodItemRow(item: FoodEntry) {
    Column {
        // Название продукта
        Text(
            text = item.name,
            fontFamily = FontFamily(Font(R.font.jura)),
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color.White // // Белый цвет текста
            )
        )

        // Детали КБЖУ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.calories.toString(),
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 24.sp,
                color = Color.White
            )
            Text(
                text = item.protein.toString(),
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 24.sp,
                color = Color.White
            )
            Text(
                text = item.fat.toString(),
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 24.sp,
                color = Color.White
            )
            Text(
                text = item.carbs.toString(),
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 24.sp,
                color = Color.White
            )
        }
    }
}







