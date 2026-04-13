package com.stafeewa.photocalorie.app.utils

private val serviceTailPattern = Regex(
    pattern = """\s*(?:[|•—–-]\s*)?(?:keywords?|ключ(?:ев(?:ое|ые))?\s*слов(?:о|а)?)\s*[:=]?\s*[\s\S]*$""",
    options = setOf(RegexOption.IGNORE_CASE)
)

fun String.toUserVisibleFoodName(): String {
    val normalized = trim()
    val withoutServiceTail = normalized.replace(serviceTailPattern, "").trim()
    return withoutServiceTail.ifEmpty { normalized }
}