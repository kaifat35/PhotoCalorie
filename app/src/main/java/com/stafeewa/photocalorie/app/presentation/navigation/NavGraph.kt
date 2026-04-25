package com.stafeewa.photocalorie.app.presentation.navigation

import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stafeewa.photocalorie.app.R
import com.stafeewa.photocalorie.app.presentation.screens.camera.CameraScreen
import com.stafeewa.photocalorie.app.presentation.screens.foodmain.FoodIntakeScreen
import com.stafeewa.photocalorie.app.presentation.screens.foodmain.FoodIntakeViewModel
import com.stafeewa.photocalorie.app.presentation.screens.profile.ProfileScreen
import com.stafeewa.photocalorie.app.presentation.screens.recipes.RecipeScreen
import com.stafeewa.photocalorie.app.presentation.screens.recommendation.RecommendationScreen
import com.stafeewa.photocalorie.app.presentation.screens.settings.SettingsScreen

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
            CameraScreen()
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
        //Рекомендации
        composable("recommendation") {
            val foodIntakeViewModel: FoodIntakeViewModel = hiltViewModel()
            RecommendationScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddProduct = { product, mealType, portion ->
                    foodIntakeViewModel.addFoodEntry(
                        product.name, mealType, portion,
                        product.proteinPer100g * portion/100,
                        product.fatPer100g * portion/100,
                        product.carbsPer100g * portion/100
                    )
                    navController.popBackStack()
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
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    @StringRes val contentDescriptionRes: Int
) {
    HOME("home", R.string.nav_home, Icons.Default.Home, R.string.nav_home),
    CAMERA("camera", R.string.nav_camera, Icons.Default.CameraAlt, R.string.nav_camera),
    RECIPES("recipes", R.string.nav_recipes, Icons.Default.Search, R.string.nav_recipes),
    PROFILE("profile", R.string.nav_profile, Icons.Default.Person, R.string.nav_profile),
    SETTINGS("settings", R.string.nav_settings, Icons.Default.Settings, R.string.nav_settings),
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
                listOf(Destination.HOME, Destination.CAMERA, Destination.RECIPES, Destination.PROFILE).forEach { destination ->
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
                                contentDescription = stringResource(destination.contentDescriptionRes)
                            )
                        },
                        label = { Text(stringResource(destination.labelRes)) }
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