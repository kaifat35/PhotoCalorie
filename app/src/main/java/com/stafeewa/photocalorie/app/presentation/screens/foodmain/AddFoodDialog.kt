package com.stafeewa.photocalorie.app.presentation.screens.foodmain

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stafeewa.photocalorie.app.R
import com.stafeewa.photocalorie.app.domain.entity.MealType

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
        carbs: Double
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var portion by remember { mutableStateOf("100.0") }
    var protein by remember { mutableStateOf("0.0") }
    var fat by remember { mutableStateOf("0.0") }
    var carbs by remember { mutableStateOf("0.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Добавить ${getMealTypeName(mealType)}",
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 24.sp,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название блюда", color = Color.White.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                    placeholder = { Text("Например: Овсянка", color = Color.White.copy(alpha = 0.5f)) }
                )

                OutlinedTextField(
                    value = portion,
                    onValueChange = { portion = it },
                    label = { Text("Вес порции (г)", color = Color.White.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                )

                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text("Белки (г)", color = Color.White.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                )

                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it },
                    label = { Text("Жиры (г)", color = Color.White.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                )

                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text("Углеводы (г)", color = Color.White.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val portionValue = portion.toDoubleOrNull() ?: 0.0
                    val proteinValue = protein.toDoubleOrNull() ?: 0.0
                    val fatValue = fat.toDoubleOrNull() ?: 0.0
                    val carbsValue = carbs.toDoubleOrNull() ?: 0.0

                    if (name.isNotBlank() && portionValue > 0) {
                        onConfirm(name, mealType, portionValue, proteinValue, fatValue, carbsValue)
                        onDismiss()
                    }
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

private fun getMealTypeName(mealType: MealType): String {
    return when (mealType) {
        MealType.BREAKFAST -> "завтрак"
        MealType.LUNCH -> "обед"
        MealType.DINNER -> "ужин"
        MealType.SNACK -> "перекус"
    }
}