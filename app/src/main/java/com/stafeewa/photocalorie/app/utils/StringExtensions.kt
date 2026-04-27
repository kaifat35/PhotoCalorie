package com.stafeewa.photocalorie.app.utils

import java.util.Locale

fun String.toUserVisibleFoodName(): String {
    val normalized = trim()
    val keywordPattern = Regex(
        """\s*(?:[|•—–-]\s*)?(?:keywords?|ключ(?:ев(?:ое|ые))?\s*слов(?:о|а)?)\s*[:=]?\s*.*$""",
        RegexOption.IGNORE_CASE
    )
    val withoutKeyword = normalized.replace(keywordPattern, "").trim()
    val cleanedName = withoutKeyword.ifEmpty { normalized }
    return cleanedName.translateFoodNameForCurrentLocale()
}

private fun String.translateFoodNameForCurrentLocale(): String {
    val currentLanguage = Locale.getDefault().language
    val normalized = trim()
    return if (currentLanguage.equals("ru", ignoreCase = true)) {
        EnglishToRussianMap.map[normalized] ?: normalized
    } else {
        EnglishToRussianMap.reverseMap[normalized.lowercase()] ?: normalized
    }
}
