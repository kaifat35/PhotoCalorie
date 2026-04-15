package com.stafeewa.photocalorie.app.utils

import android.content.Context
import android.os.Build
import java.util.Locale

object LocaleManager {

    fun setLocale(context: Context, languageCode: String): Context {
        return updateResources(context, languageCode)
    }

    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = resources.configuration

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("Deprecation")
            configuration.locale = locale
            resources.updateConfiguration(configuration, resources.displayMetrics)
            context
        }
    }

    fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0) ?: Locale.getDefault()
        } else {
            @Suppress("Deprecation")
            context.resources.configuration.locale
        }
    }
}