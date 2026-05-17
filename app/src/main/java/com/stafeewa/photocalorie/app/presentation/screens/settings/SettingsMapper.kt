package com.stafeewa.photocalorie.app.presentation.screens.settings


import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.stafeewa.photocalorie.app.R
import com.stafeewa.photocalorie.app.domain.entity.Language
import com.stafeewa.photocalorie.app.domain.entity.ThemeMode

@Composable
fun Language.toLocalizedName(): String {
    return when (this) {
        Language.ENGLISH -> stringResource(R.string.english)
        Language.RUSSIAN -> stringResource(R.string.russian)
    }
}


@Composable
fun ThemeMode.toLocalizedName(): String {
    return when (this) {
        ThemeMode.LIGHT -> stringResource(R.string.light_theme)
        ThemeMode.DARK -> stringResource(R.string.dark_theme)
    }
}
