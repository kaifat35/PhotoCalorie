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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.stafeewa.photocalorie.app.utils.toUserVisibleFoodName

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
    val context = LocalContext.current
    val newDishString = stringResource(R.string.new_dish)

    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var portion by remember { mutableStateOf("100") }
    var showProductList by remember { mutableStateOf(false) }
    var isManualMode by remember { mutableStateOf(false) }

    var manualName by remember { mutableStateOf("") }
    var manualProtein by remember { mutableStateOf("") }
    var manualFat by remember { mutableStateOf("") }
    var manualCarbs by remember { mutableStateOf("") }

    val searchResults by viewModel.productSearchResults.collectAsStateWithLifecycle()

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
                    text = if (isManualMode) {
                        stringResource(R.string.add_manually)
                    } else {
                        stringResource(R.string.add_food_for_meal, getMealTypeName(mealType))
                    },
                    fontFamily = FontFamily(Font(R.font.jura)),
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = {
                        isManualMode = !isManualMode
                        if (!isManualMode) {
                            manualName = ""
                            manualProtein = ""
                            manualFat = ""
                            manualCarbs = ""
                        } else {
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
                        contentDescription = if (isManualMode) {
                            stringResource(R.string.search)
                        } else {
                            stringResource(R.string.add_manually)
                        },
                        tint = MaterialTheme.colorScheme.primary,
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
                    OutlinedTextField(
                        value = manualName,
                        onValueChange = { manualName = it },
                        label = { Text(stringResource(R.string.dish_name), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        placeholder = { Text(stringResource(R.string.manual_dish_name_hint), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) }
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

                    OutlinedTextField(
                        value = portion,
                        onValueChange = { portion = it },
                        label = { Text(stringResource(R.string.portion_weight_grams), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                    )

                    val proteinValue = manualProtein.toDoubleOrNull() ?: 0.0
                    val fatValue = manualFat.toDoubleOrNull() ?: 0.0
                    val carbsValue = manualCarbs.toDoubleOrNull() ?: 0.0
                    val portionValue = portion.toDoubleOrNull() ?: 0.0
                    val factor = portionValue / 100.0

                    if (manualName.isNotBlank() && (proteinValue > 0 || fatValue > 0 || carbsValue > 0)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        Text(
                            text = stringResource(R.string.kbju_for_portion, portionValue.toInt()),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily(Font(R.font.jura)),
                            fontSize = 16.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            NutrientInfo(stringResource(R.string.calories), "${((proteinValue * 4 + fatValue * 9 + carbsValue * 4) * factor).toInt()} ${stringResource(R.string.kcal_short)}", MaterialTheme.colorScheme.tertiary)
                            NutrientInfo(stringResource(R.string.proteins), "${(proteinValue * factor).toInt()} ${stringResource(R.string.grams_short)}", MaterialTheme.colorScheme.secondary)
                            NutrientInfo(stringResource(R.string.fats), "${(fatValue * factor).toInt()} ${stringResource(R.string.grams_short)}", MaterialTheme.colorScheme.error)
                            NutrientInfo(stringResource(R.string.carbohydrates), "${(carbsValue * factor).toInt()} ${stringResource(R.string.grams_short)}", Color(0xFF9C27B0))
                        }
                    }

                } else {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            selectedProduct = null
                        },
                        label = { Text(stringResource(R.string.dish_name), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        placeholder = { Text(stringResource(R.string.search_dish_name_hint), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) }
                    )

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
                                            searchQuery = product.name.toUserVisibleFoodName()
                                            showProductList = false
                                            portion = product.defaultPortion.toInt().toString()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (selectedProduct != null) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))

                        Text(
                            text = stringResource(R.string.selected_dish, selectedProduct!!.name.toUserVisibleFoodName()),
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily(Font(R.font.jura)),
                            fontSize = 16.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            NutrientInfo(stringResource(R.string.calories), "${selectedProduct!!.caloriesPer100g.toInt()} ${stringResource(R.string.kcal_short)}", MaterialTheme.colorScheme.tertiary)
                            NutrientInfo(stringResource(R.string.proteins), "${selectedProduct!!.proteinPer100g.toInt()} ${stringResource(R.string.grams_short)}", MaterialTheme.colorScheme.secondary)
                            NutrientInfo(stringResource(R.string.fats), "${selectedProduct!!.fatPer100g.toInt()} ${stringResource(R.string.grams_short)}", MaterialTheme.colorScheme.error)
                            NutrientInfo(stringResource(R.string.carbohydrates), "${selectedProduct!!.carbsPer100g.toInt()} ${stringResource(R.string.grams_short)}", Color(0xFF9C27B0))
                        }

                        OutlinedTextField(
                            value = portion,
                            onValueChange = { portion = it },
                            label = { Text(stringResource(R.string.portion_weight_grams), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                        )

                        if (calculatedKbju != null && portion.toDoubleOrNull() != null) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                            Text(
                                text = stringResource(R.string.kbju_for_portion, portion.toDoubleOrNull()?.toInt() ?: 0),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily(Font(R.font.jura)),
                                fontSize = 16.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                NutrientInfo(stringResource(R.string.calories), "${calculatedKbju.calories.toInt()} ${stringResource(R.string.kcal_short)}", MaterialTheme.colorScheme.tertiary)
                                NutrientInfo(stringResource(R.string.proteins), "${calculatedKbju.protein.toInt()} ${stringResource(R.string.grams_short)}", MaterialTheme.colorScheme.secondary)
                                NutrientInfo(stringResource(R.string.fats), "${calculatedKbju.fat.toInt()} ${stringResource(R.string.grams_short)}", MaterialTheme.colorScheme.error)
                                NutrientInfo(stringResource(R.string.carbohydrates), "${calculatedKbju.carbs.toInt()} ${stringResource(R.string.grams_short)}", Color(0xFF9C27B0))
                            }
                        }
                    } else if (searchQuery.length >= 2 && searchResults.isEmpty()) {
                        Text(
                            text = stringResource(R.string.not_found_switch_to_manual),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
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
                            manualName.ifEmpty { newDishString },
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
                            selectedProduct!!.name.toUserVisibleFoodName(),
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.add), color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
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
            text = product.name.toUserVisibleFoodName(),
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily(Font(R.font.jura)),
            fontSize = 18.sp
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "${product.caloriesPer100g.toInt()} ${stringResource(R.string.kcal_short)}",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Text(
                text = stringResource(R.string.nutrient_short_proteins, product.proteinPer100g.toInt()),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Text(
                text = stringResource(R.string.nutrient_short_fats, product.fatPer100g.toInt()),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Text(
                text = stringResource(R.string.nutrient_short_carbs, product.carbsPer100g.toInt()),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
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
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily(Font(R.font.jura))
        )
    }
}

@Composable
private fun getMealTypeName(mealType: MealType): String {
    return when (mealType) {
        MealType.BREAKFAST -> stringResource(R.string.breakfast)
        MealType.LUNCH -> stringResource(R.string.lunch)
        MealType.DINNER -> stringResource(R.string.dinner)
        MealType.SNACK -> stringResource(R.string.snack)
    }
}
