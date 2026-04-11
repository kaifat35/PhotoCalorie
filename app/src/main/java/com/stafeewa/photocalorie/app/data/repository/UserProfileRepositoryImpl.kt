package com.stafeewa.photocalorie.app.data.repository

import android.util.Log
import com.stafeewa.photocalorie.app.data.local.PhotoCalorieDao
import com.stafeewa.photocalorie.app.data.local.User
import com.stafeewa.photocalorie.app.data.mapper.toUserProfile
import com.stafeewa.photocalorie.app.domain.entity.UserProfile
import com.stafeewa.photocalorie.app.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val appDao: PhotoCalorieDao
) : UserProfileRepository {

    private val tag = "UserProfileRepositoryImpl"

    // Получение профиля пользователя как Flow
    override fun getUserProfile(): Flow<UserProfile> {
        return appDao.getCurrentUserFlow().map { user ->
            user?.toUserProfile() ?: UserProfile(
                login = "",
                email = "",
                password = "",
                gender = null,
                height = null,
                weight = null,
                age = null,
                imageUri = null,
                dailyCalories = null,
                userId = null
            )
        }
    }

    // Получение текущего пользователя (синхронная версия для внутреннего использования)
    private suspend fun getCurrentUser(): User? {
        return try {
            appDao.getCurrentUser()
        } catch (e: Exception) {
            Log.e(tag, "Error getting current user", e)
            null
        }
    }

    // Обновление возраста
    override suspend fun updateAge(age: Int) {
        try {
            val currentUser = getCurrentUser()
            currentUser?.let { user ->
                val updatedUser = user.copy(age = age)
                appDao.updateUser(updatedUser)
                Log.d(tag, "Age updated to: $age")
            } ?: run {
                Log.w(tag, "No user found to update age")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating age", e)
            throw e
        }
    }

    // Обновление дневной нормы калорий
    override suspend fun updateDailyCalories(dailyCalories: Double?) {
        try {
            val currentUser = getCurrentUser()
            currentUser?.let { user ->
                val updatedUser = user.copy(dailyCalories = dailyCalories)
                appDao.updateUser(updatedUser)
                Log.d(tag, "Daily calories updated to: $dailyCalories")
            } ?: run {
                Log.w(tag, "No user found to update daily calories")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating daily calories", e)
            throw e
        }
    }


    // Обновление пола
    override suspend fun updateGender(gender: String?) {
        try {
            val currentUser = getCurrentUser()
            currentUser?.let { user ->
                val updatedUser = user.copy(gender = gender)
                appDao.updateUser(updatedUser)
                Log.d(tag, "Gender updated to: $gender")
            } ?: run {
                Log.w(tag, "No user found to update gender")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating gender", e)
            throw e
        }
    }

    // Обновление роста
    override suspend fun updateHeight(height: Double?) {
        try {
            val currentUser = getCurrentUser()
            currentUser?.let { user ->
                val updatedUser = user.copy(height = height)
                appDao.updateUser(updatedUser)
                Log.d(tag, "Height updated to: $height")
            } ?: run {
                Log.w(tag, "No user found to update height")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating height", e)
            throw e
        }
    }

    // Обновление изображения профиля
    override suspend fun updateImage(imageUri: String?) {
        try {
            var currentUser = getCurrentUser()
            if (currentUser == null) {
                currentUser = User(
                    login = "",
                    email = "",
                    password = "",
                    imageUri = imageUri
                )
                appDao.insertUser(currentUser)
            } else {
                val updatedUser = currentUser.copy(imageUri = imageUri)
                appDao.updateUser(updatedUser)
            }
            Log.d(tag, "Image updated to path: $imageUri")
        } catch (e: Exception) {
            Log.e(tag, "Error updating image", e)
            throw e
        }
    }

    // Обновление логина
    override suspend fun updateLogin(login: String) {
        try {
            val currentUser = getCurrentUser()
            currentUser?.let { user ->
                val updatedUser = user.copy(login = login)
                appDao.updateUser(updatedUser)
                Log.d(tag, "Login updated to: $login")
            } ?: run {
                Log.w(tag, "No user found to update login")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating login", e)
            throw e
        }
    }

    // Обновление email
    override suspend fun updateMail(email: String) {
        try {
            val currentUser = getCurrentUser()
            currentUser?.let { user ->
                // Проверяем, не используется ли email другим пользователем
                val existingUser = appDao.getUserByEmail(email)
                if (existingUser != null && existingUser.id != user.id) {
                    throw Exception("Email already in use by another account")
                }

                val updatedUser = user.copy(email = email)
                appDao.updateUser(updatedUser)
                Log.d(tag, "Email updated to: $email")
            } ?: run {
                Log.w(tag, "No user found to update email")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating email", e)
            throw e
        }
    }

    override suspend fun updatePassword(password: String) {
        try {
            val currentUser = getCurrentUser()
            currentUser?.let { user ->
                val hashedPassword = password
                val updatedUser = user.copy(password = hashedPassword)
                appDao.updateUser(updatedUser)
                Log.d(tag, "Password updated successfully")
            } ?: run {
                Log.w(tag, "No user found to update password")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating password", e)
            throw e
        }
    }


    // Обновление веса
    override suspend fun updateWeight(weight: Double?) {
        try {
            val currentUser = getCurrentUser()
            currentUser?.let { user ->
                val updatedUser = user.copy(weight = weight)
                appDao.updateUser(updatedUser)
                Log.d(tag, "Weight updated to: $weight")
            } ?: run {
                Log.w(tag, "No user found to update weight")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating weight", e)
            throw e
        }
    }

    // Метод для обновления нескольких полей одновременно
    override suspend fun updateProfile(
        login: String?,
        email: String?,
        password: String?,
        gender: String?,
        height: Double?,
        weight: Double?,
        age: Int?,
        imageUri: String?,
        dailyCalories: Double?
    ) {
        try {
            val currentUser = getCurrentUser()

            if (currentUser == null) {
                // Создаем нового пользователя, если его нет
                val newUser = User(
                    login = login ?: "",
                    email = email ?: "",
                    password = password ?: "",
                    gender = gender,
                    height = height,
                    weight = weight,
                    age = age,
                    imageUri = imageUri,
                    dailyCalories = dailyCalories
                )
                appDao.insertUser(newUser)
                Log.d(tag, "New user created: $newUser")
            } else {
                // Проверка уникальности email, если он обновляется
                if (email != null && email != currentUser.email) {
                    val existingUser = appDao.getUserByEmail(email)
                    if (existingUser != null && existingUser.id != currentUser.id) {
                        throw Exception("Email уже используется другим аккаунтом")
                    }
                }

                val updatedUser = currentUser.copy(
                    login = login ?: currentUser.login,
                    email = email ?: currentUser.email,
                    password = password ?: currentUser.password,
                    gender = gender ?: currentUser.gender,
                    height = height ?: currentUser.height,
                    weight = weight ?: currentUser.weight,
                    age = age ?: currentUser.age,
                    imageUri = imageUri ?: currentUser.imageUri,
                    dailyCalories = dailyCalories ?: currentUser.dailyCalories
                )

                appDao.updateUser(updatedUser)
                Log.d(tag, "User profile updated: $updatedUser")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating user profile", e)
            throw e
        }
    }

    // Расчет дневной нормы калорий (BMR)
    override suspend fun calculateDailyCalories(
        gender: String,
        height: Double,
        weight: Double,
        age: Int,
        activityLevel: Double // Множитель активности
    ): Double {
        return try {
            // Формула Миффлина-Сан Жеора
            val bmr = when (gender.lowercase()) {
                "мужской", "male" -> {
                    10 * weight + 6.25 * height - 5 * age + 5
                }
                "женский", "female" -> {
                    10 * weight + 6.25 * height - 5 * age - 161
                }
                else -> {
                    10 * weight + 6.25 * height - 5 * age
                }
            }

            val dailyCalories = bmr * activityLevel

            // Сохраняем расчет в профиль
            updateDailyCalories(dailyCalories)

            Log.d(tag, "Calculated daily calories: $dailyCalories (BMR: $bmr, Activity: $activityLevel)")
            dailyCalories
        } catch (e: Exception) {
            Log.e(tag, "Error calculating daily calories", e)
            throw e
        }
    }

    // Удаление пользователя
    override suspend fun deleteProfile(userId: Int?) {
        try {
            appDao.deleteUserById(userId)
            Log.d(tag, "User deleted: $userId")
        } catch (e: Exception) {
            Log.e(tag, "Error deleting user", e)
            throw e
        }
    }
}