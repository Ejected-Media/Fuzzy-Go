You chose the exciting path! Implementing Drag and Drop logic is the "boss fight" of Kanban board development. It requires coordinating the UI (gestures) with the Data (Firestore) perfectly.
We are going to use the modern Jetpack Compose Drag and Drop API (introduced in Compose 1.6). This allows us to drag data from one Composable (the Card) and drop it onto another (the Lane).

Here is the logic flow:
 * Repository: Needs a function to actually update the card's lane_id in Firestore.
 * UI (The Card): Needs to be a DragSource (something you can pick up).
 * UI (The Lane): Needs to be a DropTarget (something that accepts items).

Step 1: The Repository Logic (The Write)
Open data/FuzzyRepository.kt. We need to add the function that commits the move to the database.
Add this function to the class:

```
// In FuzzyRepository.kt

suspend fun moveCardToLane(projectId: String, cardId: String, newLaneId: String) {
    // We are running a Firestore Write operation
    firestore.collection("projects").document(projectId)
        .collection("cards").document(cardId)
        .update("lane_id", newLaneId)
        .addOnSuccessListener { 
            // Optional: Log success
            println("Moved card $cardId to lane $newLaneId") 
        }
        .addOnFailureListener { e ->
            // Optional: Handle error (maybe snackbar?)
            println("Failed to move card: ${e.message}")
        }
}
```

Step 2: The ViewModel Logic (The Handler)
Open ui/viewmodel/BoardViewModel.kt. The UI will call this when a drop happens.

```
// In BoardViewModel.kt

fun onCardDropped(cardId: String, newLaneId: String) {
    // 1. Optimistic Update (Optional but recommended): 
    // We could update the _boardState immediately so the user sees it move 
    // before the server responds. For now, we rely on the Firestore real-time listener 
    // we built earlier, which is usually fast enough ( < 100ms).

    viewModelScope.launch {
        // Hardcoded project ID for now
        repository.moveCardToLane("my-first-project", cardId, newLaneId)
    }
}
```

Step 3: The Drag and Drop UI Logic
This is the heavy lifting. We need to create a wrapper that makes any UI element "Draggable" and another wrapper that makes a container a "Drop Zone".
Create a new file: ui/components/DragDropWrappers.kt.

```
package com.ejectedmedia.fuzzygo.ui.components

import android.content.ClipData
import android.content.ClipDescription
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// --- 1. THE DRAGGABLE SOURCE (The Card) ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraggableCardWrapper(
    cardId: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.dragAndDropSource {
            detectTapGestures(
                onLongPress = { offset ->
                    // 1. Start the drag
                    startTransfer(
                        DragAndDropTransferData(
                            // We pass the Card ID as simple text data in the drag event
                            clipData = ClipData.newPlainText("cardId", cardId)
                        )
                    )
                }
            )
        }
    ) {
        content()
    }
}

// --- 2. THE DROP TARGET (The Lane) ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DropTargetLaneWrapper(
    laneId: String,
    onCardDropped: (cardId: String, laneId: String) -> Unit,
    content: @Composable BoxScope.(isHovering: Boolean) -> Unit
) {
    var isHovering by remember { mutableStateOf(false) }

    val dropTarget = remember(laneId) {
        object : DragAndDropTarget {
            // A. Only accept drops if they contain text (our card ID)
            override fun onStarted(event: DragAndDropEvent): Boolean {
                return event.toAndroidDragEvent().clipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true
            }

            // B. Highlight the lane when dragging over it
            override fun onEntered(event: DragAndDropEvent) {
                isHovering = true
            }

            override fun onExited(event: DragAndDropEvent) {
                isHovering = false
            }

            // C. Handle the drop!
            override fun onDrop(event: DragAndDropEvent): Boolean {
                isHovering = false
                val clipData = event.toAndroidDragEvent().clipData
                if (clipData != null && clipData.itemCount > 0) {
                    val cardId = clipData.getItemAt(0).text.toString()
                    onCardDropped(cardId, laneId)
                    return true
                }
                return false
            }
        }
    }

    Box(
        modifier = Modifier
            .dragAndDropTarget(
                shouldStartDragAndDrop = { event -> 
                    event.toAndroidDragEvent().clipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true 
                },
                target = dropTarget
            )
    ) {
        content(isHovering)
    }
}
```

Step 4: Wiring it up in the UI
Now we go back to our UI components and wrap them.
1. Update TaskCard.kt
Wrap the card content in our DraggableCardWrapper.

```
// In TaskCard.kt

@Composable
fun TaskCard(
    card: FuzzyCard,
    modifier: Modifier = Modifier
) {
    DraggableCardWrapper(cardId = card.id) {
        // ... The existing Card UI implementation goes here ...
        Card(modifier = modifier...) { ... }
    }
}
```

2. Update TaskLane.kt
Wrap the lane content in our DropTargetLaneWrapper. This is cool because we can change the background color when a user hovers a card over the lane!

```
// In TaskLane.kt

@Composable
fun TaskLane(
    title: String,
    laneId: String, // Add this parameter!
    cards: List<FuzzyCard>,
    onCardDrop: (String, String) -> Unit, // Add this callback!
    // ... existing params
) {
    DropTargetLaneWrapper(
        laneId = laneId,
        onCardDropped = onCardDrop
    ) { isHovering ->
        
        // Visual feedback: Darker grey if hovering
        val bgColor = if (isHovering) Color(0xFFE0E0E0) else Color(0xFFF3F4F6)

        Surface(
            modifier = modifier
                .width(300.dp)
                .fillMaxHeight()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = bgColor // Use the dynamic color
        ) {
            Column(...) {
                // ... Existing Header and LazyColumn ...
                // IMPORTANT: Ensure LazyColumn handles the drop target correctly 
                // by taking up the full space so you can drop anywhere in the lane.
            }
        }
    }
}
```

3. Update ProjectBoardScreen.kt
Pass the laneId and the ViewModel's drop function down to the Lane.

```
// In ProjectBoardScreen.kt

items(lanes) { lane ->
    TaskLane(
        title = lane.title,
        laneId = lane.id, // Pass ID
        cards = lane.cards,
        onAddCardClick = { viewModel.createTestCard(lane.id) },
        onCardDrop = { cardId, newLaneId -> 
            viewModel.onCardDropped(cardId, newLaneId) // Connect to ViewModel
        }
    )
}
```

The Result
You now have a fully functional logic loop:
 * User Long Presses Card: The card ID is loaded into the drag buffer.
 * User Drags to "Done": The "Done" lane detects the hover and turns slightly darker.
 * User Releases: The "Done" lane fires onCardDrop.
 * ViewModel: Calls repo.moveCardToLane.
 * Firestore: Updates the document.
 * Repository Listener: Sees the update, refreshes the lists, and the card "snaps" into the new lane on screen.
Would you like to move on to the next logic: "Adding New Cards" (The Floating Action Button or Dialog), or focus on setting up the Firestore data manually so you can test this?
