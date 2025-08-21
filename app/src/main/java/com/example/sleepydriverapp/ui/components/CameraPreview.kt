package com.example.sleepydriverapp.ui.components

import android.content.Context
import android.os.Build
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.sleepydriverapp.data.model.AppSettings
import com.example.sleepydriverapp.data.model.DetectionState
import com.example.sleepydriverapp.viewmodel.SleepyDriverViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.*

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreview(
    viewModel: SleepyDriverViewModel,
    appSettings: AppSettings,
    context: Context,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    var camera: Camera? by remember { mutableStateOf(null) }
    var cameraSetupInProgress by remember { mutableStateOf(false) }

    // Face detector configuration
    val faceDetector = remember {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(appSettings.sensitivity)
            .enableTracking()
            .build()
        FaceDetection.getClient(options)
    }

    fun setupCamera(
        cameraProvider: ProcessCameraProvider,
        previewView: PreviewView?,
        enable: Boolean
    ) {
        try {
            if (enable) {
                cameraSetupInProgress = true
            }

            cameraProvider.unbindAll()

            if (enable && previewView != null) {
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setTargetRotation(previewView.display.rotation)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalyzer.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    if (!enable || cameraSetupInProgress) {
                        imageProxy.close()
                        return@setAnalyzer
                    }

                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        faceDetector.process(image)
                            .addOnSuccessListener { faces ->
                                val currentTime = System.currentTimeMillis()
                                val newState = if (faces.isNotEmpty()) {
                                    val face = faces[0]
                                    val leftEyeOpenProbability = face.leftEyeOpenProbability
                                    val rightEyeOpenProbability = face.rightEyeOpenProbability

                                    val threshold = 0.4f // Ngưỡng mắt mở (EYE_CLOSED_THRESHOLD)
                                    val eyesOpen = leftEyeOpenProbability != null &&
                                            rightEyeOpenProbability != null &&
                                            leftEyeOpenProbability > threshold &&
                                            rightEyeOpenProbability > threshold
                                    val eyesClosed = !eyesOpen

                                    val eyesClosedDuration = if (eyesClosed) {
                                        val startTime = viewModel.detectionState.value.eyesClosedStartTime
                                        if (startTime == 0L) {
                                            // Ghi nhận thời điểm bắt đầu nhắm mắt
                                            viewModel.detectionState.value = viewModel.detectionState.value.copy(
                                                eyesClosedStartTime = currentTime
                                            )
                                            0L
                                        } else {
                                            currentTime - startTime
                                        }
                                    } else {
                                        // Reset nếu mắt mở lại
                                        viewModel.detectionState.value = viewModel.detectionState.value.copy(
                                            eyesClosedStartTime = 0L
                                        )
                                        0L
                                    }

                                    // 👉 Điều kiện bỏ qua chớp mắt nhanh < 800ms
                                    if (eyesClosed && eyesClosedDuration < 800L) {
                                        // Bỏ qua, coi như mắt mở
                                        DetectionState(
                                            faceDetected = true,
                                            eyesOpen = true,
                                            eyesClosed = false,
                                            eyesClosedDuration = 0L,
                                            confidenceScore = ((leftEyeOpenProbability ?: 0f) + (rightEyeOpenProbability ?: 0f)) / 2f,
                                            eyesClosedStartTime = viewModel.detectionState.value.eyesClosedStartTime
                                        )
                                    } else {
                                        // Nhắm mắt lâu hơn 800ms thì mới cập nhật
                                        DetectionState(
                                            faceDetected = true,
                                            eyesOpen = eyesOpen,
                                            eyesClosed = eyesClosed,
                                            eyesClosedDuration = eyesClosedDuration,
                                            confidenceScore = ((leftEyeOpenProbability ?: 0f) + (rightEyeOpenProbability ?: 0f)) / 2f,
                                            eyesClosedStartTime = viewModel.detectionState.value.eyesClosedStartTime
                                        )
                                    }
                                } else {
                                    viewModel.detectionState.value = viewModel.detectionState.value.copy(
                                        eyesClosedStartTime = 0L
                                    )
                                    DetectionState(
                                        faceDetected = false,
                                        eyesOpen = true,
                                        eyesClosed = false,
                                        eyesClosedDuration = 0L,
                                        confidenceScore = 0f
                                    )
                                }

                                viewModel.updateDetectionState(newState, context)
                            }
                            .addOnFailureListener { e ->
                                android.util.Log.e("SleepyDriver", "Face detection failed", e)
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )

                if (enable) {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(200L) // Wait for camera to stabilize
                        cameraSetupInProgress = false
                    }
                }
            } else {
                cameraSetupInProgress = false
            }
        } catch (exc: Exception) {
            android.util.Log.e("SleepyDriver", "Use case binding failed", exc)
            cameraSetupInProgress = false
            android.widget.Toast.makeText(context, "Lỗi khởi tạo camera", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                cameraProvider = cameraProviderFuture.get()
                setupCamera(cameraProvider!!, previewView, isEnabled)
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier.fillMaxSize()
    )

    LaunchedEffect(isEnabled) {
        if (cameraProvider != null) {
            if (!isEnabled) {
                setupCamera(cameraProvider!!, null, false)
            } else {
                delay(600L) // Sync with toggle animation duration
                setupCamera(cameraProvider!!, null, true)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            faceDetector.close()
            cameraProvider?.unbindAll()
        }
    }
}