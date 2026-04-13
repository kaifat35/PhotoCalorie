package com.stafeewa.photocalorie.app.data.repository

import com.stafeewa.photocalorie.app.data.local.ProductDao
import com.stafeewa.photocalorie.app.data.mapper.toDbModel
import com.stafeewa.photocalorie.app.data.mapper.toDomain
import com.stafeewa.photocalorie.app.domain.entity.MealType
import com.stafeewa.photocalorie.app.domain.entity.Product
import com.stafeewa.photocalorie.app.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
) : ProductRepository {

    override fun searchProducts(query: String): Flow<List<Product>> {
        val normalizedQuery = query.trim().lowercase()
        return productDao.searchProducts(query.trim(), normalizedQuery).map { dbModels ->
            dbModels.map { it.toDomain() }
        }
    }

    override fun getProductsByMealType(mealType: MealType): Flow<List<Product>> {
        return productDao.getProductsByMealType(mealType).map { dbModels ->
            dbModels.map { it.toDomain() }
        }
    }

    override suspend fun getProductById(id: Long): Product? {
        return productDao.getProductById(id)?.toDomain()
    }

    override suspend fun addProduct(product: Product) {
        productDao.insertProduct(product.toDbModel())
    }

    override suspend fun addProducts(products: List<Product>) {
        productDao.insertProducts(products.map { it.toDbModel() })
    }

    override suspend fun initDefaultProducts() {
        val existingProducts = productDao.getProductsByMealType(MealType.BREAKFAST).firstOrNull()
        if (existingProducts.isNullOrEmpty()) {
            addProducts(getDefaultProducts())
        }
    }

    private fun getDefaultProducts(): List<Product> {
        val breakfastProducts = createProductsForMeal(
            mealType = MealType.BREAKFAST,
            names = BREAKFAST_DISHES,
            baseProtein = 9.0,
            baseFat = 7.0,
            baseCarbs = 20.0,
            defaultPortion = 180.0
        )
        val lunchProducts = createProductsForMeal(
            mealType = MealType.LUNCH,
            names = LUNCH_DISHES,
            baseProtein = 13.0,
            baseFat = 9.0,
            baseCarbs = 18.0,
            defaultPortion = 260.0
        )
        val dinnerProducts = createProductsForMeal(
            mealType = MealType.DINNER,
            names = DINNER_DISHES,
            baseProtein = 14.0,
            baseFat = 7.0,
            baseCarbs = 12.0,
            defaultPortion = 220.0
        )
        val snackProducts = createProductsForMeal(
            mealType = MealType.SNACK,
            names = SNACK_DISHES,
            baseProtein = 6.0,
            baseFat = 6.0,
            baseCarbs = 16.0,
            defaultPortion = 90.0
        )

        return (breakfastProducts + lunchProducts + dinnerProducts + snackProducts).map { product ->
            product.copy(keywords = buildMlKeywords(product.name))
        }
    }

    private fun createProductsForMeal(
        mealType: MealType,
        names: List<String>,
        baseProtein: Double,
        baseFat: Double,
        baseCarbs: Double,
        defaultPortion: Double
    ): List<Product> {
        return names.mapIndexed { index, name ->
            val protein = (baseProtein + ((index % 9) - 4) * 0.7).coerceAtLeast(1.0)
            val fat = (baseFat + ((index % 7) - 3) * 0.6).coerceAtLeast(0.5)
            val carbs = (baseCarbs + ((index % 11) - 5) * 1.3).coerceAtLeast(2.0)
            val calories = (protein * 4 + fat * 9 + carbs * 4)

            Product(
                name = name,
                mealType = mealType,
                defaultPortion = defaultPortion,
                proteinPer100g = protein,
                fatPer100g = fat,
                carbsPer100g = carbs,
                caloriesPer100g = calories
            )
        }
    }

    private val BREAKFAST_DISHES: List<String> = listOf(
        "Овсяная каша на молоке",
        "Овсяная каша на воде",
        "Гречневая каша",
        "Рисовая каша на молоке",
        "Пшённая каша",
        "Манная каша",
        "Кукурузная каша",
        "Перловая каша",
        "Булгур на завтрак",
        "Киноа с фруктами",
        "Яичница из двух яиц",
        "Яичница с помидорами",
        "Яичница с беконом",
        "Омлет классический",
        "Омлет с сыром",
        "Омлет с овощами",
        "Омлет с грибами",
        "Скрэмбл из яиц",
        "Яйца пашот",
        "Варёные яйца",
        "Сырники из творога",
        "Творожная запеканка",
        "Творог 5%",
        "Творог с ягодами",
        "Творог с мёдом",
        "Творог с бананом",
        "Ленивые вареники",
        "Йогурт греческий",
        "Йогурт с гранолой",
        "Кефир с отрубями",
        "Блины с творогом",
        "Блины с мёдом",
        "Блины с ягодами",
        "Оладьи на кефире",
        "Панкейки",
        "Вафли домашние",
        "Гренки с яйцом",
        "Тост с авокадо",
        "Тост с сыром",
        "Тост с арахисовой пастой",
        "Бутерброд с маслом и сыром",
        "Бутерброд с ветчиной",
        "Бутерброд с индейкой",
        "Сэндвич с яйцом",
        "Сэндвич с курицей",
        "Буррито с яйцом",
        "Кесадилья с сыром",
        "Лаваш с творожным сыром",
        "Ролл с омлетом",
        "Круассан",
        "Мюсли с молоком",
        "Гранола с йогуртом",
        "Хлопья с молоком",
        "Пшеница с молоком",
        "Чиа-пудинг",
        "Пудинг рисовый",
        "Смузи банановый",
        "Смузи ягодный",
        "Смузи протеиновый",
        "Фруктовый салат",
        "Шакшука",
        "Хачапури по-аджарски",
        "Каша Дружба",
        "Сырная каша",
        "Каша с тыквой",
        "Запечённые яблоки с творогом",
        "Запеканка из овсянки",
        "Маффин овсяный",
        "Омлет белковый",
        "Протеиновый блин",
        "Рисовые хлебцы с творожным сыром",
        "Бутерброд с красной рыбой",
        "Брускетта с томатами",
        "Брускетта с авокадо",
        "Каша из полбы",
        "Паста из творога и зелени",
        "Салат с яйцом и огурцом",
        "Киноа с йогуртом",
        "Пшеничная каша",
        "Гречка с молоком",
        "Суп-пюре тыквенный",
        "Бульон куриный с яйцом",
        "Салат из творога и зелени",
        "Сырная лепёшка",
        "Лепёшка с яйцом",
        "Тортилья с омлетом",
        "Запечённый батат",
        "Творожный крем",
        "Какао с молоком",
        "Ряженка с гранолой",
        "Пита с омлетом",
        "Бейгл с лососем",
        "Бейгл с творожным сыром",
        "Сэндвич с тунцом",
        "Овсяноблин",
        "Сырный омлет",
        "Яичный салат",
        "Паштет из тунца на тосте",
        "Каша с сухофруктами",
        "Яблочный штрудель"
    )

    private val LUNCH_DISHES: List<String> = listOf(
        "Борщ с говядиной",
        "Борщ постный",
        "Щи из свежей капусты",
        "Щи из квашеной капусты",
        "Солянка мясная",
        "Рассольник",
        "Харчо",
        "Уха",
        "Суп лапша куриный",
        "Грибной суп",
        "Гороховый суп",
        "Чечевичный суп",
        "Суп-пюре из тыквы",
        "Суп-пюре из брокколи",
        "Минестроне",
        "Томатный суп",
        "Куриный бульон",
        "Свекольник",
        "Окрошка на кефире",
        "Окрошка на квасе",
        "Плов с курицей",
        "Плов с говядиной",
        "Рис с овощами и курицей",
        "Гречка с курицей",
        "Гречка с говядиной",
        "Булгур с индейкой",
        "Киноа с овощами",
        "Перловка с грибами",
        "Пшено с курицей",
        "Кускус с овощами",
        "Макароны по-флотски",
        "Спагетти болоньезе",
        "Паста карбонара",
        "Паста с курицей",
        "Лапша удон с курицей",
        "Лапша соба с овощами",
        "Лазанья мясная",
        "Каннеллони с фаршем",
        "Равиоли с сыром",
        "Ньокки с соусом",
        "Котлеты куриные с пюре",
        "Котлеты говяжьи с гречкой",
        "Тефтели с рисом",
        "Фрикадельки в соусе",
        "Голубцы с мясом",
        "Перец фаршированный",
        "Пельмени отварные",
        "Вареники с картофелем",
        "Манты",
        "Хинкали",
        "Куриная грудка запечённая",
        "Индейка запечённая",
        "Говядина тушёная",
        "Свинина запечённая",
        "Рыба запечённая",
        "Рыба на пару",
        "Лосось с рисом",
        "Треска с овощами",
        "Минтай тушёный",
        "Куриные бёдра запечённые",
        "Жаркое по-домашнему",
        "Рагу овощное с мясом",
        "Картофель тушёный с мясом",
        "Картофельное пюре с котлетой",
        "Картофель запечённый с курицей",
        "Драники со сметаной",
        "Запеканка картофельная",
        "Запеканка мясная",
        "Жульен с курицей",
        "Бефстроганов",
        "Салат Цезарь с курицей",
        "Салат Оливье",
        "Винегрет",
        "Салат Греческий",
        "Салат с тунцом",
        "Салат с курицей и овощами",
        "Салат с киноа",
        "Салат крабовый",
        "Салат с фасолью",
        "Салат с авокадо",
        "Шаурма с курицей",
        "Бургер куриный",
        "Бургер говяжий",
        "Кесадилья с курицей",
        "Тако с говядиной",
        "Пицца Маргарита",
        "Пицца с курицей",
        "Сэндвич клубный",
        "Панини с индейкой",
        "Ролл с курицей",
        "Рис с морепродуктами",
        "Паэлья",
        "Поке с лососем",
        "Поке с тунцом",
        "Курица карри с рисом",
        "Чили кон карне",
        "Фахитас с курицей",
        "Кебаб с овощами",
        "Шашлык куриный",
        "Шашлык свиной"
    )

    private val DINNER_DISHES: List<String> = listOf(
        "Запечённая куриная грудка",
        "Куриное филе на пару",
        "Индейка с овощами",
        "Треска запечённая",
        "Лосось на гриле",
        "Минтай на пару",
        "Омлет с овощами",
        "Омлет белковый",
        "Яйца варёные",
        "Яичница с овощами",
        "Творог 5%",
        "Творог с зеленью",
        "Творожная запеканка",
        "Греческий йогурт",
        "Кефир",
        "Ряженка",
        "Сырники запечённые",
        "Пудинг творожный",
        "Творог с ягодами",
        "Йогурт натуральный",
        "Салат овощной",
        "Салат Греческий",
        "Салат с тунцом",
        "Салат с курицей",
        "Салат с авокадо",
        "Салат с креветками",
        "Салат из капусты",
        "Салат из огурцов и помидоров",
        "Салат с рукколой",
        "Салат с фасолью",
        "Овощное рагу",
        "Тушёная капуста",
        "Брокколи на пару",
        "Цветная капуста запечённая",
        "Кабачки гриль",
        "Баклажаны запечённые",
        "Стручковая фасоль",
        "Шпинат тушёный",
        "Овощи гриль",
        "Тыква запечённая",
        "Гречка с грибами",
        "Рис бурый с овощами",
        "Булгур с овощами",
        "Киноа с овощами",
        "Чечевица тушёная",
        "Нут с овощами",
        "Перловка с овощами",
        "Кускус с зеленью",
        "Пшено с тыквой",
        "Картофель запечённый",
        "Куриные котлеты на пару",
        "Индейка тефтели",
        "Рыбные котлеты",
        "Фрикадельки из индейки",
        "Говядина отварная",
        "Телятина тушёная",
        "Кролик тушёный",
        "Курица в духовке",
        "Филе индейки гриль",
        "Говяжий язык отварной",
        "Суп куриный лёгкий",
        "Овощной суп",
        "Суп-пюре из брокколи",
        "Томатный суп",
        "Рыбный суп",
        "Грибной суп лёгкий",
        "Суп из чечевицы",
        "Минестроне лёгкий",
        "Бульон с зеленью",
        "Суп с фрикадельками",
        "Паста из цельнозерновых с овощами",
        "Спагетти с томатами",
        "Рисовая лапша с овощами",
        "Лапша соба с тофу",
        "Поке с курицей",
        "Поке с овощами",
        "Ролл с лососем",
        "Ролл с огурцом",
        "Суши с тунцом",
        "Сашими лосось",
        "Шакшука",
        "Фриттата овощная",
        "Кесадилья с овощами",
        "Лаваш с курицей",
        "Сэндвич с индейкой",
        "Пита с хумусом",
        "Хумус с овощами",
        "Авокадо-тост",
        "Тост с лососем",
        "Тост с творожным сыром",
        "Запечённый батат",
        "Рататуй",
        "Киноа с креветками",
        "Креветки на гриле",
        "Кальмар тушёный",
        "Мидии в томатном соусе",
        "Тофу с овощами",
        "Темпе с овощами",
        "Сыр халлуми гриль",
        "Салат с моцареллой"
    )

    private val SNACK_DISHES: List<String> = listOf(
        "Яблоко",
        "Банан",
        "Груша",
        "Апельсин",
        "Мандарин",
        "Грейпфрут",
        "Киви",
        "Манго",
        "Ананас",
        "Персик",
        "Нектарин",
        "Слива",
        "Абрикос",
        "Виноград",
        "Клубника",
        "Малина",
        "Черника",
        "Ежевика",
        "Смородина",
        "Арбуз",
        "Дыня",
        "Сухофрукты микс",
        "Курага",
        "Чернослив",
        "Изюм",
        "Финики",
        "Инжир сушёный",
        "Орехи микс",
        "Миндаль",
        "Грецкий орех",
        "Фундук",
        "Кешью",
        "Фисташки",
        "Арахис",
        "Семечки подсолнечника",
        "Семечки тыквенные",
        "Йогурт питьевой",
        "Йогурт греческий",
        "Кефир",
        "Ряженка",
        "Творог мягкий",
        "Творожок без сахара",
        "Протеиновый батончик",
        "Злаковый батончик",
        "Фруктовый батончик",
        "Рисовые хлебцы",
        "Хлебцы с отрубями",
        "Гранола порция",
        "Мюсли батончик",
        "Попкорн без масла",
        "Горький шоколад",
        "Пастила",
        "Зефир",
        "Мармелад",
        "Печенье овсяное",
        "Крекеры цельнозерновые",
        "Галеты",
        "Смузи ягодный",
        "Смузи банановый",
        "Смузи зелёный",
        "Фруктовый салат",
        "Овощные палочки",
        "Морковь с хумусом",
        "Огурцы с хумусом",
        "Сельдерей с арахисовой пастой",
        "Авокадо половина",
        "Тост цельнозерновой",
        "Сэндвич мини с индейкой",
        "Брускетта с томатами",
        "Мини-ролл с лососем",
        "Яйцо варёное",
        "Омлет маффин",
        "Сыр твёрдый",
        "Сыр моцарелла",
        "Сыр адыгейский",
        "Хумус",
        "Гуакамоле",
        "Пудинг чиа",
        "Творожный мусс",
        "Протеиновый коктейль",
        "Молочный коктейль",
        "Какао",
        "Латте",
        "Капучино",
        "Чай с мёдом",
        "Компот домашний",
        "Морс ягодный",
        "Сок апельсиновый",
        "Сок яблочный",
        "Томатный сок",
        "Энергетический шарик",
        "Финик с орехом",
        "Банановые чипсы",
        "Яблочные чипсы",
        "Кокосовые чипсы",
        "Кукуруза отварная",
        "Эдамаме",
        "Мини-салат Цезарь",
        "Мини-салат Греческий",
        "Тунец в собственном соку"
    )

    private fun buildMlKeywords(name: String): List<String> {
        val normalizedName = name.lowercase()
        val base = mutableSetOf(name.lowercase())

        if ("каша" in normalizedName) base += listOf("porridge", "oatmeal", "каша")
        if ("овся" in normalizedName) base += listOf("oatmeal", "oats")
        if ("греч" in normalizedName) base += listOf("buckwheat")
        if ("рис" in normalizedName) base += listOf("rice")
        if ("яич" in normalizedName || "омлет" in normalizedName) base += listOf("egg", "omelette", "omelet")
        if ("твор" in normalizedName || "сырник" in normalizedName || "запеканка" in normalizedName) {
            base += listOf("cottage cheese", "curd", "cheesecake")
        }
        if ("йогурт" in normalizedName) base += listOf("yogurt", "yoghurt")
        if ("блин" in normalizedName) base += listOf("pancake", "crepe")
        if ("борщ" in normalizedName) base += listOf("borscht", "beet soup")
        if ("суп" in normalizedName) base += listOf("soup")
        if ("кур" in normalizedName) base += listOf("chicken")
        if ("рыб" in normalizedName) base += listOf("fish", "seafood")
        if ("салат" in normalizedName) base += listOf("salad")
        if ("цезарь" in normalizedName) base += listOf("caesar")
        if ("гречес" in normalizedName) base += listOf("greek salad", "greek")
        if ("котлет" in normalizedName) base += listOf("cutlet", "patty")
        if ("плов" in normalizedName) base += listOf("pilaf", "pilau")
        if ("макарон" in normalizedName) base += listOf("pasta", "spaghetti", "noodles")
        if ("пюре" in normalizedName || "картофель" in normalizedName) base += listOf("mashed potato", "potato")
        if ("овощ" in normalizedName || "рагу" in normalizedName || "капуст" in normalizedName) {
            base += listOf("vegetables", "stew")
        }
        if ("яблок" in normalizedName || "груш" in normalizedName || "банан" in normalizedName) {
            base += listOf("fruit", "apple", "banana", "pear")
        }
        if ("йогурт" in normalizedName || "кефир" in normalizedName || "ряженка" in normalizedName) {
            base += listOf("dairy", "fermented milk")
        }
        if ("орех" in normalizedName || "миндал" in normalizedName || "кешью" in normalizedName) {
            base += listOf("nuts", "almond", "cashew")
        }
        if ("батончик" in normalizedName || "гранола" in normalizedName || "мюсли" in normalizedName) {
            base += listOf("bar", "granola", "muesli")
        }

        return base.toList()
    }
}