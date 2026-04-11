package com.stafeewa.photocalorie.app.presentation.navigation

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
import com.stafeewa.photocalorie.app.presentation.screens.foodmain.FoodIntakeScreen
import com.stafeewa.photocalorie.app.presentation.screens.profile.ProfileScreen
import com.stafeewa.photocalorie.app.presentation.screens.recipes.RecipeScreen
import com.stafeewa.photocalorie.app.presentation.screens.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: Destination,
    modifier: Modifier = Modifier

) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route
    ) {
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.HOME -> FoodIntakeScreen()

                    Destination.RECIPES -> RecipeScreen()
                    Destination.PROFILE -> ProfileScreen(
                        onSaveProfile = {},
                        onCalculateRate = {}
                    )

                    Destination.SETTINGS -> SettingsScreen()
                }
            }
        }
    }
}

enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    HOME("home", "Home", Icons.Default.Home, "Home"),
    RECIPES("recipes", "Recipes", Icons.Default.Search, "Recipes"),
    SETTINGS("settings", "Settings", Icons.Default.Settings, "Settings"),
    PROFILE("profile", "Profile", Icons.Default.Person, "Profile"),
}

@Composable
fun NavigationBarExample(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val startDestination = Destination.HOME
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                Destination.entries.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedDestination == index,
                        onClick = {
                            // Сначала очищаем BackStack до корня (popUpTo корневого экрана)
                            navController.popBackStack(
                                route = destination.route,
                                inclusive = false,
                                saveState = false
                            )

                            // Если уже находимся на этом экране, ничего не делаем
                            if (selectedDestination != index) {
                                navController.navigate(route = destination.route) {
                                    // Устанавливаем launchSingleTop = true чтобы не создавать дубликаты
                                    launchSingleTop = true
                                    // Очищаем весь BackStack до этого экрана
                                    popUpTo(destination.route) {
                                        saveState = false
                                    }
                                    // Сохраняем состояние (если нужно)
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
        NavGraph(navController, startDestination, modifier = Modifier.padding(contentPadding))
    }
}