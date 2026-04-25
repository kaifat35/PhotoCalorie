package com.stafeewa.photocalorie.app.data.repository

import android.util.Log
import com.stafeewa.photocalorie.app.data.local.PhotoCalorieDao
import com.stafeewa.photocalorie.app.data.local.User
import com.stafeewa.photocalorie.app.data.mapper.toUserProfile
import com.stafeewa.photocalorie.app.domain.entity.UserProfile
import com.stafeewa.photocalorie.app.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val appDao: PhotoCalorieDao
) : UserProfileRepository {

    private val tag = "UserProfileRepositoryImpl"
    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private suspend fun ensureUserExists() {
        Log.d(tag, "ensureUserExists: START")
        val currentUser = appDao.getCurrentUser()
        Log.d(tag, "ensureUserExists: currentUser = $currentUser")

        if (currentUser == null) {
            Log.d(tag, "ensureUserExists: User is null, creating default user")
            val defaultUser = User(
                login = "",
                email = "",
                password = "",
                gender = null,
                height = null,
                weight = null,
                age = null,
                imageUri = null,
                dailyCalories = 2000.0
            )
            appDao.insertUser(defaultUser)
            Log.d(tag, "ensureUserExists: Default user created with daily calories: 2000")
            refresh()
        } else {
            Log.d(tag, "ensureUserExists: User already exists with id=${currentUser.id}")
        }
        Log.d(tag, "ensureUserExists: END")
    }

    private suspend fun refresh() {
        Log.d(tag, "refresh: Emitting refresh trigger")
        refreshTrigger.emit(Unit)
        Log.d(tag, "refresh: Refresh trigger emitted")
    }

    override fun getUserProfile(): Flow<UserProfile> {
        Log.d(tag, "getUserProfile: Called, creating Flow")

        return flow {
            Log.d(tag, "flow: Started collecting")

            // Сначала получаем текущего пользователя синхронно
            var currentUser = appDao.getCurrentUser()
            Log.d(tag, "flow: Initial currentUser = $currentUser")

            if (currentUser == null) {
                Log.d(tag, "flow: No user, creating default")
                val defaultUser = User(
                    login = "",
                    email = "",
                    password = "",
                    gender = null,
                    height = null,
                    weight = null,
                    age = null,
                    imageUri = null,
                    dailyCalories = 2000.0
                )
                appDao.insertUser(defaultUser)
                currentUser = defaultUser
                Log.d(tag, "flow: Default user created")
            }

            // Отправляем текущего пользователя
            emit(currentUser.toUserProfile())
            Log.d(tag, "flow: Emitted initial user: id=${currentUser.id}")

            // Подписываемся на изменения, но с debounce и distinctUntilChanged
            appDao.getCurrentUserFlow()
                .distinctUntilChanged() // ← добавляем: не эмитим одинаковые значения
                .collect { updatedUser ->
                    if (updatedUser != null && updatedUser.id == currentUser?.id) {
                        Log.d(tag, "flow: Received update from Flow: id=${updatedUser.id}")
                        emit(updatedUser.toUserProfile())
                    }
                }
        }
    }

    override suspend fun updateAge(age: Int) {
        Log.d(tag, "updateAge: START, age=$age")
        try {
            val currentUser = appDao.getCurrentUser()
            Log.d(tag, "updateAge: currentUser = $currentUser")
            currentUser?.let { user ->
                val updatedUser = user.copy(age = age)
                appDao.updateUser(updatedUser)
                refresh()
                Log.d(tag, "updateAge: Age updated to: $age")
            } ?: Log.w(tag, "updateAge: No user found to update age")
        } catch (e: Exception) {
            Log.e(tag, "updateAge: Error updating age", e)
            throw e
        }
        Log.d(tag, "updateAge: END")
    }

    override suspend fun updateDailyCalories(dailyCalories: Double?) {
        Log.d(tag, "updateDailyCalories: START, dailyCalories=$dailyCalories")
        try {
            val currentUser = appDao.getCurrentUser()
            Log.d(tag, "updateDailyCalories: currentUser = $currentUser")
            currentUser?.let { user ->
                val updatedUser = user.copy(dailyCalories = dailyCalories)
                appDao.updateUser(updatedUser)
                refresh()
                Log.d(tag, "updateDailyCalories: Daily calories updated to: $dailyCalories")
            } ?: Log.w(tag, "updateDailyCalories: No user found to update daily calories")
        } catch (e: Exception) {
            Log.e(tag, "updateDailyCalories: Error updating daily calories", e)
            throw e
        }
        Log.d(tag, "updateDailyCalories: END")
    }

    override suspend fun updateGender(gender: String?) {
        Log.d(tag, "updateGender: START, gender=$gender")
        try {
            val currentUser = appDao.getCurrentUser()
            Log.d(tag, "updateGender: currentUser = $currentUser")
            currentUser?.let { user ->
                val updatedUser = user.copy(gender = gender)
                appDao.updateUser(updatedUser)
                refresh()
                Log.d(tag, "updateGender: Gender updated to: $gender")
            } ?: Log.w(tag, "updateGender: No user found to update gender")
        } catch (e: Exception) {
            Log.e(tag, "updateGender: Error updating gender", e)
            throw e
        }
        Log.d(tag, "updateGender: END")
    }

    override suspend fun updateHeight(height: Double?) {
        Log.d(tag, "updateHeight: START, height=$height")
        try {
            val currentUser = appDao.getCurrentUser()
            Log.d(tag, "updateHeight: currentUser = $currentUser")
            currentUser?.let { user ->
                val updatedUser = user.copy(height = height)
                appDao.updateUser(updatedUser)
                refresh()
                Log.d(tag, "updateHeight: Height updated to: $height")
            } ?: Log.w(tag, "updateHeight: No user found to update height")
        } catch (e: Exception) {
            Log.e(tag, "updateHeight: Error updating height", e)
            throw e
        }
        Log.d(tag, "updateHeight: END")
    }

    override suspend fun updateImage(imageUri: String?) {
        Log.d(tag, "updateImage: START, imageUri=$imageUri")
        try {
            var currentUser = appDao.getCurrentUser()
            Log.d(tag, "updateImage: currentUser = $currentUser")
            if (currentUser == null) {
                Log.d(tag, "updateImage: No user, creating new one")
                currentUser = User(
                    login = "",
                    email = "",
                    password = "",
                    imageUri = imageUri
                )
                appDao.insertUser(currentUser)
                Log.d(tag, "updateImage: New user created with image")
            } else {
                val updatedUser = currentUser.copy(imageUri = imageUri)
                appDao.updateUser(updatedUser)
                Log.d(tag, "updateImage: Existing user updated with image")
            }
            refresh()
            Log.d(tag, "updateImage: Image updated to path: $imageUri")
        } catch (e: Exception) {
            Log.e(tag, "updateImage: Error updating image", e)
            throw e
        }
        Log.d(tag, "updateImage: END")
    }

    override suspend fun updateLogin(login: String) {
        Log.d(tag, "updateLogin: START, login=$login")
        try {
            val currentUser = appDao.getCurrentUser()
            Log.d(tag, "updateLogin: currentUser = $currentUser")
            currentUser?.let { user ->
                val updatedUser = user.copy(login = login)
                appDao.updateUser(updatedUser)
                refresh()
                Log.d(tag, "updateLogin: Login updated to: $login")
            } ?: Log.w(tag, "updateLogin: No user found to update login")
        } catch (e: Exception) {
            Log.e(tag, "updateLogin: Error updating login", e)
            throw e
        }
        Log.d(tag, "updateLogin: END")
    }

    override suspend fun updateMail(email: String) {
        Log.d(tag, "updateMail: START, email=$email")
        try {
            val currentUser = appDao.getCurrentUser()
            Log.d(tag, "updateMail: currentUser = $currentUser")
            currentUser?.let { user ->
                val existingUser = appDao.getUserByEmail(email)
                if (existingUser != null && existingUser.id != user.id) {
                    Log.e(tag, "updateMail: Email already in use by another account")
                    throw Exception("Email already in use by another account")
                }
                val updatedUser = user.copy(email = email)
                appDao.updateUser(updatedUser)
                refresh()
                Log.d(tag, "updateMail: Email updated to: $email")
            } ?: Log.w(tag, "updateMail: No user found to update email")
        } catch (e: Exception) {
            Log.e(tag, "updateMail: Error updating email", e)
            throw e
        }
        Log.d(tag, "updateMail: END")
    }

    override suspend fun updatePassword(password: String) {
        Log.d(tag, "updatePassword: START")
        try {
            val currentUser = appDao.getCurrentUser()
            Log.d(tag, "updatePassword: currentUser = $currentUser")
            currentUser?.let { user ->
                val updatedUser = user.copy(password = password)
                appDao.updateUser(updatedUser)
                refresh()
                Log.d(tag, "updatePassword: Password updated successfully")
            } ?: Log.w(tag, "updatePassword: No user found to update password")
        } catch (e: Exception) {
            Log.e(tag, "updatePassword: Error updating password", e)
            throw e
        }
        Log.d(tag, "updatePassword: END")
    }

    override suspend fun updateWeight(weight: Double?) {
        Log.d(tag, "updateWeight: START, weight=$weight")
        try {
            val currentUser = appDao.getCurrentUser()
            Log.d(tag, "updateWeight: currentUser = $currentUser")
            currentUser?.let { user ->
                val updatedUser = user.copy(weight = weight)
                appDao.updateUser(updatedUser)
                refresh()
                Log.d(tag, "updateWeight: Weight updated to: $weight")
            } ?: Log.w(tag, "updateWeight: No user found to update weight")
        } catch (e: Exception) {
            Log.e(tag, "updateWeight: Error updating weight", e)
            throw e
        }
        Log.d(tag, "updateWeight: END")
    }

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
        Log.d(tag, "updateProfile: START")
        try {
            val currentUser = appDao.getCurrentUser()
            Log.d(tag, "updateProfile: currentUser = $currentUser")

            if (currentUser == null) {
                Log.d(tag, "updateProfile: No user, creating new one")
                val newUser = User(
                    login = login ?: "",
                    email = email ?: "",
                    password = password ?: "",
                    gender = gender,
                    height = height,
                    weight = weight,
                    age = age,
                    imageUri = imageUri,
                    dailyCalories = dailyCalories ?: 2000.0
                )
                appDao.insertUser(newUser)
                Log.d(tag, "updateProfile: New user created: $newUser")
            } else {
                if (email != null && email != currentUser.email) {
                    val existingUser = appDao.getUserByEmail(email)
                    if (existingUser != null && existingUser.id != currentUser.id) {
                        Log.e(tag, "updateProfile: Email already in use by another account")
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
                Log.d(tag, "updateProfile: User updated: $updatedUser")
            }
            refresh()
            Log.d(tag, "updateProfile: User profile updated")
        } catch (e: Exception) {
            Log.e(tag, "updateProfile: Error updating user profile", e)
            throw e
        }
        Log.d(tag, "updateProfile: END")
    }

    override suspend fun calculateDailyCalories(
        gender: String,
        height: Double,
        weight: Double,
        age: Int,
        activityLevel: Double
    ): Double {
        Log.d(tag, "calculateDailyCalories: START, gender=$gender, height=$height, weight=$weight, age=$age, activityLevel=$activityLevel")
        return try {
            val bmr = when (gender.lowercase()) {
                "мужской", "male" -> {
                    val result = 10 * weight + 6.25 * height - 5 * age + 5
                    Log.d(tag, "calculateDailyCalories: Male BMR = $result")
                    result
                }
                "женский", "female" -> {
                    val result = 10 * weight + 6.25 * height - 5 * age - 161
                    Log.d(tag, "calculateDailyCalories: Female BMR = $result")
                    result
                }
                else -> {
                    val result = 10 * weight + 6.25 * height - 5 * age
                    Log.d(tag, "calculateDailyCalories: Default BMR = $result")
                    result
                }
            }
            val dailyCalories = bmr * activityLevel
            Log.d(tag, "calculateDailyCalories: Daily calories = $dailyCalories")
            updateDailyCalories(dailyCalories)
            Log.d(tag, "calculateDailyCalories: Daily calories updated")
            dailyCalories
        } catch (e: Exception) {
            Log.e(tag, "calculateDailyCalories: Error calculating daily calories", e)
            throw e
        }
    }

    override suspend fun deleteProfile(userId: Int?) {
        Log.d(tag, "deleteProfile: START, userId=$userId")
        try {
            appDao.deleteUserById(userId)
            refresh()
            Log.d(tag, "deleteProfile: User deleted: $userId")
        } catch (e: Exception) {
            Log.e(tag, "deleteProfile: Error deleting user", e)
            throw e
        }
        Log.d(tag, "deleteProfile: END")
    }
}