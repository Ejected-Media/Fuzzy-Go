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
