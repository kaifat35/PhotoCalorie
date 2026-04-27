package com.stafeewa.photocalorie.app.presentation.screens.camera

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stafeewa.photocalorie.app.R
import com.stafeewa.photocalorie.app.domain.entity.MealType
import com.stafeewa.photocalorie.app.domain.entity.Product
import com.stafeewa.photocalorie.app.domain.repository.ProductRepository
import kotlinx.coroutines.flow.first
import com.stafeewa.photocalorie.app.utils.toUserVisibleFoodName

private enum class ManualAddMode {
    SAVE_AND_ADD,
    ADD_ONLY
}

@Composable
fun RecognitionResultDialog(
    result: RecognitionResult,
    productRepository: ProductRepository,
    onDismiss: () -> Unit,
    onConfirm: (name: String, mealType: MealType, portion: Double, protein: Double, fat: Double, carbs: Double) -> Unit,
    onAddToDatabase: (name: String, mealType: MealType, protein: Double, fat: Double, carbs: Double) -> Unit,
    bitmap: Bitmap? = null
) {
    var selectedMealType by remember { mutableStateOf(MealType.LUNCH) }
    var portion by remember { mutableStateOf("100") }
    var isAddingToDatabase by remember { mutableStateOf(false) }
    var manualAddMode by remember { mutableStateOf(ManualAddMode.SAVE_AND_ADD) }

    var manualName by remember { mutableStateOf("") }
    var manualProtein by remember { mutableStateOf("") }
    var manualFat by remember { mutableStateOf("") }
    var manualCarbs by remember { mutableStateOf("") }

    // Состояние для диалога поиска по локальной БД
    var showLocalSearchDialog by remember { mutableStateOf(false) }
    var localSearchQuery by remember { mutableStateOf("") }
    var localSearchResults by remember { mutableStateOf<List<Product>>(emptyList()) }

    // Загрузка результатов поиска при изменении запроса
    LaunchedEffect(localSearchQuery) {
        if (localSearchQuery.isNotBlank()) {
            val results = productRepository.searchProducts(localSearchQuery).first()
            localSearchResults = results
        } else {
            localSearchResults = emptyList()
        }
    }

    val isManualFormValid = manualName.isNotBlank() &&
            (manualProtein.toDoubleOrNull() != null ||
                    manualFat.toDoubleOrNull() != null ||
                    manualCarbs.toDoubleOrNull() != null)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (result) {
                    is RecognitionResult.Success -> stringResource(R.string.dish_recognized)
                    is RecognitionResult.MultipleMatches -> stringResource(R.string.choose_dish)
                    is RecognitionResult.NotFound -> stringResource(R.string.dish_not_found)
                    is RecognitionResult.LowConfidence -> stringResource(R.string.low_recognition_confidence)
                    is RecognitionResult.Error -> stringResource(R.string.error)
                },
                color = MaterialTheme.colorScheme.onSurface,
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
                            text = "${result.product.name.toUserVisibleFoodName()} (${(result.confidence * 100).toInt()}%)",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp,
                            fontFamily = FontFamily(Font(R.font.jura))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            NutrientInfoDialog(stringResource(R.string.calories), "${result.product.caloriesPer100g.toInt()} ${stringResource(R.string.kcal_short)}", MaterialTheme.colorScheme.tertiary)
                            NutrientInfoDialog(stringResource(R.string.proteins), "${result.product.proteinPer100g.toInt()} ${stringResource(R.string.grams_short)}", MaterialTheme.colorScheme.secondary)
                            NutrientInfoDialog(stringResource(R.string.fats), "${result.product.fatPer100g.toInt()} ${stringResource(R.string.grams_short)}", MaterialTheme.colorScheme.error)
                            NutrientInfoDialog(stringResource(R.string.carbohydrates), "${result.product.carbsPer100g.toInt()} ${stringResource(R.string.grams_short)}", Color(0xFF9C27B0))
                        }

                        MealTypeSelector(
                            selectedMealType = selectedMealType,
                            onMealTypeSelected = { selectedMealType = it }
                        )

                        OutlinedTextField(
                            value = portion,
                            onValueChange = { portion = it },
                            label = { Text(stringResource(R.string.portion_weight_grams), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                        )

                        val factor = portion.toDoubleOrNull()?.div(100) ?: 1.0
                        Text(
                            text = stringResource(
                                R.string.kbju_inline_format,
                                portion.toIntOrNull() ?: 100,
                                (result.product.caloriesPer100g * factor).toInt(),
                                (result.product.proteinPer100g * factor).toInt(),
                                (result.product.fatPer100g * factor).toInt(),
                                (result.product.carbsPer100g * factor).toInt()
                            ),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    is RecognitionResult.MultipleMatches -> {
                        Text(
                            text = stringResource(R.string.similar_dishes_found),
                            color = MaterialTheme.colorScheme.onSurface
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
                                            text = match.product.name.toUserVisibleFoodName(),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = stringResource(
                                                R.string.kbju_inline_per_100g,
                                                match.product.caloriesPer100g.toInt(),
                                                match.product.proteinPer100g.toInt(),
                                                match.product.fatPer100g.toInt(),
                                                match.product.carbsPer100g.toInt()
                                            ),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                            }
                        }
                    }

                    is RecognitionResult.NotFound -> {
                        Text(
                            text = stringResource(R.string.dish_not_found_in_database, result.suggestedName.toUserVisibleFoodName()),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (!isAddingToDatabase) {
                            Button(
                                onClick = {
                                    isAddingToDatabase = true
                                    manualAddMode = ManualAddMode.SAVE_AND_ADD
                                    manualName = result.suggestedName.toUserVisibleFoodName()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.add_to_database), color = MaterialTheme.colorScheme.onPrimary)
                            }

                            Button(
                                onClick = {
                                    isAddingToDatabase = true
                                    manualAddMode = ManualAddMode.ADD_ONLY
                                    manualName = result.suggestedName.toUserVisibleFoodName()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.add_manually_without_saving), color = MaterialTheme.colorScheme.onSurface)
                            }
                        } else {
                            // (ручной ввод)
                            Text(
                                text = stringResource(R.string.add_dish_manually_label),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            MealTypeSelector(
                                selectedMealType = selectedMealType,
                                onMealTypeSelected = { selectedMealType = it }
                            )

                            OutlinedTextField(
                                value = manualName,
                                onValueChange = { manualName = it },
                                label = { Text(stringResource(R.string.dish_name), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                            )

                            OutlinedTextField(
                                value = manualProtein,
                                onValueChange = { manualProtein = it },
                                label = { Text(stringResource(R.string.proteins_per_100g), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                            )

                            OutlinedTextField(
                                value = manualFat,
                                onValueChange = { manualFat = it },
                                label = { Text(stringResource(R.string.fats_per_100g), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                            )

                            OutlinedTextField(
                                value = manualCarbs,
                                onValueChange = { manualCarbs = it },
                                label = { Text(stringResource(R.string.carbs_per_100g), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                            )

                            val proteinVal = manualProtein.toDoubleOrNull() ?: 0.0
                            val fatVal = manualFat.toDoubleOrNull() ?: 0.0
                            val carbsVal = manualCarbs.toDoubleOrNull() ?: 0.0
                            val calories = proteinVal * 4 + fatVal * 9 + carbsVal * 4

                            if (manualName.isNotBlank() && (proteinVal > 0 || fatVal > 0 || carbsVal > 0)) {
                                Text(
                                    text = stringResource(
                                        R.string.kbju_manual_per_100g,
                                        calories.toInt(),
                                        proteinVal.toInt(),
                                        fatVal.toInt(),
                                        carbsVal.toInt()
                                    ),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    is RecognitionResult.LowConfidence -> {
                        Text(
                            text = stringResource(R.string.low_confidence_description),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isAddingToDatabase = true
                                    manualAddMode = ManualAddMode.ADD_ONLY
                                    manualName = result.suggestedName.toUserVisibleFoodName()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text(stringResource(R.string.enter_manually))
                            }

                            Button(
                                onClick = { showLocalSearchDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(stringResource(R.string.select_from_database))
                            }
                        }

                        if (isAddingToDatabase) {
                            // Ручной ввод (как в NotFound)
                            Text(
                                text = stringResource(R.string.add_dish_manually_label),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            MealTypeSelector(
                                selectedMealType = selectedMealType,
                                onMealTypeSelected = { selectedMealType = it }
                            )

                            OutlinedTextField(
                                value = manualName,
                                onValueChange = { manualName = it },
                                label = { Text(stringResource(R.string.dish_name), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                            )

                            OutlinedTextField(
                                value = manualProtein,
                                onValueChange = { manualProtein = it },
                                label = { Text(stringResource(R.string.proteins_per_100g), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                            )

                            OutlinedTextField(
                                value = manualFat,
                                onValueChange = { manualFat = it },
                                label = { Text(stringResource(R.string.fats_per_100g), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                            )

                            OutlinedTextField(
                                value = manualCarbs,
                                onValueChange = { manualCarbs = it },
                                label = { Text(stringResource(R.string.carbs_per_100g), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                            )

                            val proteinVal = manualProtein.toDoubleOrNull() ?: 0.0
                            val fatVal = manualFat.toDoubleOrNull() ?: 0.0
                            val carbsVal = manualCarbs.toDoubleOrNull() ?: 0.0
                            val calories = proteinVal * 4 + fatVal * 9 + carbsVal * 4

                            if (manualName.isNotBlank() && (proteinVal > 0 || fatVal > 0 || carbsVal > 0)) {
                                Text(
                                    text = stringResource(
                                        R.string.kbju_manual_per_100g,
                                        calories.toInt(),
                                        proteinVal.toInt(),
                                        fatVal.toInt(),
                                        carbsVal.toInt()
                                    ),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    is RecognitionResult.Error -> {
                        Text(
                            text = result.message,
                            color = MaterialTheme.colorScheme.error
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
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.add), color = MaterialTheme.colorScheme.onPrimary)
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

                                val finalName = manualName.ifBlank { result.suggestedName }

                                if (manualAddMode == ManualAddMode.SAVE_AND_ADD) {
                                    onAddToDatabase(
                                        finalName,
                                        selectedMealType,
                                        protein,
                                        fat,
                                        carbs
                                    )
                                }
                                onConfirm(
                                    finalName,
                                    selectedMealType,
                                    portion.toDoubleOrNull() ?: 100.0,
                                    protein * factor,
                                    fat * factor,
                                    carbs * factor
                                )
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = isManualFormValid
                        ) {
                            Text(
                                text = if (manualAddMode == ManualAddMode.SAVE_AND_ADD) {
                                    stringResource(R.string.save_and_add)
                                } else {
                                    stringResource(R.string.add_to_diary)
                                },
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                is RecognitionResult.LowConfidence -> {
                    if (isAddingToDatabase) {
                        Button(
                            onClick = {
                                val protein = manualProtein.toDoubleOrNull() ?: 0.0
                                val fat = manualFat.toDoubleOrNull() ?: 0.0
                                val carbs = manualCarbs.toDoubleOrNull() ?: 0.0
                                val factor = portion.toDoubleOrNull()?.div(100) ?: 1.0

                                val finalName = manualName.ifBlank { result.suggestedName }

                                // Не сохраняем в базу при LowConfidence – только добавляем в дневник
                                onConfirm(
                                    finalName,
                                    selectedMealType,
                                    portion.toDoubleOrNull() ?: 100.0,
                                    protein * factor,
                                    fat * factor,
                                    carbs * factor
                                )
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = isManualFormValid
                        ) {
                            Text(stringResource(R.string.add_to_diary), color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
                is RecognitionResult.MultipleMatches -> { }
                is RecognitionResult.Error -> {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(stringResource(R.string.close), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        dismissButton = {
            if (result !is RecognitionResult.MultipleMatches && result !is RecognitionResult.LowConfidence) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )

    // Диалог выбора из локальной базы
    if (showLocalSearchDialog) {
        AlertDialog(
            onDismissRequest = { showLocalSearchDialog = false },
            title = { Text(stringResource(R.string.select_product_from_database)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = localSearchQuery,
                        onValueChange = { localSearchQuery = it },
                        label = { Text(stringResource(R.string.search)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(localSearchResults) { product ->
                            TextButton(
                                onClick = {
                                    // Добавляем выбранный продукт в дневник
                                    val factor = portion.toDoubleOrNull()?.div(100) ?: 1.0
                                    onConfirm(
                                        product.name,
                                        selectedMealType,
                                        portion.toDoubleOrNull() ?: 100.0,
                                        product.proteinPer100g * factor,
                                        product.fatPer100g * factor,
                                        product.carbsPer100g * factor
                                    )
                                    showLocalSearchDialog = false
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        text = product.name.toUserVisibleFoodName(),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(
                                            R.string.kbju_inline_per_100g,
                                            product.caloriesPer100g.toInt(),
                                            product.proteinPer100g.toInt(),
                                            product.fatPer100g.toInt(),
                                            product.carbsPer100g.toInt()
                                        ),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                        }
                        if (localSearchQuery.isNotBlank() && localSearchResults.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.not_found),
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showLocalSearchDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun MealTypeSelector(
    selectedMealType: MealType,
    onMealTypeSelected: (MealType) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MealTypeButton(
                title = stringResource(R.string.breakfast),
                isSelected = selectedMealType == MealType.BREAKFAST,
                modifier = Modifier.weight(1.2f),
                onClick = { onMealTypeSelected(MealType.BREAKFAST) }
            )
            MealTypeButton(
                title = stringResource(R.string.lunch),
                isSelected = selectedMealType == MealType.LUNCH,
                modifier = Modifier.weight(1f),
                onClick = { onMealTypeSelected(MealType.LUNCH) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MealTypeButton(
                title = stringResource(R.string.dinner),
                isSelected = selectedMealType == MealType.DINNER,
                modifier = Modifier.weight(1f),
                onClick = { onMealTypeSelected(MealType.DINNER) }
            )
            MealTypeButton(
                title = stringResource(R.string.snack),
                isSelected = selectedMealType == MealType.SNACK,
                modifier = Modifier.weight(1.2f),
                onClick = { onMealTypeSelected(MealType.SNACK) }
            )
        }
    }
}

@Composable
fun MealTypeButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
            .widthIn(min = 0.dp)
            .padding(horizontal = 1.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
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
        Text(text = value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
