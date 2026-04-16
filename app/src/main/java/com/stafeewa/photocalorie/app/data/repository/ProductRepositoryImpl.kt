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
            names = breakfastDishes,
            defaultPortion = 180.0
        )
        val lunchProducts = createProductsForMeal(
            mealType = MealType.LUNCH,
            names = lunchDishes,
            defaultPortion = 260.0
        )
        val dinnerProducts = createProductsForMeal(
            mealType = MealType.DINNER,
            names = dinnerDishes,
            defaultPortion = 220.0
        )
        val snackProducts = createProductsForMeal(
            mealType = MealType.SNACK,
            names = snackDishes,
            defaultPortion = 90.0
        )

        return (breakfastProducts + lunchProducts + dinnerProducts + snackProducts).map { product ->
            product.copy(keywords = buildMlKeywords(product.name))
        }
    }

    private fun createProductsForMeal(
        mealType: MealType,
        names: List<String>,
        defaultPortion: Double
    ): List<Product> {
        return names.map { name ->
            val (protein, fat, carbs) = getRealisticNutrition(name, mealType)
            val calories = protein * 4 + fat * 9 + carbs * 4

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

    private fun getRealisticNutrition(
        name: String,
        mealType: MealType
    ): Triple<Double, Double, Double> {
        val normalized = name.lowercase()

        // Категоризация блюд с типичными диапазонами (белки, жиры, углеводы)
        when {
            // === Завтраки ===
            "каша" in normalized -> {
                if ("молок" in normalized) return Triple(5.0, 4.0, 18.0)   // каша на молоке
                return Triple(3.5, 1.5, 15.0)                              // каша на воде
            }

            "гречневая" in normalized || "гречка" in normalized -> return Triple(4.5, 2.5, 20.0)
            "рисовая" in normalized -> return Triple(3.0, 1.0, 22.0)
            "овсяная" in normalized -> return Triple(3.5, 2.0, 16.0)
            "пшённая" in normalized -> return Triple(3.5, 2.0, 17.0)
            "манная" in normalized -> return Triple(3.0, 1.5, 20.0)
            "кукурузная" in normalized -> return Triple(2.5, 1.2, 21.0)
            "перловая" in normalized -> return Triple(3.0, 1.2, 18.0)
            "булгур" in normalized -> return Triple(4.0, 1.5, 18.0)
            "киноа" in normalized -> return Triple(4.5, 2.0, 18.0)

            "яичница" in normalized || "омлет" in normalized || "яйца" in normalized -> {
                if ("сыр" in normalized) return Triple(12.0, 12.0, 2.0)
                if ("бекон" in normalized) return Triple(13.0, 15.0, 1.5)
                if ("овощ" in normalized) return Triple(9.0, 8.0, 4.0)
                return Triple(10.0, 9.0, 1.5)   // классическая яичница/омлет
            }

            "варёные яйца" in normalized -> return Triple(12.5, 11.0, 0.7)

            "творог" in normalized || "сырник" in normalized || "творожная запеканка" in normalized -> {
                if ("запеканка" in normalized) return Triple(12.0, 6.0, 12.0)
                if ("сырник" in normalized) return Triple(11.0, 9.0, 15.0)
                return Triple(12.0, 5.0, 3.0)   // творог 5%
            }

            "йогурт" in normalized -> {
                if ("греческий" in normalized) return Triple(10.0, 0.5, 4.0)
                return Triple(3.5, 1.5, 6.0)
            }

            "блин" in normalized || "оладьи" in normalized || "панкейк" in normalized -> {
                if ("творог" in normalized) return Triple(8.0, 6.0, 20.0)
                return Triple(5.5, 5.0, 22.0)
            }

            "тост" in normalized || "брускетта" in normalized -> {
                if ("авокадо" in normalized) return Triple(3.0, 9.0, 14.0)
                if ("сыр" in normalized) return Triple(6.0, 7.0, 15.0)
                return Triple(4.0, 3.0, 18.0)
            }

            "бутерброд" in normalized || "сэндвич" in normalized -> {
                if ("курица" in normalized) return Triple(12.0, 6.0, 16.0)
                if ("рыба" in normalized || "лосось" in normalized) return Triple(13.0, 8.0, 15.0)
                return Triple(8.0, 7.0, 18.0)
            }

            "смузи" in normalized -> {
                if ("банан" in normalized) return Triple(2.0, 0.5, 14.0)
                if ("ягод" in normalized) return Triple(1.5, 0.3, 12.0)
                return Triple(2.0, 0.5, 10.0)
            }

            "шакшука" in normalized -> return Triple(8.0, 7.0, 6.0)

            // === Обеды (супы, горячее, салаты) ===
            "борщ" in normalized -> return Triple(3.0, 2.5, 5.0)
            "щи" in normalized -> return Triple(2.5, 2.0, 4.5)
            "солянка" in normalized -> return Triple(4.5, 3.0, 3.0)
            "рассольник" in normalized -> return Triple(2.8, 2.2, 4.0)
            "харчо" in normalized -> return Triple(4.0, 2.5, 5.0)
            "уха" in normalized -> return Triple(3.5, 1.5, 2.0)
            "суп" in normalized -> {
                if ("кури" in normalized) return Triple(3.0, 2.0, 3.5)
                if ("гриб" in normalized) return Triple(2.0, 1.5, 3.0)
                if ("горохов" in normalized) return Triple(4.0, 1.5, 8.0)
                if ("чечевич" in normalized) return Triple(5.0, 1.0, 10.0)
                if ("пюре" in normalized) return Triple(2.5, 2.0, 6.0)
                return Triple(2.5, 1.8, 4.0)
            }

            "окрошка" in normalized -> return Triple(3.0, 2.0, 5.0)

            "плов" in normalized -> {
                if ("куриц" in normalized) return Triple(7.0, 6.0, 20.0)
                if ("говяд" in normalized) return Triple(8.0, 7.0, 20.0)
                return Triple(7.5, 6.5, 20.0)
            }

            "гречка" in normalized && ("куриц" in normalized || "говяд" in normalized) -> {
                return Triple(8.0, 5.0, 18.0)
            }

            "макарон" in normalized || "паста" in normalized || "спагетти" in normalized -> {
                if ("болоньезе" in normalized) return Triple(8.0, 6.0, 22.0)
                if ("карбонара" in normalized) return Triple(9.0, 12.0, 20.0)
                if ("куриц" in normalized) return Triple(10.0, 5.0, 22.0)
                return Triple(6.0, 4.0, 25.0)
            }

            "котлет" in normalized || "тефтели" in normalized || "фрикадельки" in normalized -> {
                if ("куриц" in normalized) return Triple(15.0, 8.0, 5.0)
                if ("говяд" in normalized) return Triple(14.0, 10.0, 4.0)
                return Triple(13.0, 9.0, 5.0)
            }

            "голубцы" in normalized -> return Triple(6.0, 4.0, 12.0)
            "перец фаршированный" in normalized -> return Triple(5.0, 4.0, 8.0)
            "пельмени" in normalized -> return Triple(9.0, 8.0, 18.0)
            "вареники" in normalized -> {
                if ("картофель" in normalized) return Triple(4.0, 3.0, 20.0)
                return Triple(6.0, 5.0, 22.0)
            }

            "манты" in normalized -> return Triple(10.0, 8.0, 16.0)

            "куриная грудка" in normalized -> return Triple(23.0, 2.0, 0.0)
            "индейка" in normalized -> return Triple(22.0, 2.5, 0.0)
            "говядина" in normalized -> return Triple(20.0, 8.0, 0.0)
            "свинина" in normalized -> return Triple(16.0, 15.0, 0.0)
            "лосось" in normalized || "семга" in normalized -> return Triple(20.0, 13.0, 0.0)
            "треска" in normalized -> return Triple(18.0, 1.0, 0.0)
            "минтай" in normalized -> return Triple(16.0, 1.0, 0.0)
            "рыба" in normalized -> return Triple(17.0, 5.0, 0.0)

            "картофельное пюре" in normalized -> return Triple(2.0, 3.0, 14.0)
            "картофель запечённый" in normalized -> return Triple(2.0, 0.5, 15.0)
            "картошка фри" in normalized -> return Triple(2.5, 10.0, 28.0)
            "драники" in normalized -> return Triple(3.0, 8.0, 20.0)

            "салат" in normalized -> {
                if ("цезарь" in normalized) return Triple(8.0, 12.0, 5.0)
                if ("оливье" in normalized) return Triple(5.0, 8.0, 7.0)
                if ("греческий" in normalized) return Triple(4.0, 8.0, 4.0)
                if ("тунец" in normalized) return Triple(12.0, 6.0, 3.0)
                if ("крабовый" in normalized) return Triple(6.0, 5.0, 8.0)
                if ("овощной" in normalized) return Triple(1.5, 0.5, 5.0)
                return Triple(3.0, 4.0, 6.0)
            }

            // === Ужины (лёгкие блюда) ===
            "запечённая куриная грудка" in normalized -> return Triple(23.0, 2.0, 0.0)
            "куриное филе на пару" in normalized -> return Triple(24.0, 1.5, 0.0)
            "творог 5%" in normalized -> return Triple(12.0, 5.0, 3.0)
            "рыба на пару" in normalized -> return Triple(18.0, 2.0, 0.0)
            "овощное рагу" in normalized -> return Triple(1.5, 1.0, 6.0)
            "тушёная капуста" in normalized -> return Triple(1.5, 0.5, 5.0)
            "брокколи на пару" in normalized -> return Triple(3.0, 0.4, 4.0)
            "цветная капуста запечённая" in normalized -> return Triple(2.5, 0.5, 4.5)
            "кабачки гриль" in normalized -> return Triple(1.2, 0.3, 3.5)
            "баклажаны запечённые" in normalized -> return Triple(1.0, 0.2, 5.0)
            "стручковая фасоль" in normalized -> return Triple(2.0, 0.2, 5.0)
            "шпинат тушёный" in normalized -> return Triple(2.5, 0.5, 1.5)

            // === Перекусы (фрукты, орехи, молочка) ===
            "яблоко" in normalized -> return Triple(0.3, 0.2, 14.0)
            "банан" in normalized -> return Triple(1.1, 0.3, 23.0)
            "груша" in normalized -> return Triple(0.4, 0.2, 15.0)
            "апельсин" in normalized -> return Triple(0.9, 0.2, 11.0)
            "мандарин" in normalized -> return Triple(0.8, 0.2, 10.0)
            "киви" in normalized -> return Triple(1.0, 0.5, 12.0)
            "ананас" in normalized -> return Triple(0.5, 0.1, 13.0)
            "клубника" in normalized -> return Triple(0.7, 0.3, 7.0)
            "малина" in normalized -> return Triple(1.2, 0.6, 6.0)
            "виноград" in normalized -> return Triple(0.6, 0.2, 17.0)
            "арбуз" in normalized -> return Triple(0.6, 0.2, 8.0)
            "дыня" in normalized -> return Triple(0.8, 0.2, 9.0)

            "сухофрукты" in normalized -> return Triple(2.0, 0.5, 55.0)
            "курага" in normalized -> return Triple(1.5, 0.2, 55.0)
            "чернослив" in normalized -> return Triple(2.0, 0.5, 57.0)
            "изюм" in normalized -> return Triple(2.5, 0.5, 66.0)
            "финики" in normalized -> return Triple(2.0, 0.2, 70.0)

            "орехи" in normalized -> {
                if ("миндаль" in normalized) return Triple(21.0, 50.0, 22.0)
                if ("грецкий" in normalized) return Triple(15.0, 65.0, 14.0)
                if ("кешью" in normalized) return Triple(18.0, 44.0, 30.0)
                if ("арахис" in normalized) return Triple(26.0, 49.0, 16.0)
                return Triple(15.0, 50.0, 20.0)
            }

            "семечки" in normalized -> {
                if ("подсолнечника" in normalized) return Triple(21.0, 51.0, 20.0)
                return Triple(20.0, 49.0, 22.0)
            }

            "протеиновый батончик" in normalized -> return Triple(25.0, 10.0, 35.0)
            "злаковый батончик" in normalized -> return Triple(5.0, 5.0, 30.0)
            "рисовые хлебцы" in normalized -> return Triple(3.0, 1.0, 28.0)
            "попкорн" in normalized -> return Triple(4.0, 2.0, 20.0)
            "горький шоколад" in normalized -> return Triple(8.0, 40.0, 35.0)

            // === Напитки ===
            "сок" in normalized -> {
                if ("апельсиновый" in normalized) return Triple(0.7, 0.2, 11.0)
                if ("яблочный" in normalized) return Triple(0.2, 0.1, 11.0)
                return Triple(0.5, 0.1, 10.0)
            }

            "компот" in normalized -> return Triple(0.2, 0.1, 8.0)
            "морс" in normalized -> return Triple(0.1, 0.0, 7.0)
            "какао" in normalized -> return Triple(3.5, 2.5, 10.0)
            "латте" in normalized -> return Triple(3.5, 2.0, 4.0)
            "капучино" in normalized -> return Triple(3.0, 1.5, 3.0)

            else -> {
                // fallback по типу приёма пищи
                return when (mealType) {
                    MealType.BREAKFAST -> Triple(7.0, 6.0, 15.0)
                    MealType.LUNCH -> Triple(10.0, 8.0, 12.0)
                    MealType.DINNER -> Triple(12.0, 5.0, 8.0)
                    MealType.SNACK -> Triple(5.0, 4.0, 15.0)
                }
            }
        }
    }

    // Списки блюд (оставлены без изменений, очень длинные)
    private val breakfastDishes: List<String> = listOf(
        "Картошка фри", "Овсяная каша на молоке", "Овсяная каша на воде", "Гречневая каша",
        "Рисовая каша на молоке", "Пшённая каша", "Манная каша", "Кукурузная каша",
        "Перловая каша", "Булгур на завтрак", "Киноа с фруктами", "Яичница из двух яиц",
        "Яичница с помидорами", "Яичница с беконом", "Омлет классический", "Омлет с сыром",
        "Омлет с овощами", "Омлет с грибами", "Скрэмбл из яиц", "Яйца пашот",
        "Варёные яйца", "Сырники из творога", "Творожная запеканка", "Творог 5%",
        "Творог с ягодами", "Творог с мёдом", "Творог с бананом", "Ленивые вареники",
        "Йогурт греческий", "Йогурт с гранолой", "Кефир с отрубями", "Блины с творогом",
        "Блины с мёдом", "Блины с ягодами", "Оладьи на кефире", "Панкейки",
        "Вафли домашние", "Гренки с яйцом", "Тост с авокадо", "Тост с сыром",
        "Тост с арахисовой пастой", "Бутерброд с маслом и сыром", "Бутерброд с ветчиной",
        "Бутерброд с индейкой", "Сэндвич с яйцом", "Сэндвич с курицей", "Буррито с яйцом",
        "Кесадилья с сыром", "Лаваш с творожным сыром", "Ролл с омлетом", "Круассан",
        "Мюсли с молоком", "Гранола с йогуртом", "Хлопья с молоком", "Пшеница с молоком",
        "Чиа-пудинг", "Пудинг рисовый", "Смузи банановый", "Смузи ягодный",
        "Смузи протеиновый", "Фруктовый салат", "Шакшука", "Хачапури по-аджарски",
        "Каша Дружба", "Сырная каша", "Каша с тыквой", "Запечённые яблоки с творогом",
        "Запеканка из овсянки", "Маффин овсяный", "Омлет белковый", "Протеиновый блин",
        "Рисовые хлебцы с творожным сыром", "Бутерброд с красной рыбой",
        "Брускетта с томатами", "Брускетта с авокадо", "Каша из полбы",
        "Паста из творога и зелени", "Салат с яйцом и огурцом", "Киноа с йогуртом",
        "Пшеничная каша", "Гречка с молоком", "Суп-пюре тыквенный",
        "Бульон куриный с яйцом", "Салат из творога и зелени", "Сырная лепёшка",
        "Лепёшка с яйцом", "Тортилья с омлетом", "Запечённый батат", "Творожный крем",
        "Какао с молоком", "Ряженка с гранолой", "Пита с омлетом", "Бейгл с лососем",
        "Бейгл с творожным сыром", "Сэндвич с тунцом", "Овсяноблин", "Сырный омлет",
        "Яичный салат", "Паштет из тунца на тосте", "Каша с сухофруктами", "Яблочный штрудель"
    )

    private val lunchDishes: List<String> = listOf(
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

    private val dinnerDishes: List<String> = listOf(
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
        "Картошка фри",
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

    private val snackDishes: List<String> = listOf(
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
        if ("яич" in normalizedName || "омлет" in normalizedName) base += listOf(
            "egg",
            "omelette",
            "omelet"
        )
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
        if ("пюре" in normalizedName || "картофель" in normalizedName) base += listOf(
            "mashed potato",
            "potato"
        )
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