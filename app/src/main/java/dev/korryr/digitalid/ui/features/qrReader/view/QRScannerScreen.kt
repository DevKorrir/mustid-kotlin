package dev.korryr.digitalid.ui.features.qrReader.view

import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dev.korryr.digitalid.ui.features.dataModel.Result
import dev.korryr.digitalid.ui.features.qrReader.model.MLKitQRCodeAnalyzer
import dev.korryr.digitalid.ui.features.qrReader.presentation.ErrorDisplay
import dev.korryr.digitalid.ui.features.qrReader.presentation.QRScannerOverlay
import dev.korryr.digitalid.ui.features.qrReader.presentation.StudentDetailsCard
import dev.korryr.digitalid.ui.features.qrReader.presentation.ZoomableImage
import dev.korryr.digitalid.ui.features.qrReader.viewModel.QRScannerViewModel

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    modifier: Modifier = Modifier,
    viewModel: QRScannerViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // For showing full-screen image when the student image is tapped
    var showImageDialog by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf("") }

    var showExpirationAlert by remember { mutableStateOf(false) }
    var alertErrorMessage by remember { mutableStateOf("") }

    // State to track if a QR code has been scanned
    val studentState by viewModel.studentState
    val hasScannedQR = studentState is Result.Success

    // State for reset function
    var isScanning by remember { mutableStateOf(true) }

    // Request camera permission using Accompanist
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    LaunchedEffect(Unit) { cameraPermissionState.launchPermissionRequest() }

    LaunchedEffect(studentState) {
        if (studentState is Result.Error) {
            val error = studentState as Result.Error
            if (error.message.contains("expired", ignoreCase = true)) {
                alertErrorMessage =
                    "This QR Code has expired. Please ask the student to generate a new one."
                showExpirationAlert = true
            } else {
                // For other errors, you might want to show a toast or handle differently
                Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
            }
        }
    }


    if (showExpirationAlert) {
        AlertDialog(
            onDismissRequest = {
                showExpirationAlert = false
                isScanning = true
                viewModel.resetScan()
            },
            title = {
                Text(
                    "QR Code Expired",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    alertErrorMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExpirationAlert = false
                        isScanning = true
                        viewModel.resetScan()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Try Again")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MUST Student ID", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (hasScannedQR) {
                FloatingActionButton(
                    onClick = {
                        isScanning = true
                        viewModel.resetScan()
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan Again"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (cameraPermissionState.status.isGranted && isScanning) {
                // Camera Preview Section
                Box(modifier = Modifier.fillMaxSize()) {
                    // Camera Preview using CameraX and ML Kit
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { previewView ->
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = androidx.camera.core.Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                // Use the ML Kit analyzer
                                val analyzer = MLKitQRCodeAnalyzer { qrCode ->
                                    viewModel.scanQRCode(qrCode)
                                    isScanning = false
                                }

                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build().also {
                                        it.setAnalyzer(
                                            ContextCompat.getMainExecutor(context),
                                            analyzer
                                        )
                                    }

                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageAnalysis
                                    )
                                } catch (exc: Exception) {
                                    Log.e("QRScannerScreen", "Error binding camera", exc)
                                }
                            }, ContextCompat.getMainExecutor(context))
                        }
                    )

                    // QR Frame overlay with cute design
                    QRScannerOverlay()

                    // Loading indicator while processing QR code
                    if (studentState is Result.Loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier.padding(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(0.65f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Finding student...", fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

//                    // Error Display
//                    AnimatedVisibility(
//                        visible = studentState is Result.Error,
//                        enter = fadeIn(),
//                        exit = fadeOut()
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .background(Color.Black.copy(alpha = 0.7f)),
//                            contentAlignment = Alignment.Center
//                        ) {
//
//                            val error = studentState as? Result.Error
//                            val errorMessage = if (error != null && error.message.contains("expired", ignoreCase = true)) {
//                                "This QR Code has expired. Please ask the student to generate a new one."
//                            } else {
//                                error?.message ?: "Unknown error"
//                            }
//
//                            ErrorDisplay(
//                                message = errorMessage,
//                                onDismiss = {
//                                    isScanning = true
//                                    viewModel.resetScan()
//                                }
//                            )

//                            ErrorDisplay(
//                                message = if (studentState is Result.Error) {
//                                    (studentState as Result.Error).message
//                                } else "Unknown error"
//                            ) {
//                                isScanning = true
//                                viewModel.resetScan()
//                            }

                       // }
                  //  }
                }
            } else if (!cameraPermissionState.status.isGranted) {
                // Permission not granted view
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Camera permission is required to scan QR codes.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { cameraPermissionState.launchPermissionRequest() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }

            // Student Details Card when QR is scanned
            AnimatedVisibility(
                visible = hasScannedQR && !isScanning,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    (studentState as? Result.Success)?.data?.let { student ->
                        StudentDetailsCard(
                            student = student,
                            onImageClick = { imageUrl ->
                                selectedImageUrl = imageUrl
                                showImageDialog = true
                            },
                            onScanAgain = {
                                isScanning = true
                                viewModel.resetScan()
                            }
                        )
                    }
                }
            }

            // Full-screen image dialog

            if (showImageDialog) {
                // Full-screen overlay with a semi-transparent background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .clickable { /* Consume clicks to prevent closing */ },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        // Use a Column to stack the zoomable image and the close button.
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { showImageDialog = false },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "close"
                                    )
                                }

                                Spacer(modifier = Modifier.width(34.dp))

                                Text(
                                    text = "Student ID Photo",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            // Zoomable image area
                            ZoomableImage(
                                painter = rememberAsyncImagePainter(model = selectedImageUrl),
                                contentDescription = "Student ID Photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)    // take as much space as possible in the Column
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            )
                            // Close button
                            Button(
                                onClick = { showImageDialog = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    }
}


