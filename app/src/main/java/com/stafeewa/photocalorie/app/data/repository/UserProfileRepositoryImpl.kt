package com.stafeewa.photocalorie.app.data.repository

import com.stafeewa.photocalorie.app.data.local.PhotoCalorieDao
import com.stafeewa.photocalorie.app.data.local.User
import com.stafeewa.photocalorie.app.data.mapper.toUserProfile
import com.stafeewa.photocalorie.app.domain.entity.UserProfile
import com.stafeewa.photocalorie.app.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val appDao: PhotoCalorieDao
) : UserProfileRepository {

    override fun getUserProfile(): Flow<UserProfile> = flow {
        var currentUser = appDao.getCurrentUser()

        if (currentUser == null) {
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
        }

        emit(currentUser.toUserProfile())

        appDao.getCurrentUserFlow()
            .distinctUntilChanged()
            .collect { updatedUser ->
                if (updatedUser != null && updatedUser.id == currentUser?.id) {
                    emit(updatedUser.toUserProfile())
                }
            }
    }

    override suspend fun updateAge(age: Int) {
        appDao.getCurrentUser()?.let { user ->
            appDao.updateUser(user.copy(age = age))
        }
    }

    override suspend fun updateDailyCalories(dailyCalories: Double?) {
        appDao.getCurrentUser()?.let { user ->
            appDao.updateUser(user.copy(dailyCalories = dailyCalories))
        }
    }

    override suspend fun updateGender(gender: String?) {
        appDao.getCurrentUser()?.let { user ->
            appDao.updateUser(user.copy(gender = gender))
        }
    }

    override suspend fun updateHeight(height: Double?) {
        appDao.getCurrentUser()?.let { user ->
            appDao.updateUser(user.copy(height = height))
        }
    }

    override suspend fun updateImage(imageUri: String?) {
        val currentUser = appDao.getCurrentUser()
        if (currentUser == null) {
            appDao.insertUser(
                User(
                    login = "",
                    email = "",
                    password = "",
                    imageUri = imageUri
                )
            )
        } else {
            appDao.updateUser(currentUser.copy(imageUri = imageUri))
        }
    }

    override suspend fun updateLogin(login: String) {
        appDao.getCurrentUser()?.let { user ->
            appDao.updateUser(user.copy(login = login))
        }
    }

    override suspend fun updateMail(email: String) {
        appDao.getCurrentUser()?.let { user ->
            val existingUser = appDao.getUserByEmail(email)
            if (existingUser != null && existingUser.id != user.id) {
                throw Exception("Email already in use by another account")
            }
            appDao.updateUser(user.copy(email = email))
        }
    }

    override suspend fun updatePassword(password: String) {
        appDao.getCurrentUser()?.let { user ->
            appDao.updateUser(user.copy(password = password))
        }
    }

    override suspend fun updateWeight(weight: Double?) {
        appDao.getCurrentUser()?.let { user ->
            appDao.updateUser(user.copy(weight = weight))
        }
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
        val currentUser = appDao.getCurrentUser()

        if (currentUser == null) {
            appDao.insertUser(
                User(
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
            )
            return
        }

        if (email != null && email != currentUser.email) {
            val existingUser = appDao.getUserByEmail(email)
            if (existingUser != null && existingUser.id != currentUser.id) {
                throw Exception("Email уже используется другим аккаунтом")
            }
        }

        appDao.updateUser(
            currentUser.copy(
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
        )
    }

    override suspend fun calculateDailyCalories(
        gender: String,
        height: Double,
        weight: Double,
        age: Int,
        activityLevel: Double
    ): Double {
        val bmr = when (gender.lowercase()) {
            "мужской", "male" -> 10 * weight + 6.25 * height - 5 * age + 5
            "женский", "female" -> 10 * weight + 6.25 * height - 5 * age - 161
            else -> 10 * weight + 6.25 * height - 5 * age
        }
        val dailyCalories = bmr * activityLevel
        updateDailyCalories(dailyCalories)
        return dailyCalories
    }

    override suspend fun deleteProfile(userId: Int?) {
        appDao.deleteUserById(userId)
    }
}
