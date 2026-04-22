package com.stafeewa.photocalorie.app.presentation.screens.camera

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.stafeewa.photocalorie.app.R
import com.stafeewa.photocalorie.app.presentation.screens.foodmain.FoodIntakeViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel()
) {
    val foodIntakeViewModel: FoodIntakeViewModel = hiltViewModel()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val recognitionResult by viewModel.recognitionResult.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraExecutor by remember { mutableStateOf<ExecutorService?>(null) }

    var showResultDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }

    LaunchedEffect(recognitionResult) {
        if (recognitionResult != null) {
            showResultDialog = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (cameraPermissionState.status.isGranted) {
                val previewView = PreviewView(context)
                android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                ).also { previewView.layoutParams = it }

                DisposableEffect(Unit) {
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build()
                        imageCapture = ImageCapture.Builder().build()

                        preview.setSurfaceProvider(previewView.surfaceProvider)

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(context))

                    cameraExecutor = Executors.newSingleThreadExecutor()

                    onDispose {
                        cameraExecutor?.shutdown()
                    }
                }

                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )

                CaptureAreaOverlay(
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = {
                        imageCapture?.let { capture ->
                            viewModel.captureAndRecognize(
                                capture,
                                cameraExecutor ?: return@let,
                                context
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 104.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = "Сделать фото",
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                }
                if (isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF009E1D))
                        Text(
                            text = stringResource(R.string.We_recognize_it),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 60.dp)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.There_is_no_access_to_the_camera),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Button(
                        onClick = { cameraPermissionState.launchPermissionRequest() },
                        modifier = Modifier.padding(top = 60.dp)
                    ) {
                        Text(stringResource(R.string.Request_permission))
                    }
                }
            }
        }
    }

    if (showResultDialog && recognitionResult != null) {
        RecognitionResultDialog(
            result = recognitionResult!!,
            productRepository = viewModel.productRepository,
            onDismiss = {
                showResultDialog = false
                viewModel.clearResult()
            },
            onConfirm = { name, mealType, portion, protein, fat, carbs ->
                foodIntakeViewModel.addFoodEntry(name, mealType, portion, protein, fat, carbs)
                viewModel.onRecognitionConfirmed(name)
                showResultDialog = false
                viewModel.clearResult()
            },
            onAddToDatabase = { name, mealType, protein, fat, carbs ->
                viewModel.addNewFoodToDatabase(name, mealType, protein, fat, carbs)
            }
        )

    }
}

@Composable
private fun CaptureAreaOverlay(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        ) {
            drawRect(color = Color(0xFFF8FFF9).copy(alpha = 0.18f))

            val holeWidth = size.width * 0.72f
            val holeHeight = size.height * 0.52f
            val left = (size.width - holeWidth) / 2f
            val top = (size.height - holeHeight) / 2f

            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(holeWidth, holeHeight),
                cornerRadius = CornerRadius(32f, 32f),
                blendMode = BlendMode.Clear
            )

            drawRoundRect(
                color = Color.White.copy(alpha = 0.85f),
                topLeft = Offset(left, top),
                size = Size(holeWidth, holeHeight),
                cornerRadius = CornerRadius(32f, 32f),
                style = Stroke(width = 4f)
            )
        }

        Text(
            text = stringResource(R.string.Place_the_dish_in_the_frame),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 92.dp)
                .background(Color.White.copy(alpha = 0.18f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
