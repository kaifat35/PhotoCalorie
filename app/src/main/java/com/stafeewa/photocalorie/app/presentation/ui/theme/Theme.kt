package com.stafeewa.photocalorie.app.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Цвета для светлой темы (почти белый фон, голубые/зелёные акценты)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),      // тёмно-зелёный для primary
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFF0288D1),    // голубой
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB3E5FC),
    onSecondaryContainer = Color(0xFF01579B),
    tertiary = Color(0xFF4CAF50),     // зелёный
    background = Color(0xFFF5F5F5),    // почти белый фон
    onBackground = Color(0xFF212121),
    surface = Color.White,
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFE0E0E0)
)

// Тёмная тема – адаптированные тёмные версии
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF1B5E20),
    primaryContainer = Color(0xFF2E7D32),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFF4FC3F7),
    onSecondary = Color(0xFF01579B),
    secondaryContainer = Color(0xFF0288D1),
    onSecondaryContainer = Color(0xFFB3E5FC),
    tertiary = Color(0xFF66BB6A),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C)
)

@Composable
fun PhotoCalorieTheme(
    darkTheme: Boolean? = true,   // null = системная, true/false = принудительно
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = when (darkTheme) {
        null -> isSystemInDarkTheme()
        else -> darkTheme
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme == null -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    disabledTextColor = Color.Gray,
    errorTextColor = Color.Red,
    cursorColor = Color.White,
    focusedBorderColor = Color.White,
    unfocusedBorderColor = Color.Gray,
    focusedLabelColor = Color.Transparent,
    unfocusedLabelColor = Color.Transparent,
    focusedContainerColor = Color(0xFF474646),
    unfocusedContainerColor = Color(0xFF474646)
)