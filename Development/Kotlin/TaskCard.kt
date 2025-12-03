package com.ejectedmedia.fuzzygo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ejectedmedia.fuzzygo.model.FuzzyCard
import com.ejectedmedia.fuzzygo.model.FuzzyPriority

@Composable
fun TaskCard(
    card: FuzzyCard,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp), // Space between cards
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White) // Clean look
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Row for Title and Priority Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = card.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                // A little badge for priority
                PriorityBadge(card.priority)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = card.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tags Row (Simple implementation)
            Row {
                card.tags.forEach { tag ->
                    AssistChip(
                        onClick = { },
                        label = { Text("#$tag", fontSize = 12.sp) },
                        modifier = Modifier.padding(end = 4.dp).height(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PriorityBadge(priority: FuzzyPriority) {
    val color = when(priority) {
        FuzzyPriority.CRITICAL -> Color(0xFFFF5252) // Red
        FuzzyPriority.FUZZY -> Color(0xFFE040FB)    // Purple (Our brand color!)
        FuzzyPriority.HIGH -> Color(0xFFFFAB40)     // Orange
        FuzzyPriority.LOW -> Color(0xFF69F0AE)      // Green
    }
    
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = priority.name,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// --- PREVIEW ---
// This lets you see the UI in Android Studio without running the app!
@Preview(showBackground = true)
@Composable
fun PreviewTaskCard() {
    val dummyCard = FuzzyCard(
        id = "1",
        title = "Fix Login Crash",
        description = "App crashes when user enters a blank password.",
        priority = FuzzyPriority.FUZZY,
        tags = listOf("Bug", "Auth")
    )
    
    // Applying a light gray background so we can see the white card
    Surface(color = Color(0xFFF0F0F0), modifier = Modifier.padding(16.dp)) {
        TaskCard(card = dummyCard)
    }
}
