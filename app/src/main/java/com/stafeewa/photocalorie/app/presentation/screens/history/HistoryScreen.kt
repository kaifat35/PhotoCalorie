package com.stafeewa.photocalorie.app.presentation.screens.history

import android.widget.CalendarView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.stafeewa.photocalorie.app.R
import com.stafeewa.photocalorie.app.domain.entity.FoodEntry
import com.stafeewa.photocalorie.app.domain.entity.MealType
import com.stafeewa.photocalorie.app.domain.entity.NutritionStatistics
import com.stafeewa.photocalorie.app.utils.toUserVisibleFoodName
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    var showCustomRangeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history)) },
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is HistoryUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is HistoryUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (uiState as HistoryUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is HistoryUiState.Success -> {
                val data = uiState as HistoryUiState.Success
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        StatisticsCard(statistics = data.statistics, dateRange = data.dateRange)
                    }
                    item {
                        PeriodSelector(
                            selectedPeriod = selectedPeriod,
                            onPeriodSelected = { viewModel.setPeriod(it) },
                            onCustomRangeClick = { showCustomRangeDialog = true }
                        )
                    }
                    items(data.groupedEntries) { dayEntries ->
                        DayGroupCard(dayEntries = dayEntries)
                    }
                    if (data.groupedEntries.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.no_entries_for_period),
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCustomRangeDialog) {
        CustomRangeCalendarDialog(
            onDismiss = { showCustomRangeDialog = false },
            onRangeSelected = { start, end ->
                viewModel.setCustomDateRange(start, end)
                showCustomRangeDialog = false
            }
        )
    }
}

@Composable
fun CustomRangeCalendarDialog(
    onDismiss: () -> Unit,
    onRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectionStep by remember { mutableStateOf(0) } // 0 - ожидание выбора начала, 1 - выбор конца

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (selectionStep) {
                    0 -> stringResource(R.string.select_start_date)
                    else -> stringResource(R.string.select_end_date)
                }
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                // Отображаем выбранные даты
                Text(
                    text = stringResource(R.string.selected_range,
                        startDate?.toString() ?: "—",
                        endDate?.toString() ?: "—"
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Календарь через AndroidView
                AndroidView(
                    factory = { context ->
                        CalendarView(context).apply {
                            setOnDateChangeListener { _, year, month, dayOfMonth ->
                                val selected = LocalDate.of(year, month + 1, dayOfMonth)
                                if (selectionStep == 0) {
                                    startDate = selected
                                    selectionStep = 1
                                } else {
                                    endDate = selected
                                    // Если endDate раньше startDate, меняем местами
                                    if (startDate != null && endDate != null && endDate!! < startDate!!) {
                                        val temp = startDate
                                        startDate = endDate
                                        endDate = temp
                                    }
                                    selectionStep = 0
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (startDate != null && endDate != null) {
                        onRangeSelected(startDate!!, endDate!!)
                    }
                },
                enabled = startDate != null && endDate != null
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    initialDate: LocalDate
) {
    val calendar = Calendar.getInstance().apply {
        set(initialDate.year, initialDate.monthValue - 1, initialDate.dayOfMonth)
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        onDateSelected(selectedDate)
                    } ?: onDismiss()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun StatisticsCard(statistics: NutritionStatistics, dateRange: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = dateRange,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(title = stringResource(R.string.avg_calories),
                    value = "${statistics.avgCalories.toInt()} ${stringResource(R.string.calories_short)}")
                StatItem(title = stringResource(R.string.protein),
                    value = "${statistics.avgProtein.toInt()} ${stringResource(R.string.gram_short)}")
                StatItem(title = stringResource(R.string.fat),
                    value = "${statistics.avgFat.toInt()} ${stringResource(R.string.gram_short)}")
                StatItem(title = stringResource(R.string.carbs),
                    value = "${statistics.avgCarbs.toInt()} ${stringResource(R.string.gram_short)}")
            }
            Text(
                text = stringResource(R.string.based_on_days, statistics.totalDays),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun StatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = title, fontSize = 12.sp)
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: PeriodType,
    onPeriodSelected: (PeriodType) -> Unit,
    onCustomRangeClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PeriodButton(
                title = stringResource(R.string.day),
                isSelected = selectedPeriod == PeriodType.DAY,
                onClick = { onPeriodSelected(PeriodType.DAY) },
                modifier = Modifier.weight(1f)
            )
            PeriodButton(
                title = stringResource(R.string.week),
                isSelected = selectedPeriod == PeriodType.WEEK,
                onClick = { onPeriodSelected(PeriodType.WEEK) },
                modifier = Modifier.weight(1f)
            )
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PeriodButton(
                title = stringResource(R.string.month),
                isSelected = selectedPeriod == PeriodType.MONTH,
                onClick = { onPeriodSelected(PeriodType.MONTH) },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onCustomRangeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedPeriod == PeriodType.CUSTOM)
                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.custom))
            }
        }
    }
}

@Composable
fun PeriodButton(title: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
    ) {
        Text(title, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun DayGroupCard(dayEntries: DayEntries) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dayEntries.date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${dayEntries.totalCalories.toInt()} ${stringResource(R.string.calories_short)}",
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutrientChip(label = stringResource(R.string.protein_short), value = dayEntries.totalProtein.toInt())
                NutrientChip(label = stringResource(R.string.fat_short), value = dayEntries.totalFat.toInt())
                NutrientChip(label = stringResource(R.string.carbs_short), value = dayEntries.totalCarbs.toInt())
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    MealType.values().forEach { mealType ->
                        val entries = dayEntries.mealsByType[mealType].orEmpty()
                        if (entries.isNotEmpty()) {
                            val totals = dayEntries.totalsByMealType[mealType]
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${mealTypeLabel(mealType)} • ${totals?.calories?.toInt() ?: 0} ${stringResource(R.string.calories_short)}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            entries.forEach { entry ->
                                EntryRow(entry = entry)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun mealTypeLabel(mealType: MealType): String = when (mealType) {
    MealType.BREAKFAST -> stringResource(R.string.breakfast)
    MealType.LUNCH -> stringResource(R.string.lunch)
    MealType.DINNER -> stringResource(R.string.dinner)
    MealType.SNACK -> stringResource(R.string.snack)
}

@Composable
fun NutrientChip(label: String, value: Int) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = "$label: $value ${stringResource(R.string.gram_short)}",
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun EntryRow(entry: FoodEntry) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = entry.name.toUserVisibleFoodName(), fontWeight = FontWeight.Medium)
            Text(
                text = "${entry.portion.toInt()} ${stringResource(R.string.gram_short)} • ${entry.calories.toInt()} ${stringResource(R.string.calories_short)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "Б:${entry.protein.toInt()} Ж:${entry.fat.toInt()} У:${entry.carbs.toInt()}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
        MealTypeBadge(mealType = entry.mealType)
    }
}

@Composable
fun MealTypeBadge(mealType: MealType) {
    val text = when (mealType) {
        MealType.BREAKFAST -> stringResource(R.string.breakfast)
        MealType.LUNCH -> stringResource(R.string.lunch)
        MealType.DINNER -> stringResource(R.string.dinner)
        MealType.SNACK -> stringResource(R.string.snack)
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(text = text, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
    }
}