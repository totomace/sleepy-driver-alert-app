package com.example.sleepydriverapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.delay
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SleepyDriverTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF5F5F5)
                ) {
                    SleepyDriverScreen()
                }
            }
        }
    }
}

// Data class cho settings
data class AppSettings(
    val sensitivity: Float = 0.5f, // 0.0 - 1.0
    val soundEnabled: Boolean = true,
    val soundVolume: Float = 0.8f, // 0.0 - 1.0
    val nightMode: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val autoStart: Boolean = false,
    val alertThreshold: Long = 800L, // milliseconds - thời gian nhắm mắt tối đa
    val soundResourceId: Int = R.raw.beep_beep_43875 // Mặc định là beep_beep_43875.mp3
)

// Data class cho trạng thái phát hiện
data class DetectionState(
    val faceDetected: Boolean = false,
    val eyesOpen: Boolean = true,
    val eyesClosed: Boolean = false,
    val eyesClosedDuration: Long = 0L,
    val confidenceScore: Float = 0f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepyDriverScreen() {
    var isDetectionEnabled by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var appSettings by remember { mutableStateOf(AppSettings()) }
    var detectionState by remember { mutableStateOf(DetectionState()) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }

    // THÊM: Biến để kiểm soát delay và trạng thái chuyển đổi
    var isToggling by remember { mutableStateOf(false) }
    var cameraInitialized by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Check camera permission
    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Permission launcher với delay đồng bộ
    val coroutineScope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted

        if (isGranted) {
            if (isDetectionEnabled) {
                coroutineScope.launch {
                    delay(450L) // Delay đồng bộ với animation toggle
                    showCamera = true
                    delay(100L) // Thêm 100ms cho camera khởi tạo
                    cameraInitialized = true
                    isToggling = false
                    Toast.makeText(context, "✅ Bắt đầu giám sát ngủ gật", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Cần quyền camera để hoạt động", Toast.LENGTH_LONG).show()
            coroutineScope.launch {
                delay(100L)
                isDetectionEnabled = false
                showCamera = false
                cameraInitialized = false
                isToggling = false
            }
        }
    }

    // Responsive sizes
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val isSmallScreen = screenHeight < 600.dp
    val isNarrowScreen = screenWidth < 360.dp

    val logoSize = when {
        isSmallScreen -> 80.dp
        screenHeight < 700.dp -> 100.dp
        else -> 120.dp
    }

    val titleFontSize = when {
        isNarrowScreen -> 20.sp
        isSmallScreen -> 24.sp
        else -> 28.sp
    }

    val headerFontSize = when {
        isNarrowScreen -> 14.sp
        else -> 16.sp
    }

    val statusFontSize = when {
        isSmallScreen -> 18.sp
        else -> 22.sp
    }

    val toggleSize = when {
        isSmallScreen -> Pair(120.dp, 60.dp)
        else -> Pair(140.dp, 70.dp)
    }

    val horizontalPadding = when {
        isNarrowScreen -> 12.dp
        else -> 16.dp
    }

    val verticalSpacing = when {
        isSmallScreen -> 0.5f
        screenHeight < 700.dp -> 0.7f
        else -> 1f
    }

    // Auto start với delay đồng bộ
    LaunchedEffect(appSettings.autoStart) {
        if (appSettings.autoStart && !isDetectionEnabled && hasCameraPermission && !isToggling) {
            isToggling = true
            isDetectionEnabled = true
            delay(450L) // Đồng bộ với animation
            showCamera = true
            delay(100L) // Camera khởi tạo
            cameraInitialized = true
            isToggling = false
        }
    }

    // Logo animation
    val infiniteTransition = rememberInfiniteTransition(label = "logo_animation")
    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_animation"
    )

    // Pulse border based on detection state
    val pulseSpeed =
        if (detectionState.eyesClosed) 800 else (2000 * (2 - appSettings.sensitivity)).toInt()
    val animatedBorder by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (detectionState.eyesClosed) 25f else 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = pulseSpeed, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_animation"
    )

    // Theme colors
    val primaryColor = when {
        detectionState.eyesClosed -> Color(0xFFFF3030) // Red when eyes closed
        !detectionState.faceDetected && isDetectionEnabled -> Color(0xFFFFA500) // Orange when no face
        appSettings.nightMode -> Color(0xFF4FC3F7)
        else -> Color(0xFFFF6B35)
    }
    val backgroundColor = if (appSettings.nightMode) Color(0xFF121212) else Color(0xFFF5F5F5)

    // State trigger để điều khiển delay
    var startMonitoringTrigger by remember { mutableStateOf(false) }
    var stopMonitoringTrigger by remember { mutableStateOf(false) }

// Hàm xử lý toggle detection
    val handleToggleDetection = {
        if (!isToggling) {
            isToggling = true
            if (!isDetectionEnabled) {
                // Bật giám sát
                if (hasCameraPermission) {
                    isDetectionEnabled = true
                    startMonitoringTrigger = true // Kích hoạt trigger để bật camera sau animation
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            } else {
                // Tắt giám sát
                isDetectionEnabled = false
                stopMonitoringTrigger = true // Kích hoạt trigger để tắt camera sau animation
            }
        }
    }

// LaunchedEffect xử lý bật
    LaunchedEffect(startMonitoringTrigger) {
        if (startMonitoringTrigger) {
            delay(400L) // Chờ animation hoàn tất (400ms)
            if (hasCameraPermission) {
                showCamera = true
                delay(100L) // Thêm delay nhỏ để đảm bảo camera khởi tạo
                cameraInitialized = true
                Toast.makeText(context, "✅ Bắt đầu giám sát ngủ gật", Toast.LENGTH_SHORT).show()
            }
            isToggling = false
            startMonitoringTrigger = false // Reset trigger
        }
    }

// LaunchedEffect xử lý tắt
    LaunchedEffect(stopMonitoringTrigger) {
        if (stopMonitoringTrigger) {
            delay(400L) // Chờ animation hoàn tất (400ms)
            showCamera = false
            cameraInitialized = false
            detectionState = DetectionState()
            Toast.makeText(context, "⏸️ Dừng giám sát ngủ gật", Toast.LENGTH_SHORT).show()
            isToggling = false
            stopMonitoringTrigger = false // Reset trigger
        }
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = (16 * verticalSpacing).dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sleepy Driver Alert",
                        fontSize = headerFontSize,
                        fontWeight = FontWeight.SemiBold,
                        color = if (appSettings.nightMode) Color.White else Color(0xFF2D2D2D)
                    )
                }

                Row {
                    // Help Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { showHelpDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (appSettings.nightMode) Color.White else Color(0xFF666666)
                        )
                    }

                    // Settings Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { showSettingsDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Column {
                            repeat(3) { index ->
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(3.dp)
                                        .background(
                                            if (appSettings.nightMode) Color.White else Color(
                                                0xFF666666
                                            ),
                                            RoundedCornerShape(1.5.dp)
                                        )
                                )
                                if (index < 2) Spacer(modifier = Modifier.height(3.dp))
                            }
                        }
                    }
                }
            }
        }

        // Camera Preview - CHỈ HIỂN THỊ KHI ĐƯỢC BẬT VÀ ĐÃ KHỞI TẠO
        if (showCamera && isDetectionEnabled && cameraInitialized) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height((20 * verticalSpacing).dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        CameraPreview(
                            onFaceDetection = { newState ->
                                // Chỉ cập nhật state khi camera đã khởi tạo hoàn toàn
                                if (cameraInitialized && !isToggling) {
                                    detectionState = newState
                                }
                            },
                            appSettings = appSettings,
                            context = context,
                            isEnabled = isDetectionEnabled && cameraInitialized
                        )
                    }
                }
            }
        }

        // Loading indicator khi đang toggle
        if (isToggling && isDetectionEnabled) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height((20 * verticalSpacing).dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (appSettings.nightMode) Color(0xFF1E1E1E) else Color(0xFFF0F0F0)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = primaryColor,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Đang khởi tạo camera...",
                                    fontSize = 14.sp,
                                    color = if (appSettings.nightMode) Color(0xFFCCCCCC) else Color(0xFF666666)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Spacer
        item {
            Spacer(modifier = Modifier.height((30 * verticalSpacing).dp))
        }

        // Logo and status
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(logoSize)
                        .graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                            alpha = animatedAlpha
                        }
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.width / 2 + animatedBorder
                            )
                        }
                        .background(
                            brush = Brush.radialGradient(
                                colors = when {
                                    detectionState.eyesClosed -> listOf(
                                        Color(0xFFFF3030),
                                        Color(0xFFFF6B6B),
                                        Color(0xFFFFABAB)
                                    )

                                    !detectionState.faceDetected && isDetectionEnabled -> listOf(
                                        Color(0xFFFFA500),
                                        Color(0xFFFFB347),
                                        Color(0xFFFFD700)
                                    )

                                    appSettings.nightMode -> listOf(
                                        Color(0xFF4FC3F7),
                                        Color(0xFF29B6F6),
                                        Color(0xFF03A9F4)
                                    )

                                    else -> listOf(
                                        Color(0xFFFF6B35),
                                        Color(0xFFFF8E53),
                                        Color(0xFFFFA07A)
                                    )
                                },
                                radius = logoSize.value
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        when {
                            isToggling -> "⏳"
                            detectionState.eyesClosed -> "⚠️"
                            !detectionState.faceDetected && isDetectionEnabled -> "🔍"
                            appSettings.nightMode -> "🌙"
                            else -> "👁"
                        },
                        fontSize = (logoSize.value * 0.35f).sp
                    )
                }

                Spacer(modifier = Modifier.height((16 * verticalSpacing).dp))

                Text(
                    text = "SLEEP ALERT",
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    letterSpacing = if (isNarrowScreen) 1.sp else 2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        // Status info
        if (isDetectionEnabled) {
            item {
                Spacer(modifier = Modifier.height((16 * verticalSpacing).dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isToggling -> if (appSettings.nightMode) Color(0xFF1E1E1E) else Color(0xFFF0F0F0)
                            detectionState.eyesClosed -> Color(0xFFFFEBEE)
                            !detectionState.faceDetected -> Color(0xFFFFF3E0)
                            else -> if (appSettings.nightMode) Color(0xFF1E1E1E) else Color(
                                0xFFE8F5E8
                            )
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val statusText = when {
                            isToggling -> "⏳ Đang khởi tạo hệ thống..."
                            !cameraInitialized -> "🔄 Đang chuẩn bị camera..."
                            detectionState.eyesClosed -> "⚠️ CẢNH BÁO: Đang ngủ gật!"
                            !detectionState.faceDetected -> "🔍 Không phát hiện khuôn mặt"
                            else -> "✅ Đang giám sát bình thường"
                        }

                        val statusColor = when {
                            isToggling || !cameraInitialized -> Color(0xFFFF8F00)
                            detectionState.eyesClosed -> Color(0xFFD32F2F)
                            !detectionState.faceDetected -> Color(0xFFFF8F00)
                            else -> Color(0xFF2E7D32)
                        }

                        Text(
                            text = statusText,
                            fontSize = if (isSmallScreen) 14.sp else 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )

                        if (detectionState.faceDetected && cameraInitialized && !isToggling) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Độ tin cậy: ${(detectionState.confidenceScore * 100).toInt()}%",
                                fontSize = if (isSmallScreen) 12.sp else 14.sp,
                                color = if (appSettings.nightMode) Color(0xFFCCCCCC) else Color(
                                    0xFF666666
                                )
                            )

                            if (detectionState.eyesClosedDuration > 0) {
                                Text(
                                    text = "Thời gian nhắm mắt: ${detectionState.eyesClosedDuration}ms",
                                    fontSize = if (isSmallScreen) 12.sp else 14.sp,
                                    color = if (detectionState.eyesClosedDuration > appSettings.alertThreshold)
                                        Color(0xFFD32F2F) else Color(0xFF666666)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Spacer
        item {
            Spacer(modifier = Modifier.height((40 * verticalSpacing).dp))
        }

        // Toggle Switch với animation đồng bộ
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val knobOffset by animateDpAsState(
                    targetValue = if (isDetectionEnabled) toggleSize.first - toggleSize.second else 0.dp,
                    animationSpec = tween(durationMillis = 400), // Animation 400ms
                    label = "knob_animation"
                )

                Box(
                    modifier = Modifier
                        .width(toggleSize.first)
                        .height(toggleSize.second)
                        .background(
                            color = if (isDetectionEnabled) {
                                if (detectionState.eyesClosed) Color(0xFFFF3030)
                                else Color(0xFF4CAF50)
                            } else Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(toggleSize.second / 2)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            enabled = !isToggling // Vô hiệu hóa khi đang toggle
                        ) {
                            if (!isToggling) {
                                handleToggleDetection()
                            }
                        }
                        .padding(6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .offset(x = knobOffset)
                            .size(toggleSize.second - 12.dp)
                            .background(Color.White, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height((16 * verticalSpacing).dp))

                Text(
                    text = when {
                        isToggling -> if (isDetectionEnabled) "Đang khởi động..." else "Đang dừng..."
                        isDetectionEnabled -> "Đang giám sát"
                        else -> "Tạm dừng"
                    },
                    fontSize = statusFontSize,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        isToggling -> Color(0xFFFF8F00)
                        isDetectionEnabled -> {
                            if (detectionState.eyesClosed) Color(0xFFFF3030)
                            else Color(0xFF4CAF50)
                        }
                        else -> if (appSettings.nightMode) Color.White else Color(0xFF666666)
                    }
                )

                Spacer(modifier = Modifier.height((8 * verticalSpacing).dp))

                Text(
                    text = when {
                        isToggling -> "Vui lòng đợi hệ thống khởi tạo..."
                        isDetectionEnabled -> {
                            if (!hasCameraPermission) "Cần quyền truy cập camera"
                            else if (!cameraInitialized) "Đang chuẩn bị camera..."
                            else "AI đang phân tích khuôn mặt và mắt\nĐộ nhạy: ${(appSettings.sensitivity * 100).toInt()}%"
                        }
                        else -> "Bật chế độ giám sát để bắt đầu"
                    },
                    fontSize = if (isSmallScreen) 12.sp else 14.sp,
                    color = if (appSettings.nightMode) Color(0xFFBBBBBB) else Color(0xFF888888),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = if (isNarrowScreen) 16.dp else 32.dp),
                    lineHeight = if (isSmallScreen) 16.sp else 18.sp
                )
            }
        }

        // Safety Card
        if (isDetectionEnabled) {
            item {
                Spacer(modifier = Modifier.height((20 * verticalSpacing).dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (appSettings.nightMode) Color(0xFF1E1E1E) else Color(
                            0xFFE3F2FD
                        )
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💡", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lưu ý an toàn",
                                fontSize = if (isSmallScreen) 14.sp else 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (appSettings.nightMode) Color(0xFF4FC3F7) else Color(
                                    0xFF1976D2
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Giữ điện thoại ổn định, camera hướng về mặt\n• Đảm bảo ánh sáng đủ để camera hoạt động\n• Không che khuất camera trước\n• Dừng xe ngay khi có cảnh báo ngủ gật",
                            fontSize = if (isSmallScreen) 11.sp else 13.sp,
                            color = if (appSettings.nightMode) Color(0xFFCCCCCC) else Color(
                                0xFF424242
                            ),
                            lineHeight = if (isSmallScreen) 14.sp else 18.sp
                        )
                    }
                }
            }
        }
    }

    // Help Dialog
    if (showHelpDialog) {
        Dialog(onDismissRequest = { showHelpDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(max = screenHeight * 0.8f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (appSettings.nightMode) Color(0xFF1E1E1E) else Color.White
                )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📱", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hướng dẫn sử dụng",
                            fontSize = if (isSmallScreen) 18.sp else 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (appSettings.nightMode) Color.White else Color(0xFF2D2D2D)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        HelpItem(
                            "1️⃣",
                            "Cấp quyền camera cho ứng dụng",
                            appSettings.nightMode,
                            isSmallScreen
                        )
                        HelpItem(
                            "2️⃣",
                            "Đặt điện thoại nhìn thấy mặt bạn",
                            appSettings.nightMode,
                            isSmallScreen
                        )
                        HelpItem("3️⃣", "Bật chế độ giám sát", appSettings.nightMode, isSmallScreen)
                        HelpItem(
                            "4️⃣",
                            "AI sẽ phân tích và cảnh báo khi ngủ gật",
                            appSettings.nightMode,
                            isSmallScreen
                        )
                        HelpItem(
                            "⚠️",
                            "Luôn ưu tiên an toàn - dừng xe khi buồn ngủ",
                            appSettings.nightMode,
                            isSmallScreen
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { showHelpDialog = false },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text(
                            text = "Đã hiểu",
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = if (isSmallScreen) 12.sp else 14.sp
                        )
                    }
                }
            }
        }
    }

    // Settings Dialog
    if (showSettingsDialog) {
        Dialog(onDismissRequest = { showSettingsDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(max = screenHeight * 0.85f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (appSettings.nightMode) Color(0xFF1E1E1E) else Color.White
                )
            ) {
                LazyColumn(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("⚙️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cài đặt",
                                fontSize = if (isSmallScreen) 18.sp else 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (appSettings.nightMode) Color.White else Color(0xFF2D2D2D)
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // Sensitivity Setting
                    item {
                        SettingWithSlider(
                            icon = "🎯",
                            title = "Độ nhạy cảm biến",
                            value = appSettings.sensitivity,
                            onValueChange = { appSettings = appSettings.copy(sensitivity = it) },
                            valueText = "${(appSettings.sensitivity * 100).toInt()}%",
                            nightMode = appSettings.nightMode,
                            isSmallScreen = isSmallScreen
                        )
                    }

                    // Alert Threshold
                    item {
                        SettingWithSlider(
                            icon = "⏱️",
                            title = "Thời gian cảnh báo (giây)",
                            value = (appSettings.alertThreshold / 1000f) / 5f,
                            onValueChange = {
                                val newThreshold = (it * 5000).toLong().coerceIn(500L, 5000L)
                                appSettings = appSettings.copy(alertThreshold = newThreshold)
                            },
                            valueText = String.format("%.1f s", appSettings.alertThreshold / 1000f),
                            nightMode = appSettings.nightMode,
                            isSmallScreen = isSmallScreen
                        )
                    }

                    // Sound Enabled
                    item {
                        SettingWithToggle(
                            icon = "🔊",
                            title = "Âm thanh cảnh báo",
                            description = "Bật/tắt âm thanh cảnh báo khi phát hiện ngủ gật",
                            checked = appSettings.soundEnabled,
                            onCheckedChange = { appSettings = appSettings.copy(soundEnabled = it) },
                            nightMode = appSettings.nightMode,
                            isSmallScreen = isSmallScreen
                        )
                    }

                    // Sound Selection
                    if (appSettings.soundEnabled) {
                        item {
                            val soundOptions = listOf(
                                Pair("Âm thanh 1", R.raw.beep_beep_43875),
                                Pair("Âm thanh 2", R.raw.beep_warning_6387),
                                Pair("Âm thanh 3", R.raw.censor_beep_3_372460)
                            )
                            SettingWithDropdown(
                                icon = "🎵",
                                title = "Chọn âm thanh cảnh báo",
                                items = soundOptions,
                                selectedItem = appSettings.soundResourceId,
                                onItemSelected = {
                                    appSettings = appSettings.copy(soundResourceId = it)
                                },
                                nightMode = appSettings.nightMode,
                                isSmallScreen = isSmallScreen
                            )
                        }
                    }

                    // Sound Volume
                    if (appSettings.soundEnabled) {
                        item {
                            SettingWithSlider(
                                icon = "🔈",
                                title = "Âm lượng cảnh báo",
                                value = appSettings.soundVolume,
                                onValueChange = {
                                    appSettings = appSettings.copy(soundVolume = it)
                                },
                                valueText = "${(appSettings.soundVolume * 100).toInt()}%",
                                nightMode = appSettings.nightMode,
                                isSmallScreen = isSmallScreen
                            )
                        }
                    }

                    // Vibration Enabled
                    item {
                        SettingWithToggle(
                            icon = "📳",
                            title = "Rung cảnh báo",
                            description = "Rung điện thoại khi phát hiện ngủ gật",
                            checked = appSettings.vibrationEnabled,
                            onCheckedChange = {
                                appSettings = appSettings.copy(vibrationEnabled = it)
                            },
                            nightMode = appSettings.nightMode,
                            isSmallScreen = isSmallScreen
                        )
                    }

                    // Auto Start
                    item {
                        SettingWithToggle(
                            icon = "🚀",
                            title = "Tự động khởi động",
                            description = "Tự động bắt đầu giám sát khi mở ứng dụng",
                            checked = appSettings.autoStart,
                            onCheckedChange = { appSettings = appSettings.copy(autoStart = it) },
                            nightMode = appSettings.nightMode,
                            isSmallScreen = isSmallScreen
                        )
                    }

                    // Night Mode
                    item {
                        SettingWithToggle(
                            icon = "🌙",
                            title = "Chế độ ban đêm",
                            description = "Giao diện tối để sử dụng ban đêm",
                            checked = appSettings.nightMode,
                            onCheckedChange = { appSettings = appSettings.copy(nightMode = it) },
                            nightMode = appSettings.nightMode,
                            isSmallScreen = isSmallScreen
                        )
                    }

                    item {
                        Divider(
                            color = if (appSettings.nightMode) Color(0xFF333333) else Color(
                                0xFFE0E0E0
                            )
                        )
                    }

                    // Buttons
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isNarrowScreen)
                                Arrangement.SpaceBetween else
                                Arrangement.spacedBy(8.dp, Alignment.End)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    appSettings = AppSettings() // Reset về mặc định
                                    Toast.makeText(
                                        context,
                                        "Đã reset về cài đặt mặc định",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = primaryColor
                                )
                            ) {
                                Text(
                                    "Reset",
                                    fontSize = if (isSmallScreen) 12.sp else 14.sp
                                )
                            }

                            Button(
                                onClick = { showSettingsDialog = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor
                                )
                            ) {
                                Text(
                                    text = "Đóng",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = if (isSmallScreen) 12.sp else 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreview(
    onFaceDetection: (DetectionState) -> Unit,
    appSettings: AppSettings,
    context: Context,
    isEnabled: Boolean // Thêm tham số để kiểm soát bật/tắt camera
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    var camera: Camera? by remember { mutableStateOf(null) }
    var eyesClosedStartTime by remember { mutableStateOf(0L) }
    var lastAlertTime by remember { mutableStateOf(0L) }

    // THÊM: Biến để kiểm soát trạng thái camera
    var cameraSetupInProgress by remember { mutableStateOf(false) }

    // Face detector configuration
    val faceDetector = remember {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()
        FaceDetection.getClient(options)
    }

    // Custom vibration pattern
    val vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)

    // Alert functions
    fun playAlertSound() {
        if (!appSettings.soundEnabled) return
        try {
            val mediaPlayer = MediaPlayer.create(context, appSettings.soundResourceId)
            mediaPlayer?.apply {
                setVolume(appSettings.soundVolume, appSettings.soundVolume)
                start()
                setOnCompletionListener { release() }
            }
        } catch (e: Exception) {
            Log.e("SleepyDriver", "Lỗi khi phát âm thanh cảnh báo", e)
        }
    }

    fun vibrate() {
        if (!appSettings.vibrationEnabled) return
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(vibrationPattern, -1)
            }
        } catch (e: Exception) {
            Log.e("SleepyDriver", "Error vibrating", e)
        }
    }

    // THAY ĐỔI: setupCamera với delay và trạng thái đồng bộ
    fun setupCamera(
        cameraProvider: ProcessCameraProvider,
        previewView: PreviewView?,
        enable: Boolean
    ) {
        try {
            // Đánh dấu bắt đầu setup camera
            if (enable) {
                cameraSetupInProgress = true
            }

            // Luôn unbind trước khi thiết lập lại
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
                    // Kiểm tra trạng thái enable và setup completion
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

                                if (faces.isNotEmpty()) {
                                    val face = faces[0]
                                    val leftEyeOpenProbability = face.leftEyeOpenProbability
                                    val rightEyeOpenProbability = face.rightEyeOpenProbability

                                    val threshold = 0.5f - (appSettings.sensitivity * 0.3f)
                                    val eyesOpen = if (leftEyeOpenProbability != null && rightEyeOpenProbability != null) {
                                        leftEyeOpenProbability > threshold && rightEyeOpenProbability > threshold
                                    } else true

                                    val eyesClosed = !eyesOpen

                                    if (eyesClosed) {
                                        if (eyesClosedStartTime == 0L) {
                                            eyesClosedStartTime = currentTime
                                        }
                                    } else {
                                        eyesClosedStartTime = 0L
                                    }

                                    val eyesClosedDuration = if (eyesClosedStartTime > 0) {
                                        currentTime - eyesClosedStartTime
                                    } else 0L

                                    // Anti-spam alert logic
                                    if (eyesClosedDuration > appSettings.alertThreshold &&
                                        currentTime - lastAlertTime > 3000
                                    ) {
                                        playAlertSound()
                                        vibrate()
                                        lastAlertTime = currentTime
                                        Toast.makeText(
                                            context,
                                            "⚠️ Cảnh báo: Ngủ gật!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    onFaceDetection(
                                        DetectionState(
                                            faceDetected = true,
                                            eyesOpen = eyesOpen,
                                            eyesClosed = eyesClosed,
                                            eyesClosedDuration = eyesClosedDuration,
                                            confidenceScore = ((leftEyeOpenProbability ?: 0f) +
                                                    (rightEyeOpenProbability ?: 0f)) / 2f
                                        )
                                    )
                                } else {
                                    eyesClosedStartTime = 0L
                                    onFaceDetection(
                                        DetectionState(
                                            faceDetected = false,
                                            eyesOpen = true,
                                            eyesClosed = false,
                                            eyesClosedDuration = 0L,
                                            confidenceScore = 0f
                                        )
                                    )
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("SleepyDriver", "Face detection failed", e)
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

                // THÊM: Delay để đánh dấu camera đã setup xong
                if (enable) {
                    // Sử dụng coroutine scope để delay
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        delay(200L) // Delay 200ms để camera ổn định
                        cameraSetupInProgress = false
                    }
                }

            } else {
                cameraSetupInProgress = false
            }
        } catch (exc: Exception) {
            Log.e("SleepyDriver", "Use case binding failed", exc)
            cameraSetupInProgress = false
        }
    }

    // View hiển thị camera
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
        modifier = Modifier.fillMaxSize()
    )

    // THAY ĐỔI: Xử lý khi trạng thái isEnabled thay đổi với delay
    LaunchedEffect(isEnabled) {
        if (cameraProvider != null) {
            if (!isEnabled) {
                // Tắt camera ngay lập tức
                setupCamera(cameraProvider!!, null, false)
            } else {
                // Bật camera sau khi chờ animation
                delay(800L) // Đồng bộ với thời gian animation của toggle
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

@Composable
fun HelpItem(icon: String, text: String, nightMode: Boolean = false, isSmallScreen: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = icon,
            fontSize = if (isSmallScreen) 16.sp else 18.sp,
            modifier = Modifier.width(if (isSmallScreen) 28.dp else 32.dp)
        )
        Text(
            text = text,
            fontSize = if (isSmallScreen) 14.sp else 16.sp,
            color = if (nightMode) Color(0xFFCCCCCC) else Color(0xFF424242),
            modifier = Modifier.weight(1f),
            lineHeight = if (isSmallScreen) 18.sp else 20.sp
        )
    }
}

@Composable
fun SettingWithToggle(
    icon: String,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    nightMode: Boolean = false,
    isSmallScreen: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = if (isSmallScreen) 18.sp else 20.sp,
            modifier = Modifier.width(if (isSmallScreen) 35.dp else 40.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = if (isSmallScreen) 14.sp else 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (nightMode) Color.White else Color(0xFF2D2D2D)
            )
            Text(
                text = description,
                fontSize = if (isSmallScreen) 12.sp else 14.sp,
                color = if (nightMode) Color(0xFF999999) else Color(0xFF666666),
                lineHeight = if (isSmallScreen) 14.sp else 16.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFFF6B35),
                checkedTrackColor = Color(0xFFFF6B35).copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun SettingWithSlider(
    icon: String,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueText: String,
    nightMode: Boolean = false,
    isSmallScreen: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = if (isSmallScreen) 18.sp else 20.sp,
                modifier = Modifier.width(if (isSmallScreen) 35.dp else 40.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = if (isSmallScreen) 14.sp else 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (nightMode) Color.White else Color(0xFF2D2D2D)
                )
            }
            Text(
                text = valueText,
                fontSize = if (isSmallScreen) 12.sp else 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFFF6B35)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFF6B35),
                activeTrackColor = Color(0xFFFF6B35),
                inactiveTrackColor = if (nightMode) Color(0xFF333333) else Color(0xFFE0E0E0)
            ),
            modifier = Modifier.padding(start = if (isSmallScreen) 35.dp else 40.dp)
        )
    }
}

@Composable
fun SettingWithDropdown(
    icon: String,
    title: String,
    items: List<Pair<String, Int>>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    nightMode: Boolean = false,
    isSmallScreen: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = if (isSmallScreen) 18.sp else 20.sp,
                modifier = Modifier.width(if (isSmallScreen) 35.dp else 40.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = if (isSmallScreen) 14.sp else 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (nightMode) Color.White else Color(0xFF2D2D2D)
                )
                Text(
                    text = items.find { it.second == selectedItem }?.first ?: "Chọn âm thanh",
                    fontSize = if (isSmallScreen) 12.sp else 14.sp,
                    color = if (nightMode) Color(0xFF999999) else Color(0xFF666666)
                )
            }
            Box {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Chọn âm thanh",
                    tint = if (nightMode) Color.White else Color(0xFF2D2D2D)
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    items.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.first) },
                            onClick = {
                                onItemSelected(item.second)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SleepyDriverTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFFFF6B35),
            secondary = Color(0xFFFF8E53),
            background = Color(0xFFF5F5F5),
            surface = Color.White,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF2D2D2D),
            onSurface = Color(0xFF2D2D2D)
        ),
        content = content
    )
}