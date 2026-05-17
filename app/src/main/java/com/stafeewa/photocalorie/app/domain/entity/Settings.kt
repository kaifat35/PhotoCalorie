package com.stafeewa.photocalorie.app.domain.entity

import java.util.Locale

data class Settings(
    val language: Language
) {

    companion object {
        val DEFAULT_LANGUAGE = Language.ENGLISH
    }
}

enum class Language(val code: String) {
    ENGLISH("en"),
    RUSSIAN("ru");

    fun toReadableFormat(): String {
        return when (this) {
            ENGLISH -> "English"
            RUSSIAN -> "Русский"
        }
    }

    fun getLocale(): Locale = Locale(code)
}