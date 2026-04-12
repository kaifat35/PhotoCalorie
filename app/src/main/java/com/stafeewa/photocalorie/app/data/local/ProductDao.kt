package com.stafeewa.photocalorie.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stafeewa.photocalorie.app.domain.entity.MealType
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductDbModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductDbModel>)

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' ORDER BY name LIMIT 10")
    fun searchProducts(query: String): Flow<List<ProductDbModel>>

    @Query("SELECT * FROM products WHERE mealType = :mealType ORDER BY name")
    fun getProductsByMealType(mealType: MealType): Flow<List<ProductDbModel>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductDbModel?

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}