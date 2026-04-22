@file:OptIn(ExperimentalMaterial3Api::class)

package com.stafeewa.photocalorie.app.presentation.screens.settings

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.stafeewa.photocalorie.app.R
import com.stafeewa.photocalorie.app.domain.entity.Language
import com.stafeewa.photocalorie.app.domain.entity.MinTrainingExamplesOption
import com.stafeewa.photocalorie.app.domain.entity.ThemeMode
import com.stafeewa.photocalorie.app.domain.entity.TrainingFrequencyOption
import com.stafeewa.photocalorie.app.presentation.ui.theme.textFieldColors

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            viewModel.processCommand(SettingsCommand.SetNotificationEnabled(it))
        }
    )
    val context = LocalContext.current
    // Следим за изменением языка
    val state by viewModel.state.collectAsState()

    var lastLanguage by remember { mutableStateOf<Language?>(null) }
    var lastTheme by remember { mutableStateOf<ThemeMode?>(null) }

    // Перезапускаем Activity только когда язык в state действительно изменился
    LaunchedEffect(state) {
        if (state is SettingsState.Configuration) {
            val currentLanguage = (state as SettingsState.Configuration).language
            val currentTheme = (state as SettingsState.Configuration).themeMode
            if ((lastLanguage != null && lastLanguage != currentLanguage) ||
                (lastTheme != null && lastTheme != currentTheme)
            ) {
                (context as? Activity)?.recreate()
            }
            lastLanguage = currentLanguage
            lastTheme = currentTheme
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = stringResource(R.string.back),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            stringResource(R.string.settings),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            fontFamily = FontFamily(Font(R.font.jura)),
                            fontSize = 36.sp,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
            )
        },
        content = { contentPadding ->

            when (val currentState = state) {
                is SettingsState.Configuration -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = contentPadding,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            SettingsCard(
                                title = stringResource(R.string.search_language),
                                subtitle = stringResource(R.string.select_language_for_recipes_search)

                            ) {
                                SettingsDropdown(
                                    items = currentState.languages,
                                    selectedItem = currentState.language,
                                    onItemSelected = { language ->
                                        viewModel.processCommand(
                                            SettingsCommand.SelectLanguage(
                                                language
                                            )
                                        )
                                    },
                                    itemAsString = {
                                        it.toLocalizedName()
                                    }
                                )
                            }
                        }
                        item {
                            SettingsCard(
                                title = stringResource(R.string.update_interval),
                                subtitle = stringResource(R.string.how_often_to_update_recipe)

                            ) {
                                SettingsDropdown(
                                    items = currentState.intervals,
                                    selectedItem = currentState.interval,
                                    onItemSelected = {
                                        viewModel.processCommand(SettingsCommand.SelectInterval(it))
                                    },
                                    itemAsString = {
                                        it.toLocalizedName()
                                    }
                                )
                            }
                        }
                        item {
                            SettingsCard(
                                title = stringResource(R.string.notifications),
                                subtitle = stringResource(R.string.show_notifications_about_new_recipes)

                            ) {
                                Switch(
                                    checked = currentState.notificationsEnabled,
                                    onCheckedChange = { enabled ->
                                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            permissionLauncher.launch(
                                                Manifest.permission.POST_NOTIFICATIONS
                                            )
                                        } else {
                                            viewModel.processCommand(
                                                SettingsCommand
                                                    .SetNotificationEnabled(enabled)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                        item {
                            SettingsCard(
                                title = stringResource(R.string.update_only_via_wi_fi),
                                subtitle = stringResource(R.string.save_mobile_data)

                            ) {
                                Switch(
                                    checked = currentState.wifiOnly,
                                    onCheckedChange = {
                                        viewModel.processCommand(
                                            SettingsCommand
                                                .SeWifiOnly(it)
                                        )
                                    }
                                )
                            }
                        }
                        item {
                            SettingsCard(
                                title = stringResource(R.string.theme),
                                subtitle = stringResource(R.string.select_theme)
                            ) {
                                SettingsDropdown(
                                    items = currentState.themeModes,
                                    selectedItem = currentState.themeMode,
                                    onItemSelected = { themeMode ->
                                        viewModel.processCommand(
                                            SettingsCommand.SetThemeMode(
                                                themeMode
                                            )
                                        )
                                    },
                                    itemAsString = {
                                        it.toLocalizedName()
                                    }
                                )
                            }
                        }
                        item {
                            SettingsCard(
                                title = stringResource(R.string.training_frequency),
                                subtitle = stringResource(R.string.training_frequency_hint)
                            ) {
                                SettingsDropdown(
                                    items = currentState.trainingFrequencyOptions,
                                    selectedItem = TrainingFrequencyOption.fromHours(
                                        currentState.trainingFrequencyHours
                                    ),
                                    onItemSelected = {
                                        viewModel.processCommand(
                                            SettingsCommand.SetTrainingFrequencyHours(it.hours)
                                        )
                                    },
                                    itemAsString = { it.title }
                                )
                            }
                        }
                        item {
                            SettingsCard(
                                title = stringResource(R.string.min_training_examples),
                                subtitle = stringResource(R.string.min_training_examples_hint)
                            ) {
                                SettingsDropdown(
                                    items = currentState.minTrainingExamplesOptions,
                                    selectedItem = MinTrainingExamplesOption.fromCount(
                                        currentState.minTrainingExamples
                                    ),
                                    onItemSelected = {
                                        viewModel.processCommand(
                                            SettingsCommand.SetMinTrainingExamples(it.count)
                                        )
                                    },
                                    itemAsString = { it.title }
                                )
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }

                    }
                }

                SettingsState.Initial -> {}
            }
        }
    )
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 24.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 24.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
private fun <T> SettingsDropdown(
    modifier: Modifier = Modifier,
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    itemAsString: @Composable (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier.fillMaxWidth(),
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            value = itemAsString(selectedItem),
            onValueChange = {},
            shape = RoundedCornerShape(30.dp),
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = textFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            shape = RoundedCornerShape(30.dp),
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(itemAsString(item))
                    },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}


