package com.stafeewa.photocalorie.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.stafeewa.photocalorie.app.domain.entity.MealType
import com.stafeewa.photocalorie.app.domain.entity.Product
import com.stafeewa.photocalorie.app.domain.repository.ProductRepository
import com.stafeewa.photocalorie.app.presentation.navigation.NavigationBarExample
import com.stafeewa.photocalorie.app.presentation.ui.theme.PhotoCalorieTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var productRepository: ProductRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            val existing = productRepository.searchProducts("Картошка фри").first()
            if (existing.isEmpty()) {
                val potatoFries = Product(
                    name = "Картошка фри",
                    mealType = MealType.DINNER,
                    defaultPortion = 150.0,
                    proteinPer100g = 3.5,
                    fatPer100g = 15.0,
                    carbsPer100g = 41.0,
                    caloriesPer100g = 312.0,
                    keywords = listOf("fries", "картофель", "фри")
                )
                productRepository.addProduct(potatoFries)
                Log.d("InitDB", "Картошка фри добавлена в базу")
            }
        }

        // Инициализация базы продуктов при запуске приложения
        lifecycleScope.launch {
            productRepository.initDefaultProducts()
        }

        setContent {
            PhotoCalorieTheme {
                NavigationBarExample(modifier = Modifier)
            }
        }
    }
}