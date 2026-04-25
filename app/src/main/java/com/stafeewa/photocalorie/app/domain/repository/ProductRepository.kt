package com.stafeewa.photocalorie.app.domain.repository

import com.stafeewa.photocalorie.app.domain.entity.MealType
import com.stafeewa.photocalorie.app.domain.entity.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun searchProducts(query: String): Flow<List<Product>>
    fun getProductsByMealType(mealType: MealType): Flow<List<Product>>
    suspend fun getProductById(id: Long): Product?
    suspend fun addProduct(product: Product)
    suspend fun addProducts(products: List<Product>)
    suspend fun initDefaultProducts()
    suspend fun getProductByName(name: String): Product?
}