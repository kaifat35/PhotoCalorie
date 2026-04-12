package com.stafeewa.photocalorie.app.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.stafeewa.photocalorie.app.data.local.PhotoCalorieDao
import com.stafeewa.photocalorie.app.data.local.PhotoCalorieDatabase
import com.stafeewa.photocalorie.app.data.local.ProductDao
import com.stafeewa.photocalorie.app.data.remote.RecipesApiService
import com.stafeewa.photocalorie.app.data.repository.PhotoCalorieRepositoryImpl
import com.stafeewa.photocalorie.app.data.repository.FoodIntakeRepositoryImpl
import com.stafeewa.photocalorie.app.data.repository.ProductRepositoryImpl
import com.stafeewa.photocalorie.app.data.repository.SettingsRepositoryImpl
import com.stafeewa.photocalorie.app.data.repository.UserProfileRepositoryImpl
import com.stafeewa.photocalorie.app.domain.repository.FoodIntakeRepository
import com.stafeewa.photocalorie.app.domain.repository.ProductRepository
import com.stafeewa.photocalorie.app.domain.repository.RecipeRepository
import com.stafeewa.photocalorie.app.domain.repository.SettingsRepository
import com.stafeewa.photocalorie.app.domain.repository.UserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Singleton
    fun bindProductRepository(
        impl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    fun bindRecipeRepository(
        impl: PhotoCalorieRepositoryImpl
    ): RecipeRepository

    @Binds
    @Singleton
    fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    fun bindUserProfileRepository(
        impl: UserProfileRepositoryImpl
    ): UserProfileRepository

    @Binds
    @Singleton
    fun bindFoodIntakeRepository(
        impl: FoodIntakeRepositoryImpl
    ): FoodIntakeRepository

    companion object {

        @Provides
        @Singleton
        fun provideWorkManager(
            @ApplicationContext context: Context
        ): WorkManager = WorkManager.getInstance(context)

        @Provides
        @Singleton
        fun provideJson(): Json {
            return Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
        }

        @Provides
        @Singleton
        fun provideConverterFactory(
            json: Json
        ): Converter.Factory {
            return json.asConverterFactory(
                "application/json".toMediaType()
            )
        }

        @Provides
        @Singleton
        fun providesRetrofit(
            converterFactory: Converter.Factory
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl("https://api.spoonacular.com/")
                .addConverterFactory(converterFactory)
                .build()
        }

        @Provides
        @Singleton
        fun provideApiService(
            retrofit: Retrofit
        ): RecipesApiService {
            return retrofit.create()
        }

        @Singleton
        @Provides
        fun provideRecipesDatabase(
            @ApplicationContext context: Context
        ): PhotoCalorieDatabase {
            return Room.databaseBuilder(
                context = context,
                klass = PhotoCalorieDatabase::class.java,
                name = "photo_calorie.db"
            ).fallbackToDestructiveMigration(true)
                .build()
        }

        @Singleton
        @Provides
        fun providesRecipesDao(
            database: PhotoCalorieDatabase
        ): PhotoCalorieDao = database.photoCalorieDao()

        @Provides
        @Singleton
        fun providesProductDao(
            database: PhotoCalorieDatabase
        ): ProductDao = database.productDao()
    }
}

