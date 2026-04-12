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
        return productDao.searchProducts(query).map { dbModels ->
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
            // ============ ЗАВТРАК ============
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

            // ============ ОБЕД ============
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

            // ============ УЖИН ============
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
        )
    }
}