Great choice. A Kanban board isn't very useful if you can't add work to it.
We need a way for the user to input a Title, Description, and Priority. A standard Modal Dialog is perfect for this—it keeps the context of the board while focusing the user on the task creation.

Step 1: The "Add Card" Dialog UI
Create a new file: ui/components/AddCardDialog.kt.
We will use a AlertDialog from Material3. I'll add a simple text field for the title and a row of chips to select the Priority.

```
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
```

Step 2: Update the Repository
We need a proper createCard function that accepts all these details. Open data/FuzzyRepository.kt and replace/update the addCard helper we wrote earlier.

```
// In FuzzyRepository.kt

fun createCard(
    projectId: String, 
    laneId: String, 
    title: String, 
    description: String, 
    priority: FuzzyPriority
) {
    val cardData = hashMapOf(
        "title" to title,
        "description" to description,
        "lane_id" to laneId,
        "priority" to priority.name, // Store ENUM as String
        "tags" to emptyList<String>(), // Default empty tags for now
        "created_at" to com.google.firebase.Timestamp.now()
    )

    firestore.collection("projects").document(projectId)
        .collection("cards")
        .add(cardData)
        .addOnFailureListener { e ->
            // Log error
             println("Error creating card: ${e.message}")
        }
}
```

Step 3: Manage Dialog State in ViewModel
We need the ViewModel to know which lane we are adding a card to. If activeLaneId is null, the dialog is closed. If it has an ID, the dialog is open for that lane.
Open ui/viewmodel/BoardViewModel.kt:

```
// In BoardViewModel.kt

// Add this state to track the active dialog
private val _activeLaneIdForDialog = MutableStateFlow<String?>(null)
val activeLaneIdForDialog: StateFlow<String?> = _activeLaneIdForDialog

fun openAddCardDialog(laneId: String) {
    _activeLaneIdForDialog.value = laneId
}

fun closeAddCardDialog() {
    _activeLaneIdForDialog.value = null
}

fun createCard(title: String, description: String, priority: FuzzyPriority) {
    val laneId = _activeLaneIdForDialog.value ?: return
    
    viewModelScope.launch {
        repository.createCard(
            projectId = "my-first-project",
            laneId = laneId,
            title = title,
            description = description,
            priority = priority
        )
        // Close dialog after creating
        closeAddCardDialog()
    }
}
```

Step 4: Wire it all up in the Screen
Finally, update ProjectBoardScreen.kt. We need to:
 * Observe the dialog state.
 * Show the AddCardDialog if a lane ID is active.
 * Pass the open/close actions to the TaskLane.

```
<!-- end list -->
// In ProjectBoardScreen.kt

@Composable
fun ProjectBoardScreen(
    viewModel: BoardViewModel = ...
) {
    val lanes by viewModel.boardState.collectAsStateWithLifecycle()
    // Observe the dialog state
    val activeLaneId by viewModel.activeLaneIdForDialog.collectAsStateWithLifecycle()

    Scaffold(...) { paddingValues ->
        Box(...) {
            // ... LazyRow implementation ...
             items(lanes) { lane ->
                TaskLane(
                    // ... other params ...
                    onAddCardClick = { 
                        viewModel.openAddCardDialog(lane.id) // OPEN DIALOG
                    }
                )
            }
            
            // --- CONDITIONAL DIALOG RENDERING ---
            if (activeLaneId != null) {
                // Find the lane title for the header (optional nicety)
                val laneTitle = lanes.find { it.id == activeLaneId }?.title ?: "Lane"
                
                AddCardDialog(
                    laneTitle = laneTitle,
                    onDismiss = { viewModel.closeAddCardDialog() },
                    onConfirm = { title, desc, priority ->
                        viewModel.createCard(title, desc, priority)
                    }
                )
            }
        }
    }
}
```

The Result
Now, when you tap the "+" icon on the "To Do" column:
 * The AddCardDialog pops up.
 * You type "Fix the Bugs", select "Critical" (Red), and hit Create.
 * The dialog closes.
 * Instantaneously, the new card appears in the "To Do" column via the Firestore real-time listener.

You now have a fully functional CRUD loop (Create, Read, Update/Move).
Shall we wrap this up by creating a basic "Project Picker" screen so you aren't stuck on "my-first-project" forever, or would you prefer to refine the UI styling?
