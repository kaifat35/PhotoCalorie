package com.stafeewa.photocalorie.app.domain.entity

enum class ActivityLevel(
    val title: String,
    val multiplier: Double,
    val description: String
) {
    SEDENTARY(
        title = "Сидячий образ жизни",
        multiplier = 1.2,
        description = "Минимальная активность или её отсутствие"
    ),
    LIGHT(
        title = "Легкая активность",
        multiplier = 1.375,
        description = "Легкие упражнения 1-3 раза в неделю"
    ),
    MODERATE(
        title = "Умеренная активность",
        multiplier = 1.55,
        description = "Умеренные упражнения 3-5 раз в неделю"
    ),
    ACTIVE(
        title = "Высокая активность",
        multiplier = 1.725,
        description = "Интенсивные упражнения 6-7 раз в неделю"
    ),
    VERY_ACTIVE(
        title = "Очень высокая активность",
        multiplier = 1.9,
        description = "Очень тяжелые упражнения или физическая работа"
    );

    companion object {
        fun fromTitle(title: String): ActivityLevel? {
            return entries.find { it.title == title }
        }
    }
}