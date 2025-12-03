package com.ejectedmedia.fuzzygo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ejectedmedia.fuzzygo.model.FuzzyPriority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardDialog(
    laneTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, priority: FuzzyPriority) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(FuzzyPriority.LOW) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Add to $laneTitle", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                // Priority Selector
                Text("Priority:", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FuzzyPriority.values().forEach { priority ->
                        PriorityChip(
                            priority = priority,
                            isSelected = priority == selectedPriority,
                            onSelect = { selectedPriority = priority }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, description, selectedPriority)
                    }
                },
                // Disable button if title is empty
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PriorityChip(
    priority: FuzzyPriority,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val color = when(priority) {
        FuzzyPriority.CRITICAL -> Color(0xFFFF5252)
        FuzzyPriority.FUZZY -> Color(0xFFE040FB)
        FuzzyPriority.HIGH -> Color(0xFFFFAB40)
        FuzzyPriority.LOW -> Color(0xFF69F0AE)
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) color else Color.Transparent,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, color) else null,
        modifier = Modifier
            .toggleable(value = isSelected, onValueChange = { onSelect() })
    ) {
        Text(
            text = priority.name.take(1), // Just first letter (L, H, C, F) to save space
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else color,
            fontWeight = FontWeight.Bold
        )
    }
}
