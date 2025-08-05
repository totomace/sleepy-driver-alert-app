package com.example.sleepydriverapp.ui.screen

import android.Manifest
import android.content.Context
import android.graphics.Rect as AndroidRect
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MonitorScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var faceBox by remember { mutableStateOf<AndroidRect?>(null) }
    var previewSize by remember { mutableStateOf(Size(1, 1)) }

    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }

    if (cameraPermissionState.status.isGranted) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = {
                    val previewView = PreviewView(it).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        post {
                            previewSize = Size(width, height)
                        }
                    }

                    startCamera(context, lifecycleOwner, previewView) { rect ->
                        faceBox = rect
                    }

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            faceBox?.let { rect ->
                val density = LocalDensity.current

                val imageWidth = 720f
                val imageHeight = 1280f

                val scaleX = previewSize.width / imageWidth
                val scaleY = previewSize.height / imageHeight

                // Lật chiều ngang nếu là camera trước
                val mirroredLeft = imageWidth - rect.right
                val mirroredRight = imageWidth - rect.left

                val leftDp = with(density) { (mirroredLeft * scaleX).toDp() }
                val topDp = with(density) { (rect.top * scaleY).toDp() }
                val widthDp = with(density) { (rect.width() * scaleX).toDp() }
                val heightDp = with(density) { (rect.height() * scaleY).toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = leftDp, y = topDp)
                        .size(widthDp, heightDp)
                        .border(2.dp, Color.Yellow)
                        .align(Alignment.TopStart)
                )
            }
        }
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Ứng dụng cần quyền CAMERA để hoạt động", color = Color.Red)
        }
    }
}

private fun startCamera(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    onFaceDetected: (AndroidRect?) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val executor = Executors.newSingleThreadExecutor()

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setTargetRotation(Surface.ROTATION_0)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetResolution(Size(720, 1280)) // Portrait
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(executor, FaceAnalyzer(onFaceDetected))
            }

        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalyzer
        )
    }, ContextCompat.getMainExecutor(context))
}

class FaceAnalyzer(
    private val onFaceDetected: (AndroidRect?) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .enableTracking()
            .build()
    )

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        onFaceDetected(faces[0].boundingBox)
                    } else {
                        onFaceDetected(null)
                    }
                }
                .addOnFailureListener {
                    onFaceDetected(null)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
