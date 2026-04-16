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
    primary = Color(0xFF006D3B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF93F7AC),
    onPrimaryContainer = Color(0xFF00210F),
    secondary = Color(0xFF0E61A4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0E4FF),
    onSecondaryContainer = Color(0xFF001D36),
    tertiary = Color(0xFF4A6354),
    background = Color(0xFFFBFDF8),
    onBackground = Color(0xFF191C19),
    surface = Color(0xFFFBFDF8),
    onSurface = Color(0xFF191C19),
    surfaceVariant = Color(0xFFDDF3E4)
)

// Тёмная тема – адаптированные тёмные версии
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF77DA91),
    onPrimary = Color(0xFF00391D),
    primaryContainer = Color(0xFF00522C),
    onPrimaryContainer = Color(0xFF93F7AC),
    secondary = Color(0xFFA2C9FF),
    onSecondary = Color(0xFF00315A),
    secondaryContainer = Color(0xFF00497E),
    onSecondaryContainer = Color(0xFFD0E4FF),
    tertiary = Color(0xFFB0CCB8),
    background = Color(0xFF111411),
    onBackground = Color(0xFFE1E3DE),
    surface = Color(0xFF111411),
    onSurface = Color(0xFFE1E3DE),
    surfaceVariant = Color(0xFF3C4940)
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
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
    errorTextColor = Color.Red,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface
)
