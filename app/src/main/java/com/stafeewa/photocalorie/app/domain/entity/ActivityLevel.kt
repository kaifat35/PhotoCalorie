package com.stafeewa.photocalorie.app.domain.entity

import com.stafeewa.photocalorie.app.R

enum class ActivityLevel(val titleRes: Int, val multiplier: Double) {
    SEDENTARY(R.string.activity_sedentary, 1.2),
    LIGHTLY_ACTIVE(R.string.activity_lightly_active, 1.375),
    MODERATELY_ACTIVE(R.string.activity_moderately_active, 1.55),
    VERY_ACTIVE(R.string.activity_very_active, 1.725),
    EXTRA_ACTIVE(R.string.activity_extra_active, 1.9);

    companion object {
        fun fromMultiplier(multiplier: Double): ActivityLevel {
            return values().find { it.multiplier == multiplier } ?: MODERATELY_ACTIVE
        }
    }
}