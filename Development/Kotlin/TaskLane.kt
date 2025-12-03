package com.ejectedmedia.fuzzygo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ejectedmedia.fuzzygo.model.FuzzyCard
import com.ejectedmedia.fuzzygo.model.FuzzyPriority

@Composable
fun TaskLane(
    title: String,
    cards: List<FuzzyCard>,
    modifier: Modifier = Modifier,
    onCardClick: (String) -> Unit = {},
    onAddCardClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .width(300.dp) // Fixed width for columns usually looks best in Kanban
            .fillMaxHeight()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF3F4F6) // Light grey background for the lane itself
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // --- Lane Header ---
            LaneHeader(
                title = title,
                count = cards.size,
                onAddClick = onAddCardClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- Scrollable List of Cards ---
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp) // Gap between cards
            ) {
                items(cards) { card ->
                    TaskCard(
                        card = card,
                        modifier = Modifier
                            // Placeholder for click handling
                            // .clickable { onCardClick(card.id) } 
                    )
                }
            }
        }
    }
}

@Composable
fun LaneHeader(
    title: String,
    count: Int,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Badge for card count
            Surface(
                color = Color.LightGray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(50),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = count.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Action Buttons (Add / Menu)
        Row {
            IconButton(onClick = onAddClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Card",
                    tint = Color.DarkGray
                )
            }
            IconButton(onClick = { /* TODO: Lane Options */ }, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = Color.DarkGray
                )
            }
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun PreviewTaskLane() {
    val dummyCards = listOf(
        FuzzyCard("1", "Fix database schema", "The schema is not normalized", FuzzyPriority.HIGH, listOf("DB")),
        FuzzyCard("2", "Update Android Icon", "Make it look more 'Fuzzy'", FuzzyPriority.LOW, listOf("Design")),
        FuzzyCard("3", "Investigate memory leak", "Happens on rotation", FuzzyPriority.CRITICAL, listOf("Bug"))
    )

    // Dark background behind the lane to verify the lane's light grey color
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(16.dp)
    ) {
        TaskLane(
            title = "In Progress",
            cards = dummyCards
        )
    }
}
