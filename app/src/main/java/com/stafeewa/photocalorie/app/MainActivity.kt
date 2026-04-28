package com.stafeewa.photocalorie.app

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Инициализация базы продуктов при запуске приложения
        lifecycleScope.launch {
            productRepository.initDefaultProducts()
        }

        setContent {
            val sharedPrefs = remember { getSharedPreferences("app_settings", MODE_PRIVATE) }
            var themeMode by remember {
                mutableStateOf(loadThemeMode(sharedPrefs))
            }

            DisposableEffect(sharedPrefs) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == THEME_MODE_PREF_KEY) {
                        themeMode = loadThemeMode(sharedPrefs)
                    }
                }
                sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose {
                    sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            PhotoCalorieTheme(
                darkTheme = themeMode.isDark,
                dynamicColor = false
            ) {
                NavigationBarExample(modifier = Modifier)
            }
        }
    }

    private fun loadThemeMode(sharedPrefs: android.content.SharedPreferences): ThemeMode {
        val themeName = sharedPrefs.getString(THEME_MODE_PREF_KEY, ThemeMode.DEFAULT.name)
            ?: ThemeMode.DEFAULT.name
        return try {
            ThemeMode.valueOf(themeName)
        } catch (_: IllegalArgumentException) {
            ThemeMode.DEFAULT
        }
    }

    private companion object {
        const val THEME_MODE_PREF_KEY = "theme_mode"
    }
}