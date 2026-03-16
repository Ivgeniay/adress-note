package com.example.adressnote.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adressnote.settings.SettingsManager
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    settingsManager: SettingsManager,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var radius by remember { mutableFloatStateOf(settingsManager.notificationRadius.toFloat()) }
    var trackingEnabled by remember { mutableStateOf(settingsManager.trackingEnabled) }
    var trackingInterval by remember { mutableFloatStateOf(settingsManager.trackingInterval.toFloat()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Настройки",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Отслеживание локации",
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = trackingEnabled,
                    onCheckedChange = { trackingEnabled = it }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Радиус уведомления",
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${radius.roundToInt()} м",
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = radius,
                onValueChange = { radius = it },
                valueRange = 10f..200f,
                steps = 18,
                modifier = Modifier.fillMaxWidth(),
                enabled = trackingEnabled
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "10 м", fontSize = 12.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "200 м", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Интервал опроса сервера",
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${trackingInterval.roundToInt()} сек",
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = trackingInterval,
                onValueChange = { trackingInterval = it },
                valueRange = 10f..120f,
                steps = 10,
                modifier = Modifier.fillMaxWidth(),
                enabled = trackingEnabled
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "10 сек", fontSize = 12.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "120 сек", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    settingsManager.notificationRadius = radius.roundToInt()
                    settingsManager.trackingEnabled = trackingEnabled
                    settingsManager.trackingInterval = trackingInterval.roundToInt()
                    onDismiss()
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Сохранить")
            }
        }
    }
}