package com.stafeewa.photocalorie.app.utils

object EnglishToRussianMap {
    private val translitMap = mapOf(
        'а' to "a", 'б' to "b", 'в' to "v", 'г' to "g", 'д' to "d", 'е' to "e", 'ё' to "e",
        'ж' to "zh", 'з' to "z", 'и' to "i", 'й' to "y", 'к' to "k", 'л' to "l", 'м' to "m",
        'н' to "n", 'о' to "o", 'п' to "p", 'р' to "r", 'с' to "s", 'т' to "t", 'у' to "u",
        'ф' to "f", 'х' to "kh", 'ц' to "ts", 'ч' to "ch", 'ш' to "sh", 'щ' to "sch",
        'ъ' to "", 'ы' to "y", 'ь' to "", 'э' to "e", 'ю' to "yu", 'я' to "ya"
    )

    val map = mapOf(
        "apple_pie" to "Яблочный пирог",
        "baby_back_ribs" to "Ребрышки по-домашнему",
        "baklava" to "Пахлава",
        "beef_carpaccio" to "Карпаччо из говядины",
        "beef_tartare" to "Тартар из говядины",
        "beet_salad" to "Салат из свеклы",
        "beignets" to "Бенье",
        "bibimbap" to "Пибимпап",
        "borsh" to "Борщ",
        "bread_pudding" to "Хлебный пудинг",
        "breakfast_burrito" to "Буррито на завтрак",
        "bruschetta" to "Брускетта",
        "buckwheat_with_meat" to "Гречка с мясом",
        "caesar_salad" to "Салат Цезарь",
        "cannoli" to "Канноли",
        "caprese_salad" to "Капрезе",
        "carrot_cake" to "Морковный торт",
        "ceviche" to "Севиче",
        "cheese_plate" to "Сырная тарелка",
        "cheesecake" to "Чизкейк",
        "chicken_curry" to "Карри из курицы",
        "chicken_quesadilla" to "Кесадилья с курицей",
        "chicken_wings" to "Куриные крылышки",
        "chocolate_cake" to "Шоколадный торт",
        "chocolate_mousse" to "Шоколадный мусс",
        "churros" to "Чуррос",
        "clam_chowder" to "Суп из моллюсков",
        "club_sandwich" to "Клубный сэндвич",
        "crab_cakes" to "Крабовые котлеты",
        "creme_brulee" to "Крем-брюле",
        "croque_madame" to "Крок-мадам",
        "cup_cakes" to "Капкейки",
        "deviled_eggs" to "Фаршированные яйца",
        "donuts" to "Пончики",
        "dumplings" to "Пельмени",
        "edamame" to "Эдамаме",
        "eggs_benedict" to "Яйца Бенедикт",
        "escargots" to "Улитки",
        "falafel" to "Фалафель",
        "filet_mignon" to "Филе-миньон",
        "fish_and_chips" to "Рыба с картошкой фри",
        "foie_gras" to "Фуа-гра",
        "french_fries" to "Картофель фри",
        "french_onion_soup" to "Луковый суп по-французски",
        "french_toast" to "Французский тост",
        "fried_calamari" to "Жареные кальмары",
        "fried_eggs" to "Яичница",
        "fried_rice" to "Жареный рис",
        "frozen_yogurt" to "Замороженный йогурт",
        "garlic_bread" to "Чесночный хлеб",
        "gnocchi" to "Ньокки",
        "greek_salad" to "Греческий салат",
        "grilled_cheese_sandwich" to "Сэндвич с плавленым сыром",
        "grilled_salmon" to "Лосось на гриле",
        "guacamole" to "Гуакамоле",
        "gyoza" to "Гёдза",
        "hamburger" to "Гамбургер",
        "hot_and_sour_soup" to "Кисло-острый суп",
        "hot_dog" to "Хот-дог",
        "huevos_rancheros" to "Уэвос ранчерос",
        "hummus" to "Хумус",
        "ice_cream" to "Мороженое",
        "lasagna" to "Лазанья",
        "lobster_bisque" to "Биск из омара",
        "lobster_roll_sandwich" to "Сэндвич с омаром",
        "macaroni_and_cheese" to "Макароны с сыром",
        "macarons" to "Макаронс",
        "miso_soup" to "Мисо-суп",
        "mussels" to "Мидии",
        "nachos" to "Начос",
        "omelette" to "Омлет",
        "onion_rings" to "Луковые кольца",
        "oysters" to "Устрицы",
        "pad_thai" to "Пад-тай",
        "paella" to "Паэлья",
        "pancakes" to "Блины",
        "panna_cotta" to "Панна-котта",
        "peking_duck" to "Утка по-пекински",
        "pho" to "Фо",
        "pizza" to "Пицца",
        "pork_chop" to "Свиная отбивная",
        "poutine" to "Путин",
        "prime_rib" to "Ростбиф",
        "pulled_pork_sandwich" to "Сэндвич с тянутой свининой",
        "ramen" to "Лапша рамен",
        "ravioli" to "Равиоли",
        "red_velvet_cake" to "Красный бархат",
        "risotto" to "Ризотто",
        "samosa" to "Самоса",
        "sashimi" to "Сашими",
        "scallops" to "Гребешки",
        "seaweed_salad" to "Салат из морской капусты",
        "semolina" to "Манная каша",
        "shrimp_and_grits" to "Креветки с мамалыгой",
        "spaghetti_bolognese" to "Спагетти болоньезе",
        "spaghetti_carbonara" to "Спагетти карбонара",
        "spring_rolls" to "Спринг-роллы",
        "steak" to "Стейк",
        "strawberry_shortcake" to "Клубничный пирог",
        "sushi" to "Суши",
        "tacos" to "Тако",
        "takoyaki" to "Такояки",
        "tiramisu" to "Тирамису",
        "tuna_tartare" to "Тартар из тунца",
        "waffles" to "Вафли"
    )

    val reverseMap: Map<String, String> = map.entries.associate { (en, ru) ->
        ru.lowercase() to en
    }

    fun getEnglishName(russianName: String): String? {
        return map.entries.find { it.value.equals(russianName, ignoreCase = true) }?.key
    }

    fun getEnglishDisplayName(russianName: String): String? {
        return getEnglishName(russianName)?.replace("_", " ")
    }

    fun getEnglishOrTransliteratedName(russianName: String): String {
        return getEnglishDisplayName(russianName) ?: transliterate(russianName)
    }

    fun transliterate(value: String): String {
        return value.map { ch ->
            val lower = ch.lowercaseChar()
            val tr = translitMap[lower]
            when {
                tr != null && ch.isUpperCase() -> tr.replaceFirstChar { it.uppercase() }
                tr != null -> tr
                ch == '№' -> "No"
                else -> ch.toString()
            }
        }.joinToString("")
    }

    fun getLocalizedName(russianName: String, languageCode: String): String {
        return if (languageCode.equals("ru", ignoreCase = true)) {
            russianName
        } else {
            getEnglishOrTransliteratedName(russianName)
        }
    }
}


