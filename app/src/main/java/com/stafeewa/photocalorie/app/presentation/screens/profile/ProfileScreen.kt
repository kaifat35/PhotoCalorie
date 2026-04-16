package com.stafeewa.photocalorie.app.presentation.screens.profile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.stafeewa.photocalorie.app.R
import com.stafeewa.photocalorie.app.presentation.ui.theme.textFieldColors
import java.io.File

@ExperimentalMaterial3Api
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onCalculateRate: () -> Unit,
    onSaveProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val stateProfile by viewModel.stateProfile.collectAsStateWithLifecycle()
    val editableProfile by viewModel.editableProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var bmrMenu by rememberSaveable { mutableStateOf(false) }
    val genderLevels = listOf(stringResource(R.string.Male), stringResource(R.string.Female))

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.processCommand(ProfileCommand.UpdateImage(it.toString()))
        }
    }

    // Функция для получения Uri из пути к файлу
    fun getImageUriFromPath(imagePath: String?): Uri? {
        return if (!imagePath.isNullOrEmpty()) {
            try {
                val file = File(imagePath)
                if (file.exists()) {
                    Uri.fromFile(file)
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("ProfileScreen", "Error creating URI from path", e)
                null
            }
        } else {
            null
        }
    }

    // Обработка состояний успеха/ошибки
    LaunchedEffect(stateProfile) {
        when (val state = stateProfile) {
            is ProfileState.Success -> {
                // Можно показать Snackbar или Toast
                println("Success: ${state.message}")
            }

            is ProfileState.Error -> {
                // Можно показать Snackbar или Toast с ошибкой
                println("Error: ${state.message}")
            }

            else -> {}
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
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
                            contentDescription = "Настройки",
                            tint = Color.White
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
                .padding(bottom = 104.dp)
        ) {
            item {
                if (viewModel.isLoading.collectAsStateWithLifecycle().value) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF009E1D))
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 38.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Text(
                            text = stringResource(R.string.Your_profile),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White
                            ),
                            fontFamily = FontFamily(Font(R.font.jura)),
                            fontSize = 36.sp,
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        // Аватар пользователя
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
                                fontSize = 24.sp,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.Login),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily(Font(R.font.jura)),
                                fontSize = 24.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            OutlinedTextField(
                                value = editableProfile.login,
                                onValueChange = {
                                    viewModel.processCommand(ProfileCommand.UpdateLogin(it))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Done
                                ),
                                shape = RoundedCornerShape(30.dp),
                                colors = textFieldColors()
                            )
                        }

                        // Кнопка "Пол"
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.Gender),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily(Font(R.font.jura)),
                                fontSize = 24.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Button(
                                onClick = { bmrMenu = !bmrMenu },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(30.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF5C5A5A)
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        editableProfile.gender ?: stringResource(R.string.Choose_a_gender),
                                        fontFamily = FontFamily(Font(R.font.jura)),
                                        fontSize = 24.sp
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.down),
                                        contentDescription = "Пол",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Подменю "Пол"
                            if (bmrMenu) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(30.dp)
                                        )
                                        .padding(vertical = 8.dp)
                                ) {
                                    genderLevels.forEach { level ->
                                        Button(
                                            onClick = {
                                                viewModel.processCommand(
                                                    ProfileCommand.UpdateGender(level)
                                                )
                                                bmrMenu = false
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp),
                                            shape = RoundedCornerShape(0.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (editableProfile.gender == level) Color(
                                                    0xFF313131
                                                ) else Color.Transparent,
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Text(
                                                level,
                                                fontFamily = FontFamily(Font(R.font.jura)),
                                                fontSize = 24.sp,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }

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
                                    // Разрешаем ввод только цифр и одной точки
                                    if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                        viewModel.processCommand(ProfileCommand.UpdateHeightStr(newValue))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Done
                                ),
                                shape = RoundedCornerShape(30.dp),
                                colors = textFieldColors()
                            )
                        }

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
                                shape = RoundedCornerShape(30.dp),
                                colors = textFieldColors()
                            )
                        }

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
                                    if (newValue.matches(Regex("^\\d*$"))) { // только цифры
                                        viewModel.processCommand(ProfileCommand.UpdateAgeStr(newValue))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                shape = RoundedCornerShape(30.dp),
                                colors = textFieldColors()
                            )
                        }

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
                                onValueChange = {
                                    viewModel.processCommand(ProfileCommand.UpdateMail(it))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(30.dp),
                                colors = textFieldColors()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        ButtonSaveProfile(
                            modifier = Modifier,
                            onSaveProfile = {
                                viewModel.processCommand(ProfileCommand.SaveProfile)
                                onSaveProfile()
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ButtonCalculateRate(
                            modifier = Modifier,
                            onCalculateRate = {
                                when (val state = stateProfile) {
                                    is ProfileState.Configuration -> {
                                        val config = state
                                        viewModel.processCommand(
                                            ProfileCommand.Calculate(
                                                gender = config.gender ?: "",
                                                height = config.height ?: 0.0,
                                                weight = config.weight ?: 0.0,
                                                age = config.age ?: 0
                                            )
                                        )
                                    }

                                    else -> {}
                                }
                                onCalculateRate()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ButtonSaveProfile(
    modifier: Modifier = Modifier,
    onSaveProfile: () -> Unit
) {
    Button(
        onClick = {
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
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun ButtonCalculateRate(
    modifier: Modifier = Modifier,
    onCalculateRate: () -> Unit
) {
    Button(
        onClick = {
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
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}