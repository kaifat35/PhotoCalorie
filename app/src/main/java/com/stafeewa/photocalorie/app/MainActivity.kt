package com.stafeewa.photocalorie.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.stafeewa.photocalorie.app.domain.entity.ThemeMode
import com.stafeewa.photocalorie.app.domain.repository.ProductRepository
import com.stafeewa.photocalorie.app.presentation.navigation.NavigationBarExample
import com.stafeewa.photocalorie.app.presentation.ui.theme.PhotoCalorieTheme
import com.stafeewa.photocalorie.app.utils.LocaleManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var productRepository: ProductRepository

    override fun attachBaseContext (newBase: android . content . Context ) {

        val prefs = newBase.getSharedPreferences( "app_settings" , MODE_PRIVATE)
        val languageCode = prefs.getString( "language" , "en" ) ?: "en"
        super.attachBaseContext (LocaleManager.setLocale(newBase, languageCode))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Инициализация базы продуктов при запуске приложения
        lifecycleScope.launch {
            productRepository.initDefaultProducts()
        }

        setContent {
            val sharedPrefs = getSharedPreferences("app_settings", MODE_PRIVATE)
            val themeName = sharedPrefs.getString("theme_mode", ThemeMode.DEFAULT.name) ?: ThemeMode.DEFAULT.name
            val isDark = try {
                ThemeMode.valueOf(themeName).isDark
            } catch (e: Exception) {
                ThemeMode.DEFAULT.isDark
            }
            PhotoCalorieTheme(
                darkTheme = isDark,   // принудительно
                dynamicColor = false  // отключаем dynamic color, чтобы не перебивало ручной выбор
            ) {
                NavigationBarExample(modifier = Modifier)
            }
        }
    }
}