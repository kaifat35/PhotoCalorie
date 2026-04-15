package com.stafeewa.photocalorie.app.presentation.navigation

import android.os.Bundle
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stafeewa.photocalorie.app.presentation.screens.camera.CameraScreen
import com.stafeewa.photocalorie.app.presentation.screens.foodmain.FoodIntakeScreen
import com.stafeewa.photocalorie.app.presentation.screens.profile.ProfileScreen
import com.stafeewa.photocalorie.app.presentation.screens.recipes.RecipeScreen
import com.stafeewa.photocalorie.app.presentation.screens.settings.SettingsScreen
import androidx.navigation.NavGraph.Companion.findStartDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Главная
        composable(Destination.HOME.route) {
            FoodIntakeScreen(navController = navController)
        }

        // Камера
        composable(Destination.CAMERA.route) {
            CameraScreen(
                onFoodRecognized = { name, mealType, portion, protein, fat, carbs ->
                    val resultBundle = Bundle().apply {
                        putString("name", name)
                        putString("mealType", mealType.name)
                        putDouble("portion", portion)
                        putDouble("protein", protein)
                        putDouble("fat", fat)
                        putDouble("carbs", carbs)
                    }
                    navController.previousBackStackEntry?.savedStateHandle?.set("food_result", resultBundle)
                    navController.popBackStack()
                }
            )
        }

        // Рецепты
        composable(Destination.RECIPES.route) {
            RecipeScreen()
        }

        // Профиль
        composable(Destination.PROFILE.route) {
            ProfileScreen(
                onSaveProfile = {},
                onCalculateRate = {},
                onNavigateToSettings = {
                    navController.navigate(Destination.SETTINGS.route)
                }
            )
        }

        // Настройки (скрыты из нижней навигации)
        composable(Destination.SETTINGS.route) {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}

enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    HOME("home", "Главная", Icons.Default.Home, "Главная"),
    CAMERA("camera", "Камера", Icons.Default.CameraAlt, "Камера"),
    RECIPES("recipes", "Рецепты", Icons.Default.Search, "Рецепты"),
    PROFILE("profile", "Профиль", Icons.Default.Person, "Профиль"),
    SETTINGS("settings", "Настройки", Icons.Default.Settings, "Настройки"),
}

@Composable
fun NavigationBarExample(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val startDestination = Destination.HOME.route
    var selectedDestination by rememberSaveable { mutableIntStateOf(Destination.HOME.ordinal) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                // Показываем только нужные пункты (без настроек)
                listOf(Destination.HOME, Destination.CAMERA, Destination.RECIPES, Destination.PROFILE).forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedDestination == destination.ordinal,
                        onClick = {
                            if (selectedDestination != destination.ordinal) {
                                navController.navigate(route = destination.route) {
                                    launchSingleTop = true
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    restoreState = true
                                }
                                selectedDestination = destination.ordinal
                            }
                        },
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = destination.contentDescription
                            )
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { contentPadding ->
        NavGraph(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(contentPadding)
        )
    }
}