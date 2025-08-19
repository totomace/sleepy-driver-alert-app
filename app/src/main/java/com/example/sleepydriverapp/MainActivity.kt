package com.example.sleepydriverapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.CircleShape

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
    val autoStart: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepyDriverScreen() {
    var isDetectionEnabled by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var appSettings by remember { mutableStateOf(AppSettings()) }
    val context = LocalContext.current

    // Lấy thông tin màn hình
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val isSmallScreen = screenHeight < 600.dp
    val isNarrowScreen = screenWidth < 360.dp

    // Responsive sizes dựa trên kích thước màn hình
    val logoSize = when {
        isSmallScreen -> 100.dp
        screenHeight < 700.dp -> 120.dp
        else -> 140.dp
    }

    val titleFontSize = when {
        isNarrowScreen -> 24.sp
        isSmallScreen -> 28.sp
        else -> 32.sp
    }

    val headerFontSize = when {
        isNarrowScreen -> 16.sp
        else -> 18.sp
    }

    val statusFontSize = when {
        isSmallScreen -> 20.sp
        else -> 24.sp
    }

    val toggleSize = when {
        isSmallScreen -> Pair(140.dp, 70.dp)
        else -> Pair(160.dp, 80.dp)
    }

    val horizontalPadding = when {
        isNarrowScreen -> 12.dp
        else -> 16.dp
    }

    val verticalSpacing = when {
        isSmallScreen -> 0.6f
        screenHeight < 700.dp -> 0.8f
        else -> 1f
    }

    // Auto start khi mở app (nếu được bật)
    LaunchedEffect(appSettings.autoStart) {
        if (appSettings.autoStart && !isDetectionEnabled) {
            isDetectionEnabled = true
        }
    }

    // --- Animation cho logo ---
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

    // Hiệu ứng vòng sáng (pulse border) - tốc độ phụ thuộc vào sensitivity
    val pulseSpeed = (2000 * (2 - appSettings.sensitivity)).toInt()
    val animatedBorder by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = pulseSpeed, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_animation"
    )

    // Màu theme dựa trên night mode
    val primaryColor = if (appSettings.nightMode) Color(0xFF4FC3F7) else Color(0xFFFF6B35)
    val backgroundColor = if (appSettings.nightMode) Color(0xFF121212) else Color(0xFFF5F5F5)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // --- Header ---
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
                                            if (appSettings.nightMode) Color.White else Color(0xFF666666),
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

        // Spacer item
        item {
            Spacer(modifier = Modifier.height((60 * verticalSpacing).dp))
        }

        // --- Logo chính ---
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
                            // Vẽ border nhấp nháy
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(primaryColor.copy(alpha = 0.4f), Color.Transparent)
                                ),
                                radius = size.width / 2 + animatedBorder
                            )
                        }
                        .background(
                            brush = Brush.radialGradient(
                                colors = if (appSettings.nightMode) {
                                    listOf(
                                        Color(0xFF4FC3F7),
                                        Color(0xFF29B6F6),
                                        Color(0xFF03A9F4)
                                    )
                                } else {
                                    listOf(
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
                        if (appSettings.nightMode) "🌙" else "👁",
                        fontSize = (logoSize.value * 0.35f).sp
                    )
                }

                Spacer(modifier = Modifier.height((24 * verticalSpacing).dp))

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

        // Spacer item
        item {
            Spacer(modifier = Modifier.height((80 * verticalSpacing).dp))
        }

// --- Toggle Switch ---
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val knobOffset by animateDpAsState(
                    targetValue = if (isDetectionEnabled) toggleSize.first - toggleSize.second else 0.dp,
                    animationSpec = tween(durationMillis = 400), // thời gian trượt
                    label = "knob_animation"
                )

                Box(
                    modifier = Modifier
                        .width(toggleSize.first)
                        .height(toggleSize.second)
                        .background(
                            color = if (isDetectionEnabled) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(toggleSize.second / 2)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            isDetectionEnabled = !isDetectionEnabled
                            val message = if (isDetectionEnabled)
                                "✅ Bắt đầu giám sát ngủ gật"
                            else
                                "⏸️ Dừng giám sát ngủ gật"

                            if (appSettings.soundEnabled) {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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

                Spacer(modifier = Modifier.height((24 * verticalSpacing).dp))

                Text(
                    text = if (isDetectionEnabled) "Đang giám sát" else "Tạm dừng",
                    fontSize = statusFontSize,
                    fontWeight = FontWeight.Medium,
                    color = if (isDetectionEnabled) Color(0xFF4CAF50) else
                        if (appSettings.nightMode) Color.White else Color(0xFF666666)
                )

                Spacer(modifier = Modifier.height((8 * verticalSpacing).dp))

                Text(
                    text = if (isDetectionEnabled)
                        "Ứng dụng đang theo dõi dấu hiệu ngủ gật của bạn\nĐộ nhạy: ${(appSettings.sensitivity * 100).toInt()}%"
                    else
                        "Bật chế độ giám sát để bắt đầu",
                    fontSize = if (isSmallScreen) 12.sp else 14.sp,
                    color = if (appSettings.nightMode) Color(0xFFBBBBBB) else Color(0xFF888888),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = if (isNarrowScreen) 16.dp else 32.dp),
                    lineHeight = if (isSmallScreen) 16.sp else 18.sp
                )
            }
        }

        // Spacer item
        item {
            Spacer(modifier = Modifier.height((40 * verticalSpacing).dp))
        }

        // --- Safety Card ---
        if (isDetectionEnabled) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = (32 * verticalSpacing).dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (appSettings.nightMode) Color(0xFF1E1E1E) else Color(0xFFE3F2FD)
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
                                color = if (appSettings.nightMode) Color(0xFF4FC3F7) else Color(0xFF1976D2)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Giữ điện thoại ở vị trí ổn định\n• Đảm bảo có đủ ánh sáng\n• Không che khuất camera trước\n• Dừng xe ngay nếu cảm thấy buồn ngủ",
                            fontSize = if (isSmallScreen) 11.sp else 13.sp,
                            color = if (appSettings.nightMode) Color(0xFFCCCCCC) else Color(0xFF424242),
                            lineHeight = if (isSmallScreen) 14.sp else 18.sp
                        )
                    }
                }
            }
        }
    }

    // --- Help Dialog ---
    if (showHelpDialog) {
        Dialog(onDismissRequest = { showHelpDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(max = screenHeight * 0.8f), // Giới hạn chiều cao
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (appSettings.nightMode) Color(0xFF1E1E1E) else Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                ) {
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

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HelpItem("1️⃣", "Đặt điện thoại nhìn thấy mặt", appSettings.nightMode, isSmallScreen)
                        HelpItem("2️⃣", "Bật chế độ giám sát", appSettings.nightMode, isSmallScreen)
                        HelpItem("3️⃣", "Ứng dụng sẽ cảnh báo khi ngủ gật", appSettings.nightMode, isSmallScreen)
                        HelpItem("⚠️", "Luôn ưu tiên an toàn khi lái xe", appSettings.nightMode, isSmallScreen)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { showHelpDialog = false },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
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

    // --- Settings Dialog ---
    if (showSettingsDialog) {
        Dialog(onDismissRequest = { showSettingsDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(max = screenHeight * 0.85f), // Giới hạn chiều cao
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

                    item {
                        Divider(color = if (appSettings.nightMode) Color(0xFF333333) else Color(0xFFE0E0E0))
                    }

                    // Sound Settings
                    item {
                        SettingWithToggle(
                            icon = "🔊",
                            title = "Âm thanh cảnh báo",
                            description = "Bật/tắt âm báo khi phát hiện ngủ gật",
                            checked = appSettings.soundEnabled,
                            onCheckedChange = { appSettings = appSettings.copy(soundEnabled = it) },
                            nightMode = appSettings.nightMode,
                            isSmallScreen = isSmallScreen
                        )
                    }

                    if (appSettings.soundEnabled) {
                        item {
                            SettingWithSlider(
                                icon = "🔈",
                                title = "Âm lượng",
                                value = appSettings.soundVolume,
                                onValueChange = { appSettings = appSettings.copy(soundVolume = it) },
                                valueText = "${(appSettings.soundVolume * 100).toInt()}%",
                                nightMode = appSettings.nightMode,
                                isSmallScreen = isSmallScreen
                            )
                        }
                    }

                    item {
                        Divider(color = if (appSettings.nightMode) Color(0xFF333333) else Color(0xFFE0E0E0))
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

                    // Vibration
                    item {
                        SettingWithToggle(
                            icon = "📳",
                            title = "Rung cảnh báo",
                            description = "Rung điện thoại khi phát hiện ngủ gật",
                            checked = appSettings.vibrationEnabled,
                            onCheckedChange = { appSettings = appSettings.copy(vibrationEnabled = it) },
                            nightMode = appSettings.nightMode,
                            isSmallScreen = isSmallScreen
                        )
                    }

                    // Auto Start
                    item {
                        SettingWithToggle(
                            icon = "🚀",
                            title = "Tự động bắt đầu",
                            description = "Tự động giám sát khi mở ứng dụng",
                            checked = appSettings.autoStart,
                            onCheckedChange = { appSettings = appSettings.copy(autoStart = it) },
                            nightMode = appSettings.nightMode,
                            isSmallScreen = isSmallScreen
                        )
                    }

                    item { Spacer(modifier = Modifier.height(12.dp)) }

                    // Buttons
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isNarrowScreen)
                                Arrangement.SpaceBetween else
                                Arrangement.spacedBy(8.dp, Alignment.End)
                        ) {
                            // Reset Button
                            OutlinedButton(
                                onClick = {
                                    appSettings = AppSettings() // Reset về mặc định
                                    Toast.makeText(context, "Đã reset về cài đặt mặc định", Toast.LENGTH_SHORT).show()
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

                            // Close Button
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

// --- Theme ---
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