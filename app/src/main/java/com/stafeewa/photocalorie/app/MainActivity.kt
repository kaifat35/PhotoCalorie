package com.stafeewa.photocalorie.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import com.stafeewa.photocalorie.app.presentation.navigation.NavigationBarExample
import com.stafeewa.photocalorie.app.presentation.ui.theme.PhotoCalorieTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhotoCalorieTheme {
                NavigationBarExample(modifier = Modifier)
            }
        }
    }
}