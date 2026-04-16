package com.stafeewa.photocalorie.app.utils

fun String.toUserVisibleFoodName(): String {
    val normalized = trim()
    val keywordPattern = Regex(
        """\s*(?:[|•—–-]\s*)?(?:keywords?|ключ(?:ев(?:ое|ые))?\s*слов(?:о|а)?)\s*[:=]?\s*.*$""",
        RegexOption.IGNORE_CASE
    )
    val withoutKeyword = normalized.replace(keywordPattern, "").trim()
    return withoutKeyword.ifEmpty { normalized }
}