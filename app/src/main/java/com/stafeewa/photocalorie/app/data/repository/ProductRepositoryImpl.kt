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
        return listOf(
            Product(
                name = "Овсяная каша на молоке",
                mealType = MealType.BREAKFAST,
                proteinPer100g = 3.5,
                fatPer100g = 3.2,
                carbsPer100g = 14.0,
                caloriesPer100g = 98.0
            ),
            Product(
                name = "Гречневая каша",
                mealType = MealType.BREAKFAST,
                proteinPer100g = 4.5,
                fatPer100g = 2.3,
                carbsPer100g = 19.0,
                caloriesPer100g = 110.0
            ),
            Product(
                name = "Рисовая каша на молоке",
                mealType = MealType.BREAKFAST,
                proteinPer100g = 2.8,
                fatPer100g = 3.0,
                carbsPer100g = 16.0,
                caloriesPer100g = 97.0
            ),
            Product(
                name = "Яичница из 2 яиц",
                mealType = MealType.BREAKFAST,
                defaultPortion = 110.0,
                proteinPer100g = 12.6,
                fatPer100g = 10.6,
                carbsPer100g = 1.2,
                caloriesPer100g = 155.0
            ),
            Product(
                name = "Творог 5% с ягодами",
                mealType = MealType.BREAKFAST,
                proteinPer100g = 15.0,
                fatPer100g = 5.0,
                carbsPer100g = 8.0,
                caloriesPer100g = 137.0
            ),
            Product(
                name = "Сырники из творога",
                mealType = MealType.BREAKFAST,
                proteinPer100g = 13.5,
                fatPer100g = 9.0,
                carbsPer100g = 18.0,
                caloriesPer100g = 207.0
            ),
            Product(
                name = "Бутерброд с маслом и сыром",
                mealType = MealType.BREAKFAST,
                defaultPortion = 80.0,
                proteinPer100g = 12.0,
                fatPer100g = 15.0,
                carbsPer100g = 25.0,
                caloriesPer100g = 283.0
            ),
            Product(
                name = "Йогурт греческий 2%",
                mealType = MealType.BREAKFAST,
                proteinPer100g = 10.0,
                fatPer100g = 2.0,
                carbsPer100g = 5.0,
                caloriesPer100g = 78.0
            ),
            Product(
                name = "Мюсли с молоком",
                mealType = MealType.BREAKFAST,
                proteinPer100g = 7.0,
                fatPer100g = 6.0,
                carbsPer100g = 25.0,
                caloriesPer100g = 182.0
            ),
            Product(
                name = "Блины с творогом",
                mealType = MealType.BREAKFAST,
                defaultPortion = 150.0,
                proteinPer100g = 9.5,
                fatPer100g = 8.0,
                carbsPer100g = 28.0,
                caloriesPer100g = 222.0
            ),
            Product(
                name = "Борщ с говядиной",
                mealType = MealType.LUNCH,
                proteinPer100g = 4.5,
                fatPer100g = 3.0,
                carbsPer100g = 6.0,
                caloriesPer100g = 66.0
            ),
            Product(
                name = "Куриный суп с лапшой",
                mealType = MealType.LUNCH,
                proteinPer100g = 4.0,
                fatPer100g = 2.0,
                carbsPer100g = 7.0,
                caloriesPer100g = 60.0
            ),
            Product(
                name = "Гречка с курицей и овощами",
                mealType = MealType.LUNCH,
                proteinPer100g = 12.0,
                fatPer100g = 6.0,
                carbsPer100g = 18.0,
                caloriesPer100g = 174.0
            ),
            Product(
                name = "Рис с овощами и рыбой",
                mealType = MealType.LUNCH,
                proteinPer100g = 11.0,
                fatPer100g = 5.0,
                carbsPer100g = 20.0,
                caloriesPer100g = 169.0
            ),
            Product(
                name = "Макароны с фаршем по-флотски",
                mealType = MealType.LUNCH,
                proteinPer100g = 10.0,
                fatPer100g = 9.0,
                carbsPer100g = 22.0,
                caloriesPer100g = 209.0
            ),
            Product(
                name = "Плов с курицей",
                mealType = MealType.LUNCH,
                proteinPer100g = 9.5,
                fatPer100g = 7.0,
                carbsPer100g = 20.0,
                caloriesPer100g = 179.0
            ),
            Product(
                name = "Салат Цезарь с курицей",
                mealType = MealType.LUNCH,
                proteinPer100g = 8.0,
                fatPer100g = 12.0,
                carbsPer100g = 6.0,
                caloriesPer100g = 164.0
            ),
            Product(
                name = "Картофельное пюре с котлетой",
                mealType = MealType.LUNCH,
                defaultPortion = 250.0,
                proteinPer100g = 8.0,
                fatPer100g = 10.0,
                carbsPer100g = 18.0,
                caloriesPer100g = 194.0
            ),
            Product(
                name = "Запечённая рыба с овощами",
                mealType = MealType.LUNCH,
                proteinPer100g = 16.0,
                fatPer100g = 7.0,
                carbsPer100g = 5.0,
                caloriesPer100g = 147.0
            ),
            Product(
                name = "Овощное рагу с говядиной",
                mealType = MealType.LUNCH,
                proteinPer100g = 9.0,
                fatPer100g = 5.0,
                carbsPer100g = 8.0,
                caloriesPer100g = 113.0
            ),
            Product(
                name = "Запечённая куриная грудка",
                mealType = MealType.DINNER,
                proteinPer100g = 22.0,
                fatPer100g = 3.0,
                carbsPer100g = 0.5,
                caloriesPer100g = 117.0
            ),
            Product(
                name = "Творожная запеканка",
                mealType = MealType.DINNER,
                proteinPer100g = 14.0,
                fatPer100g = 6.0,
                carbsPer100g = 12.0,
                caloriesPer100g = 158.0
            ),
            Product(
                name = "Омлет с овощами",
                mealType = MealType.DINNER,
                proteinPer100g = 9.0,
                fatPer100g = 7.0,
                carbsPer100g = 4.0,
                caloriesPer100g = 115.0
            ),
            Product(
                name = "Рыба на пару с овощами",
                mealType = MealType.DINNER,
                proteinPer100g = 18.0,
                fatPer100g = 5.0,
                carbsPer100g = 6.0,
                caloriesPer100g = 141.0
            ),
            Product(
                name = "Салат овощной с маслом",
                mealType = MealType.DINNER,
                proteinPer100g = 1.5,
                fatPer100g = 8.0,
                carbsPer100g = 5.0,
                caloriesPer100g = 94.0
            ),
            Product(
                name = "Гречка с грибами",
                mealType = MealType.DINNER,
                proteinPer100g = 5.0,
                fatPer100g = 4.0,
                carbsPer100g = 18.0,
                caloriesPer100g = 128.0
            ),
            Product(
                name = "Куриные котлеты на пару",
                mealType = MealType.DINNER,
                defaultPortion = 150.0,
                proteinPer100g = 18.0,
                fatPer100g = 5.0,
                carbsPer100g = 8.0,
                caloriesPer100g = 149.0
            ),
            Product(
                name = "Тушёная капуста с курицей",
                mealType = MealType.DINNER,
                proteinPer100g = 9.0,
                fatPer100g = 4.0,
                carbsPer100g = 7.0,
                caloriesPer100g = 100.0
            ),
            Product(
                name = "Салат Греческий",
                mealType = MealType.DINNER,
                proteinPer100g = 3.0,
                fatPer100g = 10.0,
                carbsPer100g = 4.0,
                caloriesPer100g = 118.0
            ),
            Product(
                name = "Творог с зеленью",
                mealType = MealType.DINNER,
                proteinPer100g = 16.0,
                fatPer100g = 5.0,
                carbsPer100g = 4.0,
                caloriesPer100g = 125.0
            )
        ).map { product ->
            product.copy(keywords = buildMlKeywords(product.name))
        }
    }

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

        return base.toList()
    }
}
