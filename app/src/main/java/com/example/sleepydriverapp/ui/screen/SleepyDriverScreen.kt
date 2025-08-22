package com.example.sleepydriverapp.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sleepydriverapp.ui.components.*
import com.example.sleepydriverapp.viewmodel.SleepyDriverViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sleepydriverapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepyDriverScreen(viewModel: SleepyDriverViewModel = viewModel()) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val isSmallScreen = screenHeight < 600.dp
    val isNarrowScreen = screenWidth < 360.dp

    // Responsive sizes
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

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted, context)
    }

    // Check camera permission on start
    LaunchedEffect(Unit) {
        viewModel.checkCameraPermission(context)
    }

    // Handle auto start
    LaunchedEffect(viewModel.appSettings.value.autoStart) {
        viewModel.handleAutoStart()
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
    val pulseSpeed = when {
        viewModel.detectionState.value.eyesClosed -> 600
        !viewModel.detectionState.value.faceDetected && viewModel.isDetectionEnabled.value -> 1000
        else -> (2000 * (2 - viewModel.appSettings.value.sensitivity)).toInt()
    }

    val animatedBorder by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = when {
            viewModel.detectionState.value.eyesClosed -> 30f
            !viewModel.detectionState.value.faceDetected && viewModel.isDetectionEnabled.value -> 20f
            else -> 15f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = pulseSpeed, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_animation"
    )

    // Theme colors with more distinct states
    val primaryColor = when {
        viewModel.detectionState.value.eyesClosed -> Color(0xFFFF1744) // Bright red for danger
        !viewModel.detectionState.value.faceDetected && viewModel.isDetectionEnabled.value -> Color(0xFFF57C00) // Orange for warning
        viewModel.appSettings.value.nightMode -> Color(0xFF4FC3F7)
        else -> Color(0xFFFF6B35)
    }

    val backgroundColor = if (viewModel.appSettings.value.nightMode) Color(0xFF121212) else Color(0xFFF5F5F5)

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
                        color = if (viewModel.appSettings.value.nightMode) Color.White else Color(0xFF2D2D2D)
                    )
                }

                Row {
                    // Help button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { viewModel.showHelpDialog.value = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewModel.appSettings.value.nightMode) Color.White else Color(0xFF666666)
                        )
                    }

                    // Settings button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { viewModel.showSettingsDialog.value = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Column {
                            repeat(3) { index ->
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(3.dp)
                                        .background(
                                            if (viewModel.appSettings.value.nightMode) Color.White else Color(0xFF666666),
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

        // Camera Preview
        item {
            if (viewModel.showCamera.value && viewModel.isDetectionEnabled.value) {
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
                            containerColor = if (viewModel.appSettings.value.nightMode) Color(0xFF1E1E1E) else Color(0xFFF0F0F0)
                        )
                    ) {
                        if (viewModel.cameraInitialized.value) {
                            CameraPreview(
                                viewModel = viewModel,
                                appSettings = viewModel.appSettings.value,
                                context = context,
                                isEnabled = viewModel.isDetectionEnabled.value
                            )
                        } else {
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
                                        color = if (viewModel.appSettings.value.nightMode) Color(0xFFCCCCCC) else Color(0xFF666666)
                                    )
                                }
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
                                    viewModel.detectionState.value.eyesClosed -> listOf(
                                        Color(0xFFFF1744),
                                        Color(0xFFFF5252),
                                        Color(0xFFFFABAB)
                                    )
                                    !viewModel.detectionState.value.faceDetected && viewModel.isDetectionEnabled.value -> listOf(
                                        Color(0xFFF57C00),
                                        Color(0xFFFFB74D),
                                        Color(0xFFFFD54F)
                                    )
                                    viewModel.appSettings.value.nightMode -> listOf(
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
                            viewModel.isToggling.value -> "⏳"
                            viewModel.detectionState.value.eyesClosed -> "🚨"
                            !viewModel.detectionState.value.faceDetected && viewModel.isDetectionEnabled.value -> "🔍"
                            viewModel.appSettings.value.nightMode -> "🌙"
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
        item {
            if (viewModel.isDetectionEnabled.value || viewModel.isToggling.value) {
                Spacer(modifier = Modifier.height((16 * verticalSpacing).dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            viewModel.isToggling.value -> if (viewModel.appSettings.value.nightMode) Color(0xFF1E1E1E) else Color(0xFFF0F0F0)
                            viewModel.detectionState.value.eyesClosed -> Color(0xFFFFEBEE)
                            !viewModel.detectionState.value.faceDetected -> Color(0xFFFFF8E1)
                            else -> if (viewModel.appSettings.value.nightMode) Color(0xFF1E1E1E) else Color(0xFFE8F5E8)
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val statusText = when {
                            viewModel.isToggling.value -> "⏳ Đang khởi tạo hệ thống..."
                            !viewModel.cameraInitialized.value -> "📄 Đang chuẩn bị camera..."
                            viewModel.detectionState.value.eyesClosed -> "🚨 CẢNH BÁO: Đang ngủ gật!"
                            !viewModel.detectionState.value.faceDetected -> "🔍 Không phát hiện khuôn mặt"
                            else -> "✅ Đang giám sát bình thường"
                        }

                        val statusColor = when {
                            viewModel.isToggling.value || !viewModel.cameraInitialized.value -> Color(0xFFF57C00)
                            viewModel.detectionState.value.eyesClosed -> Color(0xFFD32F2F)
                            !viewModel.detectionState.value.faceDetected -> Color(0xFFF57C00)
                            else -> Color(0xFF2E7D32)
                        }

                        Text(
                            text = statusText,
                            fontSize = if (isSmallScreen) 14.sp else 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )

                        if (viewModel.detectionState.value.faceDetected &&
                            viewModel.cameraInitialized.value &&
                            !viewModel.isToggling.value) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Độ tin cậy: ${(viewModel.detectionState.value.confidenceScore * 100).toInt()}%",
                                fontSize = if (isSmallScreen) 12.sp else 14.sp,
                                color = if (viewModel.appSettings.value.nightMode) Color(0xFFCCCCCC) else Color(0xFF666666)
                            )

                            if (viewModel.detectionState.value.eyesClosedDuration > 0) {
                                Text(
                                    text = "Thời gian nhắm mắt: ${viewModel.detectionState.value.eyesClosedDuration}ms",
                                    fontSize = if (isSmallScreen) 12.sp else 14.sp,
                                    color = if (viewModel.detectionState.value.eyesClosedDuration > viewModel.appSettings.value.alertThreshold)
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

        // Custom Toggle Switch with smooth animation
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Custom animated toggle
                Box(
                    modifier = Modifier
                        .width(toggleSize.first)
                        .height(toggleSize.second)
                        .background(
                            color = when {
                                viewModel.toggleProgress.value > 0.5f -> {
                                    if (viewModel.detectionState.value.eyesClosed) Color(0xFFFF1744)
                                    else Color(0xFF4CAF50)
                                }
                                else -> Color(0xFFE0E0E0)
                            },
                            shape = RoundedCornerShape(toggleSize.second / 2)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            enabled = !viewModel.isToggling.value
                        ) {
                            viewModel.handleToggleDetection(context) {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                        .padding(6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Animated knob
                    Box(
                        modifier = Modifier
                            .offset(x = (toggleSize.first - toggleSize.second) * viewModel.toggleProgress.value)
                            .size(toggleSize.second - 12.dp)
                            .background(Color.White, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height((16 * verticalSpacing).dp))

                Text(
                    text = when {
                        viewModel.isToggling.value -> {
                            if (viewModel.isDetectionEnabled.value) "Đang khởi động..." else "Đang dừng..."
                        }

                        viewModel.isDetectionEnabled.value -> "Đang giám sát"
                        else -> "Tạm dừng"
                    },
                    fontSize = statusFontSize,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        viewModel.isToggling.value -> Color(0xFFF57C00)
                        viewModel.isDetectionEnabled.value -> {
                            if (viewModel.detectionState.value.eyesClosed) Color(0xFFFF1744)
                            else Color(0xFF4CAF50)
                        }
                        else -> if (viewModel.appSettings.value.nightMode) Color.White else Color(
                            0xFF666666
                        )
                    }
                )

                Spacer(modifier = Modifier.height((8 * verticalSpacing).dp))

                Text(
                    text = when {
                        viewModel.isToggling.value -> "Vui lòng đợi hệ thống khởi tạo..."
                        viewModel.toggleProgress.value > 0.5f -> {
                            if (!viewModel.hasCameraPermission.value) "Cần quyền truy cập camera"
                            else if (!viewModel.cameraInitialized.value) "Đang chuẩn bị camera..."
                            else "AI đang phân tích khuôn mặt và mắt\nĐộ nhạy: ${(viewModel.appSettings.value.sensitivity * 100).toInt()}%"
                        }
                        else -> "Bật chế độ giám sát để bắt đầu"
                    },
                    fontSize = if (isSmallScreen) 12.sp else 14.sp,
                    color = if (viewModel.appSettings.value.nightMode) Color(0xFFBBBBBB) else Color(0xFF888888),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = if (isNarrowScreen) 16.dp else 32.dp),
                    lineHeight = if (isSmallScreen) 16.sp else 18.sp
                )
            }
        }

        // Safety Card - only show when detection is active
        item {
            if (viewModel.toggleProgress.value > 0.5f) {
                Spacer(modifier = Modifier.height((20 * verticalSpacing).dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (viewModel.appSettings.value.nightMode) Color(0xFF1E1E1E) else Color(0xFFE3F2FD)
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
                                color = if (viewModel.appSettings.value.nightMode) Color(0xFF4FC3F7) else Color(0xFF1976D2)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Giữ điện thoại ổn định, camera hướng về mặt\n• Đảm bảo ánh sáng đủ để camera hoạt động\n• Không che khuất camera trước\n• Dừng xe ngay khi có cảnh báo ngủ gật",
                            fontSize = if (isSmallScreen) 11.sp else 13.sp,
                            color = if (viewModel.appSettings.value.nightMode) Color(0xFFCCCCCC) else Color(0xFF424242),
                            lineHeight = if (isSmallScreen) 14.sp else 18.sp
                        )
                    }
                }
            }
        }
    }

    // Help Dialog
    if (viewModel.showHelpDialog.value) {
        Dialog(onDismissRequest = { viewModel.showHelpDialog.value = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(max = screenHeight * 0.8f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.appSettings.value.nightMode) Color(0xFF1E1E1E) else Color.White
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
                            color = if (viewModel.appSettings.value.nightMode) Color.White else Color(0xFF2D2D2D)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        HelpItem(
                            "1️⃣",
                            "Cấp quyền camera cho ứng dụng",
                            viewModel.appSettings.value.nightMode,
                            isSmallScreen
                        )
                        HelpItem(
                            "2️⃣",
                            "Đặt điện thoại nhìn thấy mặt bạn",
                            viewModel.appSettings.value.nightMode,
                            isSmallScreen
                        )
                        HelpItem("3️⃣", "Bật chế độ giám sát", viewModel.appSettings.value.nightMode, isSmallScreen)
                        HelpItem(
                            "4️⃣",
                            "AI sẽ phân tích và cảnh báo khi ngủ gật",
                            viewModel.appSettings.value.nightMode,
                            isSmallScreen
                        )
                        HelpItem(
                            "⚠️",
                            "Luôn ưu tiên an toàn - dừng xe khi buồn ngủ",
                            viewModel.appSettings.value.nightMode,
                            isSmallScreen
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.showHelpDialog.value = false },
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
    if (viewModel.showSettingsDialog.value) {
        Dialog(onDismissRequest = { viewModel.showSettingsDialog.value = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(max = screenHeight * 0.85f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.appSettings.value.nightMode) Color(0xFF1E1E1E) else Color.White
                )
            ) {
                LazyColumn(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                                color = if (viewModel.appSettings.value.nightMode) Color.White else Color(0xFF2D2D2D)
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    item {
                        SettingWithSlider(
                            icon = "🎯",
                            title = "Độ nhạy cảm biến",
                            value = viewModel.appSettings.value.sensitivity,
                            onValueChange = { viewModel.appSettings.value = viewModel.appSettings.value.copy(sensitivity = it) },
                            valueText = "${(viewModel.appSettings.value.sensitivity * 100).toInt()}%",
                            nightMode = viewModel.appSettings.value.nightMode,
                            isSmallScreen = isSmallScreen
                        )
                    }

                    item {
                        SettingWithSlider(
                            icon = "⏱️",
                            title = "Thời gian cảnh báo (giây)",
                            value = (viewModel.appSettings.value.alertThreshold / 1000f) / 5f,
                            onValueChange = {
                                val newThreshold = (it * 5000).toLong().coerceIn(500L, 5000L)
                                viewModel.appSettings.value = viewModel.appSettings.value.copy(alertThreshold = newThreshold)
                            },
                            valueText = String.format("%.1f s", viewModel.appSettings.value.alertThreshold / 1000f),
                            nightMode = viewModel.appSettings.value.nightMode,
                            isSmallScreen = isSmallScreen
                        )
                    }

                    item {
                        SettingWithToggle(
                            icon = "🔊",
                            title = "Âm thanh cảnh báo",
                            description = "Bật/tắt âm thanh cảnh báo khi phát hiện ngủ gật",
                            checked = viewModel.appSettings.value.soundEnabled,
                            onCheckedChange = { viewModel.appSettings.value = viewModel.appSettings.value.copy(soundEnabled = it) },
                            nightMode = viewModel.appSettings.value.nightMode,
                            isSmallScreen = isSmallScreen
                        )
                    }

                    if (viewModel.appSettings.value.soundEnabled) {
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
                                selectedItem = viewModel.appSettings.value.soundResourceId,
                                onItemSelected = { selectedId: Int ->
                                    viewModel.appSettings.value = viewModel.appSettings.value.copy(soundResourceId = selectedId)
                                },
                                nightMode = viewModel.appSettings.value.nightMode,
                                isSmallScreen = isSmallScreen
                            )
                        }
                    }

                    if (viewModel.appSettings.value.soundEnabled) {
                        item {
                            SettingWithSlider(
                                icon = "📈",
                                title = "Âm lượng cảnh báo",
                                value = viewModel.appSettings.value.soundVolume,
                                onValueChange = { viewModel.appSettings.value = viewModel.appSettings.value.copy(soundVolume = it) },
                                valueText = "${(viewModel.appSettings.value.soundVolume * 100).toInt()}%",
                                nightMode = viewModel.appSettings.value.nightMode,
                                isSmallScreen = isSmallScreen
                            )
                        }
                    }

                    item {
                        SettingWithToggle(
                            icon = "📳",
                            title = "Rung cảnh báo",
                            description = "Rung điện thoại khi phát hiện ngủ gật",
                            checked = viewModel.appSettings.value.vibrationEnabled,
                            onCheckedChange = { viewModel.appSettings.value = viewModel.appSettings.value.copy(vibrationEnabled = it) },
                            nightMode = viewModel.appSettings.value.nightMode,
                            isSmallScreen = isSmallScreen
                        )
                    }

                    item {
                        SettingWithToggle(
                            icon = "🚀",
                            title = "Tự động khởi động",
                            description = "Tự động bắt đầu giám sát khi mở ứng dụng",
                            checked = viewModel.appSettings.value.autoStart,
                            onCheckedChange = { viewModel.appSettings.value = viewModel.appSettings.value.copy(autoStart = it) },
                            nightMode = viewModel.appSettings.value.nightMode,
                            isSmallScreen = isSmallScreen
                        )
                    }

                    item {
                        SettingWithToggle(
                            icon = "🌙",
                            title = "Chế độ ban đêm",
                            description = "Giao diện tối để sử dụng ban đêm",
                            checked = viewModel.appSettings.value.nightMode,
                            onCheckedChange = { viewModel.appSettings.value = viewModel.appSettings.value.copy(nightMode = it) },
                            nightMode = viewModel.appSettings.value.nightMode,
                            isSmallScreen = isSmallScreen
                        )
                    }

                    item {
                        Divider(
                            color = if (viewModel.appSettings.value.nightMode) Color(0xFF333333) else Color(0xFFE0E0E0)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isNarrowScreen) Arrangement.SpaceBetween else Arrangement.spacedBy(8.dp, Alignment.End)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.resetSettings(context) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
                            ) {
                                Text(
                                    "Reset",
                                    fontSize = if (isSmallScreen) 12.sp else 14.sp
                                )
                            }

                            Button(
                                onClick = { viewModel.showSettingsDialog.value = false },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
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