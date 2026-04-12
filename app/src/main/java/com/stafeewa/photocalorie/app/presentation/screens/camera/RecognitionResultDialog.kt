package com.stafeewa.photocalorie.app.presentation.screens.camera

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stafeewa.photocalorie.app.R
import com.stafeewa.photocalorie.app.domain.entity.MealType
import com.stafeewa.photocalorie.app.domain.usecase.foodrecognition.RecognizeFoodUseCase


@Composable
fun RecognitionResultDialog(
    result: RecognitionResult,
    onDismiss: () -> Unit,
    onConfirm: (name: String, mealType: MealType, portion: Double, protein: Double, fat: Double, carbs: Double) -> Unit,
    onAddToDatabase: (name: String, mealType: MealType, protein: Double, fat: Double, carbs: Double, recognitionKeyword: String?) -> Unit
) {
    var selectedMealType by remember { mutableStateOf(MealType.LUNCH) }
    var portion by remember { mutableStateOf("100") }
    var isAddingToDatabase by remember { mutableStateOf(false) }

    // Поля для ручного добавления
    var manualName by remember { mutableStateOf("") }
    var manualProtein by remember { mutableStateOf("") }
    var manualFat by remember { mutableStateOf("") }
    var manualCarbs by remember { mutableStateOf("") }

    // Проверка, можно ли сохранить блюдо
    val isManualFormValid = manualName.isNotBlank() &&
            (manualProtein.toDoubleOrNull() != null ||
                    manualFat.toDoubleOrNull() != null ||
                    manualCarbs.toDoubleOrNull() != null)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (result) {
                    is RecognitionResult.Success -> "Распознано блюдо"
                    is RecognitionResult.MultipleMatches -> "Выберите блюдо"
                    is RecognitionResult.NotFound -> "Блюдо не найдено"
                    is RecognitionResult.Error -> "Ошибка"
                },
                color = Color.White,
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 24.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (result) {
                    is RecognitionResult.Success -> {
                        Text(
                            text = "${result.product.name} (${(result.confidence * 100).toInt()}%)",
                            color = Color(0xFF009E1D),
                            fontSize = 18.sp,
                            fontFamily = FontFamily(Font(R.font.jura))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            NutrientInfoDialog("Калории", "${result.product.caloriesPer100g.toInt()} ккал", Color(0xFFFF9800))
                            NutrientInfoDialog("Белки", "${result.product.proteinPer100g.toInt()} г", Color(0xFF4CAF50))
                            NutrientInfoDialog("Жиры", "${result.product.fatPer100g.toInt()} г", Color(0xFF2196F3))
                            NutrientInfoDialog("Углеводы", "${result.product.carbsPer100g.toInt()} г", Color(0xFF9C27B0))
                        }

                        MealTypeSelector(
                            selectedMealType = selectedMealType,
                            onMealTypeSelected = { selectedMealType = it }
                        )

                        OutlinedTextField(
                            value = portion,
                            onValueChange = { portion = it },
                            label = { Text("Вес порции (г)", color = Color.White.copy(alpha = 0.7f)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                        )

                        val factor = portion.toDoubleOrNull()?.div(100) ?: 1.0
                        Text(
                            text = "КБЖУ на ${portion.toIntOrNull() ?: 100} г: ${(result.product.caloriesPer100g * factor).toInt()} ккал | Б: ${(result.product.proteinPer100g * factor).toInt()}г | Ж: ${(result.product.fatPer100g * factor).toInt()}г | У: ${(result.product.carbsPer100g * factor).toInt()}г",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    is RecognitionResult.MultipleMatches -> {
                        Text(
                            text = "Найдено несколько похожих блюд:",
                            color = Color.White
                        )
                        LazyColumn(
                            modifier = Modifier.height(200.dp)
                        ) {
                            items(result.matches) { match ->
                                TextButton(
                                    onClick = {
                                        onConfirm(
                                            match.product.name,
                                            selectedMealType,
                                            portion.toDoubleOrNull() ?: 100.0,
                                            match.product.proteinPer100g * (portion.toDoubleOrNull()?.div(100) ?: 1.0),
                                            match.product.fatPer100g * (portion.toDoubleOrNull()?.div(100) ?: 1.0),
                                            match.product.carbsPer100g * (portion.toDoubleOrNull()?.div(100) ?: 1.0)
                                        )
                                        onDismiss()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            text = match.product.name,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "КБЖУ: ${match.product.caloriesPer100g.toInt()} ккал | Б:${match.product.proteinPer100g.toInt()}г Ж:${match.product.fatPer100g.toInt()}г У:${match.product.carbsPer100g.toInt()}г",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            }
                        }
                    }

                    is RecognitionResult.NotFound -> {
                        Text(
                            text = "Блюдо не найдено в базе.",
                            color = Color.White
                        )

                        if (!isAddingToDatabase) {
                            Button(
                                onClick = { isAddingToDatabase = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009E1D)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("➕ Добавить в базу")
                            }

                            // Кнопка для ручного добавления без сохранения в базу
                            Button(
                                onClick = {
                                    isAddingToDatabase = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C5A5A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("📝 Добавить вручную (без сохранения)")
                            }
                        } else {
                            Text(
                                text = "Добавьте блюдо вручную:",
                                color = Color.White
                            )

                            OutlinedTextField(
                                value = manualName.ifEmpty { result.suggestedName },
                                onValueChange = { manualName = it },
                                label = { Text("Название", color = Color.White.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                            )

                            OutlinedTextField(
                                value = manualProtein,
                                onValueChange = { manualProtein = it },
                                label = { Text("Белки на 100г", color = Color.White.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                            )

                            OutlinedTextField(
                                value = manualFat,
                                onValueChange = { manualFat = it },
                                label = { Text("Жиры на 100г", color = Color.White.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                            )

                            OutlinedTextField(
                                value = manualCarbs,
                                onValueChange = { manualCarbs = it },
                                label = { Text("Углеводы на 100г", color = Color.White.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                            )

                            // Предпросмотр КБЖУ
                            val proteinVal = manualProtein.toDoubleOrNull() ?: 0.0
                            val fatVal = manualFat.toDoubleOrNull() ?: 0.0
                            val carbsVal = manualCarbs.toDoubleOrNull() ?: 0.0
                            val calories = proteinVal * 4 + fatVal * 9 + carbsVal * 4

                            if (manualName.isNotBlank() && (proteinVal > 0 || fatVal > 0 || carbsVal > 0)) {
                                Text(
                                    text = "КБЖУ на 100г: ${calories.toInt()} ккал | Б: ${proteinVal.toInt()}г | Ж: ${fatVal.toInt()}г | У: ${carbsVal.toInt()}г",
                                    fontSize = 12.sp,
                                    color = Color(0xFF009E1D)
                                )
                            }
                        }
                    }

                    is RecognitionResult.Error -> {
                        Text(
                            text = result.message,
                            color = Color.Red
                        )
                    }
                }
            }
        },
        confirmButton = {
            when (result) {
                is RecognitionResult.Success -> {
                    Button(
                        onClick = {
                            val factor = portion.toDoubleOrNull()?.div(100) ?: 1.0
                            onConfirm(
                                result.product.name,
                                selectedMealType,
                                portion.toDoubleOrNull() ?: 100.0,
                                result.product.proteinPer100g * factor,
                                result.product.fatPer100g * factor,
                                result.product.carbsPer100g * factor
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009E1D))
                    ) {
                        Text("Добавить", color = Color.White)
                    }
                }
                is RecognitionResult.NotFound -> {
                    if (isAddingToDatabase) {
                        Button(
                            onClick = {
                                val protein = manualProtein.toDoubleOrNull() ?: 0.0
                                val fat = manualFat.toDoubleOrNull() ?: 0.0
                                val carbs = manualCarbs.toDoubleOrNull() ?: 0.0
                                val factor = portion.toDoubleOrNull()?.div(100) ?: 1.0

                                // Добавляем в базу
                                onAddToDatabase(
                                    manualName,
                                    selectedMealType,
                                    protein,
                                    fat,
                                    carbs,
                                    result.suggestedName
                                )
                                // Добавляем в дневник
                                onConfirm(
                                    manualName,
                                    selectedMealType,
                                    portion.toDoubleOrNull() ?: 100.0,
                                    protein * factor,
                                    fat * factor,
                                    carbs * factor
                                )
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009E1D)),
                            enabled = isManualFormValid
                        ) {
                            Text("💾 Сохранить и добавить", color = Color.White)
                        }
                    }
                }
                is RecognitionResult.MultipleMatches -> {
                    // Кнопка не нужна
                }
                is RecognitionResult.Error -> {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF474646))
                    ) {
                        Text("Закрыть", color = Color.White)
                    }
                }
            }
        },
        dismissButton = {
            if (result !is RecognitionResult.MultipleMatches) {
                TextButton(onClick = onDismiss) {
                    Text("Отмена", color = Color.White.copy(alpha = 0.7f))
                }
            }
        },
        containerColor = Color(0xFF474646),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

// Остальные функции остаются без изменений
@Composable
fun MealTypeSelector(
    selectedMealType: MealType,
    onMealTypeSelected: (MealType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MealTypeButton(
            title = "Завтрак",
            isSelected = selectedMealType == MealType.BREAKFAST,
            onClick = { onMealTypeSelected(MealType.BREAKFAST) }
        )
        MealTypeButton(
            title = "Обед",
            isSelected = selectedMealType == MealType.LUNCH,
            onClick = { onMealTypeSelected(MealType.LUNCH) }
        )
        MealTypeButton(
            title = "Ужин",
            isSelected = selectedMealType == MealType.DINNER,
            onClick = { onMealTypeSelected(MealType.DINNER) }
        )
    }
}

@Composable
fun MealTypeButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF009E1D) else Color(0xFF5C5A5A)
        ),
        modifier = Modifier
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

@Composable
fun NutrientInfoDialog(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = color)
        Text(text = value, fontSize = 13.sp, color = Color.White)
    }
}
