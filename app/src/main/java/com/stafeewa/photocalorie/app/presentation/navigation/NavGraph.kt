package com.stafeewa.photocalorie.app.presentation.navigation

import android.os.Bundle
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
        // Основные экраны
        composable(Destination.HOME.route) {
            FoodIntakeScreen(
                navController = navController  // ← передаём NavController
            )
        }

        composable(Destination.RECIPES.route) {
            RecipeScreen()
        }

        composable(Destination.PROFILE.route) {
            ProfileScreen(
                onSaveProfile = {},
                onCalculateRate = {}
            )
        }

        composable(Destination.SETTINGS.route) {
            SettingsScreen()
        }

        // Экран камеры
        composable("camera") {
            CameraScreen(
                onBack = {
                    navController.popBackStack()
                },
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
    }
}

enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    HOME("home", "Главная", Icons.Default.Home, "Home"),
    RECIPES("recipes", "Рецепты", Icons.Default.Search, "Recipes"),
    SETTINGS("settings", "Настройки", Icons.Default.Settings, "Settings"),
    PROFILE("profile", "Профиль", Icons.Default.Person, "Profile"),
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
                Destination.entries.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedDestination == index,
                        onClick = {
                            if (selectedDestination != index) {
                                navController.navigate(route = destination.route) {
                                    launchSingleTop = true
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    restoreState = true
                                }
                                selectedDestination = index
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