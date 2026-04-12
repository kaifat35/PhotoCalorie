package com.stafeewa.photocalorie.app.presentation.screens.foodmain

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stafeewa.photocalorie.app.R
import com.stafeewa.photocalorie.app.domain.entity.MealType
import com.stafeewa.photocalorie.app.domain.entity.Product

@Composable
fun AddFoodDialog(
    mealType: MealType,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        mealType: MealType,
        portion: Double,
        protein: Double,
        fat: Double,
        carbs: Double,
    ) -> Unit
) {
    val viewModel: FoodIntakeViewModel = hiltViewModel()

    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var portion by remember { mutableStateOf("100") }
    var showProductList by remember { mutableStateOf(false) }
    var isManualMode by remember { mutableStateOf(false) }

    // Поля для ручного ввода
    var manualName by remember { mutableStateOf("") }
    var manualProtein by remember { mutableStateOf("") }
    var manualFat by remember { mutableStateOf("") }
    var manualCarbs by remember { mutableStateOf("") }

    val searchResults by viewModel.productSearchResults.collectAsStateWithLifecycle()

    // Рассчитываем КБЖУ на основе выбранного продукта и порции
    val calculatedKbju = remember(selectedProduct, portion) {
        val portionValue = portion.toDoubleOrNull() ?: 0.0
        selectedProduct?.calculateKbjuForPortion(portionValue)
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2 && !isManualMode) {
            viewModel.searchProducts(searchQuery)
            showProductList = true
        } else {
            showProductList = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isManualMode) "Добавить вручную" else "Добавить ${getMealTypeName(mealType)}",
                    fontFamily = FontFamily(Font(R.font.jura)),
                    fontSize = 24.sp,
                    color = Color.White
                )

                // Иконка переключения режима
                IconButton(
                    onClick = {
                        isManualMode = !isManualMode
                        if (!isManualMode) {
                            // Очищаем ручные поля при переключении
                            manualName = ""
                            manualProtein = ""
                            manualFat = ""
                            manualCarbs = ""
                        } else {
                            // Очищаем поиск при переключении
                            searchQuery = ""
                            selectedProduct = null
                            showProductList = false
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isManualMode) R.drawable.ic_search else R.drawable.ic_edit
                        ),
                        contentDescription = if (isManualMode) "Поиск" else "Ручной ввод",
                        tint = Color(0xFF009E1D),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isManualMode) {
                    // ============ РУЧНОЙ РЕЖИМ ============
                    OutlinedTextField(
                        value = manualName,
                        onValueChange = { manualName = it },
                        label = { Text("Название блюда", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                        placeholder = { Text("Например: Мой фирменный салат", color = Color.White.copy(alpha = 0.5f)) }
                    )

                    OutlinedTextField(
                        value = manualProtein,
                        onValueChange = { manualProtein = it },
                        label = { Text("Белки (на 100г)", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                    )

                    OutlinedTextField(
                        value = manualFat,
                        onValueChange = { manualFat = it },
                        label = { Text("Жиры (на 100г)", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                    )

                    OutlinedTextField(
                        value = manualCarbs,
                        onValueChange = { manualCarbs = it },
                        label = { Text("Углеводы (на 100г)", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                    )

                    OutlinedTextField(
                        value = portion,
                        onValueChange = { portion = it },
                        label = { Text("Вес порции (г)", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                    )

                    // Показываем рассчитанное КБЖУ для ручного режима
                    val proteinValue = manualProtein.toDoubleOrNull() ?: 0.0
                    val fatValue = manualFat.toDoubleOrNull() ?: 0.0
                    val carbsValue = manualCarbs.toDoubleOrNull() ?: 0.0
                    val portionValue = portion.toDoubleOrNull() ?: 0.0
                    val factor = portionValue / 100.0

                    if (manualName.isNotBlank() && (proteinValue > 0 || fatValue > 0 || carbsValue > 0)) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                        Text(
                            text = "КБЖУ на ${portionValue.toInt()} г:",
                            color = Color.White,
                            fontFamily = FontFamily(Font(R.font.jura)),
                            fontSize = 16.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            NutrientInfo("Калории", "${((proteinValue * 4 + fatValue * 9 + carbsValue * 4) * factor).toInt()} ккал", Color(0xFFFF9800))
                            NutrientInfo("Белки", "${(proteinValue * factor).toInt()} г", Color(0xFF4CAF50))
                            NutrientInfo("Жиры", "${(fatValue * factor).toInt()} г", Color(0xFF2196F3))
                            NutrientInfo("Углеводы", "${(carbsValue * factor).toInt()} г", Color(0xFF9C27B0))
                        }
                    }

                } else {
                    // ============ РЕЖИМ ПОИСКА ============
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            selectedProduct = null
                        },
                        label = { Text("Название блюда", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                        placeholder = { Text("Например: Овсяная каша", color = Color.White.copy(alpha = 0.5f)) }
                    )

                    // Список найденных продуктов
                    if (showProductList && searchResults.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            LazyColumn {
                                items(searchResults) { product ->
                                    ProductSearchItem(
                                        product = product,
                                        onClick = {
                                            selectedProduct = product
                                            searchQuery = product.name
                                            showProductList = false
                                            portion = product.defaultPortion.toInt().toString()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Отображение выбранного продукта и его КБЖУ
                    if (selectedProduct != null) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.3f))

                        Text(
                            text = "Выбрано: ${selectedProduct!!.name}",
                            color = Color(0xFF009E1D),
                            fontFamily = FontFamily(Font(R.font.jura)),
                            fontSize = 16.sp
                        )

                        // КБЖУ на 100г
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            NutrientInfo("Калории", "${selectedProduct!!.caloriesPer100g.toInt()} ккал", Color(0xFFFF9800))
                            NutrientInfo("Белки", "${selectedProduct!!.proteinPer100g.toInt()} г", Color(0xFF4CAF50))
                            NutrientInfo("Жиры", "${selectedProduct!!.fatPer100g.toInt()} г", Color(0xFF2196F3))
                            NutrientInfo("Углеводы", "${selectedProduct!!.carbsPer100g.toInt()} г", Color(0xFF9C27B0))
                        }

                        // Поле для ввода веса
                        OutlinedTextField(
                            value = portion,
                            onValueChange = { portion = it },
                            label = { Text("Вес порции (г)", color = Color.White.copy(alpha = 0.7f)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                        )

                        // Рассчитанное КБЖУ для выбранной порции
                        if (calculatedKbju != null && portion.toDoubleOrNull() != null) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                            Text(
                                text = "КБЖУ на ${portion.toDoubleOrNull()?.toInt()} г:",
                                color = Color.White,
                                fontFamily = FontFamily(Font(R.font.jura)),
                                fontSize = 16.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                NutrientInfo("Калории", "${calculatedKbju.calories.toInt()} ккал", Color(0xFFFF9800))
                                NutrientInfo("Белки", "${calculatedKbju.protein.toInt()} г", Color(0xFF4CAF50))
                                NutrientInfo("Жиры", "${calculatedKbju.fat.toInt()} г", Color(0xFF2196F3))
                                NutrientInfo("Углеводы", "${calculatedKbju.carbs.toInt()} г", Color(0xFF9C27B0))
                            }
                        }
                    } else if (searchQuery.length >= 2 && searchResults.isEmpty()) {
                        Text(
                            text = "Ничего не найдено. Нажмите на иконку руки для ручного ввода",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isManualMode) {
                        val portionValue = portion.toDoubleOrNull() ?: 100.0
                        val proteinValue = manualProtein.toDoubleOrNull() ?: 0.0
                        val fatValue = manualFat.toDoubleOrNull() ?: 0.0
                        val carbsValue = manualCarbs.toDoubleOrNull() ?: 0.0
                        val factor = portionValue / 100.0

                        onConfirm(
                            manualName.ifEmpty { "Новое блюдо" },
                            mealType,
                            portionValue,
                            proteinValue * factor,
                            fatValue * factor,
                            carbsValue * factor
                        )

                    } else if (selectedProduct != null) {
                        val portionValue = portion.toDoubleOrNull() ?: 100.0
                        val kbju = selectedProduct!!.calculateKbjuForPortion(portionValue)
                        onConfirm(
                            selectedProduct!!.name,
                            mealType,
                            portionValue,
                            kbju.protein,
                            kbju.fat,
                            kbju.carbs
                        )

                    }
                },
                enabled = if (isManualMode) {
                    manualName.isNotBlank() &&
                            (manualProtein.toDoubleOrNull() != null ||
                                    manualFat.toDoubleOrNull() != null ||
                                    manualCarbs.toDoubleOrNull() != null)
                } else {
                    selectedProduct != null
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009E1D))
            ) {
                Text("Добавить", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = Color.White.copy(alpha = 0.7f))
            }
        },
        containerColor = Color(0xFF474646),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}
@Composable
fun ProductSearchItem(
    product: Product,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Text(
            text = product.name,
            color = Color.White,
            fontFamily = FontFamily(Font(R.font.jura)),
            fontSize = 18.sp
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "${product.caloriesPer100g.toInt()} ккал",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Text(
                text = "Б: ${product.proteinPer100g.toInt()}г",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Text(
                text = "Ж: ${product.fatPer100g.toInt()}г",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Text(
                text = "У: ${product.carbsPer100g.toInt()}г",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
}

@Composable
fun NutrientInfo(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = color
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.White,
            fontFamily = FontFamily(Font(R.font.jura))
        )
    }
}

private fun getMealTypeName(mealType: MealType): String {
    return when (mealType) {
        MealType.BREAKFAST -> "завтрак"
        MealType.LUNCH -> "обед"
        MealType.DINNER -> "ужин"
        MealType.SNACK -> "перекус"
    }
}