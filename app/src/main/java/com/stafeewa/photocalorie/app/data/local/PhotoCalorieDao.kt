package com.stafeewa.photocalorie.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoCalorieDao {

    @Query("SELECT * FROM subscriptions")
    fun getAllSubscriptions(): Flow<List<SubscriptionDbModel>>

    @Insert(onConflict = IGNORE)
    suspend fun addSubscription(subscriptionDbModel: SubscriptionDbModel)

    @Transaction
    @Delete
    suspend fun deleteSubscription(subscriptionDbModel: SubscriptionDbModel)

    @Query("SELECT * FROM recipes WHERE topic IN (:topics)")
    fun getAllRecipesByTopics(topics: List<String>): Flow<List<RecipeDbModel>>

    @Insert(onConflict = IGNORE)
    suspend fun addRecipes(recipes: List<RecipeDbModel>): List<Long>

    @Query("DELETE FROM recipes WHERE topic IN (:topics)")
    suspend fun deleteRecipesByTopics(topics: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): User?

    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUserFlow(): Flow<User?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Update
    suspend fun updateUser(user: User)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Int?)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("SELECT * FROM food_entry WHERE date(timestamp/1000, 'unixepoch') = date('now')")
    fun getTodayEntries(): Flow<List<FoodEntryDbModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodEntry(entry: FoodEntryDbModel)

    @Delete
    suspend fun deleteFoodEntry(entry: FoodEntryDbModel)

    @Query("DELETE FROM food_entry WHERE id = :entryId")
    suspend fun deleteFoodEntry(entryId: String)

    @Query("UPDATE food_entry SET portion = :portion WHERE id = :entryId")
    suspend fun updatePortion(entryId: String, portion: Double)
}