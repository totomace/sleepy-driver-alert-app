package com.example.sleepydriverapp.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import com.example.sleepydriverapp.ui.components.Header

@Composable
fun MainScreen(
    isMonitoring: Boolean,
    onToggleMonitoring: (Boolean) -> Unit,
    onShowInstructions: () -> Unit,
    onShowSettings: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF9F9FF)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Header(
                onShowInstructions = onShowInstructions,
                onShowSettings = onShowSettings
            )

            // Nút Switch iOS custom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    CustomSwitch(
                        checked = isMonitoring,
                        onCheckedChange = onToggleMonitoring
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isMonitoring) "Đang giám sát" else "Chưa giám sát",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 16.sp,
                    color = Color(0xFF333333)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    width: Dp = 175.dp,
    height: Dp = 90.dp,
    padding: Dp = 8.dp
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
        label = "trackColor"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) (width - height + padding) else padding,
        label = "thumbOffset"
    )

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
            .clickable { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .size(height - padding * 2)
                .offset(x = thumbOffset)
                .align(Alignment.CenterStart)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
