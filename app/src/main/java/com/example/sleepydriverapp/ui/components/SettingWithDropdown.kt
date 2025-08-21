package com.example.sleepydriverapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingWithDropdown(
    icon: String,
    title: String,
    items: List<Pair<String, Int>>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit, // Kiểu phải là (Int) -> Unit
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
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
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