package com.example.adressnote.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adressnote.network.Note
import com.example.adressnote.network.NotesApiService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteBottomSheet(
    address: String,
    building: String,
    entrance: String,
    lat: Double,
    lng: Double,
    openedAt: Long,
    apiService: NotesApiService,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    var noteText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(openedAt) {
        isLoading = true
        val existing = apiService.getNote(address, building, entrance)
        noteText = existing?.note ?: ""
        isLoading = false
    }

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
                text = "$address, $building",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Подъезд $entrance",
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Заметка") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (noteText.isNotBlank()) {
                        scope.launch {
                            apiService.saveNote(
                                Note(
                                    address = address,
                                    building = building,
                                    entrance = entrance,
                                    note = noteText,
                                    lat = lat,
                                    lng = lng
                                )
                            )
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.align(Alignment.End),
                enabled = !isLoading && noteText.isNotBlank()
            ) {
                Text("Сохранить")
            }
        }
    }
}