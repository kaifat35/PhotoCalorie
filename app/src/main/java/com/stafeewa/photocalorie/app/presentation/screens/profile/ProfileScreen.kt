package com.stafeewa.photocalorie.app.presentation.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.stafeewa.photocalorie.app.R
import com.stafeewa.photocalorie.app.domain.entity.ActivityLevel
import com.stafeewa.photocalorie.app.presentation.ui.theme.textFieldColors
import java.io.File

private const val GENDER_MALE = "male"
private const val GENDER_FEMALE = "female"

private fun normalizeGender(gender: String?): String? {
    return when (gender?.trim()?.lowercase()) {
        "мужской", "male" -> GENDER_MALE
        "женский", "female" -> GENDER_FEMALE
        else -> null
    }
}

fun getImageUriFromPath(imagePath: String?): Uri? {
    return if (!imagePath.isNullOrEmpty()) {
        try {
            val file = File(imagePath)
            if (file.exists()) Uri.fromFile(file) else null
        } catch (e: Exception) {
            null
        }
    } else {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onCalculateRate: () -> Unit = {},
    onSaveProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val stateProfile by viewModel.stateProfile.collectAsStateWithLifecycle()
    val editableProfile by viewModel.editableProfile.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val configuration = LocalConfiguration.current
    val context = LocalContext.current

    var genderMenu by remember { mutableStateOf(false) }
    var expandedActivity by remember { mutableStateOf(false) }
    val genderLevels = listOf(GENDER_MALE, GENDER_FEMALE)

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.processCommand(ProfileCommand.UpdateImage(it.toString()))
        }
    }

    // Обработка сообщений (snackbar)
    LaunchedEffect(Unit) {
        viewModel.uiMessages.collect { message ->
            val text = when (message) {
                is UiMessage.Resource -> {
                    if (message.args.isNotEmpty()) {
                        context.getString(message.resId, *message.args)
                    } else {
                        context.getString(message.resId)
                    }
                }
                is UiMessage.Plain -> message.text
            }
            snackbarHostState.showSnackbar(text)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 96.dp)) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.Settings),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        fontFamily = FontFamily(Font(R.font.jura)),
                        fontSize = 24.sp,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(bottom = 104.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 38.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF009E1D))
                        }
                    } else {
                        // Заголовок
                        Text(
                            text = stringResource(R.string.Your_profile),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                            fontFamily = FontFamily(Font(R.font.jura)),
                            fontSize = 36.sp,
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Аватар
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                                .clickable { imagePicker.launch("image/*") }
                        ) {
                            val imageUri = getImageUriFromPath(editableProfile.imageUri)
                            if (imageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        ImageRequest.Builder(context)
                                            .data(imageUri)
                                            .crossfade(true)
                                            .build()
                                    ),
                                    contentDescription = "Profile Image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_add_photo),
                                        contentDescription = "Add Photo",
                                        modifier = Modifier.size(155.dp)
                                    )
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_user_image),
                                        contentDescription = "User Image",
                                        modifier = Modifier.size(121.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { imagePicker.launch("image/*") },
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(top = 8.dp)
                                .height(44.dp),
                            shape = RoundedCornerShape(30.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                stringResource(R.string.Add_a_photo),
                                fontFamily = FontFamily(Font(R.font.jura)),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }

                        // Login
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.name),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily(Font(R.font.jura)),
                                fontSize = 24.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            OutlinedTextField(
                                value = editableProfile.login,
                                onValueChange = { viewModel.processCommand(ProfileCommand.UpdateLogin(it)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(30.dp),
                                colors = textFieldColors()
                            )
                        }

                        // Gender
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.Gender),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily(Font(R.font.jura)),
                                fontSize = 24.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Button(
                                onClick = { genderMenu = !genderMenu },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(30.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        when (normalizeGender(editableProfile.gender)) {
                                            GENDER_MALE -> stringResource(R.string.Male)
                                            GENDER_FEMALE -> stringResource(R.string.Female)
                                            else -> stringResource(R.string.Choose_a_gender)
                                        },
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontFamily = FontFamily(Font(R.font.jura)),
                                        fontSize = 24.sp
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.down),
                                        contentDescription = "Gender",
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            if (genderMenu) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(30.dp)
                                        )
                                        .padding(vertical = 8.dp)
                                ) {
                                    genderLevels.forEach { level ->
                                        val localizedLevel = when (level) {
                                            GENDER_MALE -> stringResource(R.string.Male)
                                            GENDER_FEMALE -> stringResource(R.string.Female)
                                            else -> level
                                        }
                                        Button(
                                            onClick = {
                                                viewModel.processCommand(ProfileCommand.UpdateGender(level))
                                                genderMenu = false
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp),
                                            shape = RoundedCornerShape(0.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (normalizeGender(editableProfile.gender) == level) {
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                } else {
                                                    Color.Transparent
                                                }
                                            )
                                        ) {
                                            Text(
                                                localizedLevel,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontFamily = FontFamily(Font(R.font.jura)),
                                                fontSize = 24.sp,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Height
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.Height_m),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily(Font(R.font.jura)),
                                fontSize = 24.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            OutlinedTextField(
                                value = editableProfile.heightStr,
                                onValueChange = { newValue ->
                                    if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                        viewModel.processCommand(ProfileCommand.UpdateHeightStr(newValue))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(30.dp),
                                colors = textFieldColors()
                            )
                        }

                        // Weight
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.Weight_kg),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily(Font(R.font.jura)),
                                fontSize = 24.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            OutlinedTextField(
                                value = editableProfile.weightStr,
                                onValueChange = { newValue ->
                                    if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                        viewModel.processCommand(ProfileCommand.UpdateWeightStr(newValue))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(30.dp),
                                colors = textFieldColors()
                            )
                        }

                        // Age
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.Age),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily(Font(R.font.jura)),
                                fontSize = 24.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            OutlinedTextField(
                                value = editableProfile.ageStr,
                                onValueChange = { newValue ->
                                    if (newValue.matches(Regex("^\\d*$"))) {
                                        viewModel.processCommand(ProfileCommand.UpdateAgeStr(newValue))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(30.dp),
                                colors = textFieldColors()
                            )
                        }

                        // Activity Level
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.activity_level),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily(Font(R.font.jura)),
                                fontSize = 24.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            ExposedDropdownMenuBox(
                                expanded = expandedActivity,
                                onExpandedChange = { expandedActivity = it }
                            ) {
                                OutlinedTextField(
                                    value = stringResource(editableProfile.activityLevel.titleRes),
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedActivity) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    shape = RoundedCornerShape(30.dp),
                                    colors = textFieldColors()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedActivity,
                                    onDismissRequest = { expandedActivity = false }
                                ) {
                                    ActivityLevel.values().forEach { level ->
                                        DropdownMenuItem(
                                            text = { Text(stringResource(level.titleRes)) },
                                            onClick = {
                                                viewModel.processCommand(ProfileCommand.UpdateActivityLevel(level))
                                                expandedActivity = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Email
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.Mail),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily(Font(R.font.jura)),
                                fontSize = 24.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            OutlinedTextField(
                                value = editableProfile.email,
                                onValueChange = { viewModel.processCommand(ProfileCommand.UpdateMail(it)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                shape = RoundedCornerShape(30.dp),
                                colors = textFieldColors()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Кнопка сохранения профиля
                        Button(
                            onClick = {
                                viewModel.processCommand(ProfileCommand.SaveProfile)
                                onSaveProfile()
                            },
                            modifier = Modifier
                                .wrapContentSize()
                                .height(44.dp),
                            shape = RoundedCornerShape(30.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                stringResource(R.string.Save),
                                fontFamily = FontFamily(Font(R.font.jura)),
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Кнопка расчёта нормы калорий
                        Button(
                            onClick = {
                                val gender = normalizeGender(editableProfile.gender) ?: ""
                                val height = editableProfile.getHeight() ?: 0.0
                                val weight = editableProfile.getWeight() ?: 0.0
                                val age = editableProfile.getAge() ?: 0
                                viewModel.processCommand(
                                    ProfileCommand.Calculate(
                                        gender = gender,
                                        height = height,
                                        weight = weight,
                                        age = age,
                                        activityLevel = editableProfile.activityLevel
                                    )
                                )
                                onCalculateRate()
                            },
                            modifier = Modifier
                                .wrapContentSize()
                                .height(44.dp),
                            shape = RoundedCornerShape(30.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                stringResource(R.string.Calculate_the_rate),
                                fontFamily = FontFamily(Font(R.font.jura)),
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}