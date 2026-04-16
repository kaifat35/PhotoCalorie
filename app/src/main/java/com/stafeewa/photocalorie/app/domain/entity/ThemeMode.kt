package com.stafeewa.photocalorie.app.domain.entity

enum class ThemeMode(val isDark: Boolean) {
    LIGHT(false),
    DARK(true);

    companion object {
        val DEFAULT = LIGHT
    }
}