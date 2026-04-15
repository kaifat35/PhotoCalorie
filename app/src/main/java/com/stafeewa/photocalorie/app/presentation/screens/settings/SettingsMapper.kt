package com.stafeewa.photocalorie.app.presentation.screens.settings


import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.stafeewa.photocalorie.app.R
import com.stafeewa.photocalorie.app.domain.entity.Interval
import com.stafeewa.photocalorie.app.domain.entity.Language

@Composable
fun Language.toLocalizedName(): String {
    return when (this) {
        Language.ENGLISH -> stringResource(R.string.english)
        Language.RUSSIAN -> stringResource(R.string.russian)
    }
}

@Composable
fun Interval.toLocalizedName(): String {
    return when (this) {
        Interval.MIN_15 -> stringResource(R.string.interval_15_minutes)
        Interval.MIN_30 -> stringResource(R.string.interval_30_minutes)
        Interval.HOUR_1 -> stringResource(R.string.interval_1_hour)
        Interval.HOUR_2 -> stringResource(R.string.interval_2_hours)
        Interval.HOUR_4 -> stringResource(R.string.interval_4_hours)
        Interval.HOUR_8 -> stringResource(R.string.interval_8_hours)
        Interval.HOUR_24 -> stringResource(R.string.interval_24_hours)
    }
}
