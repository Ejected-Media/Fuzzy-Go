package com.ejectedmedia.fuzzygo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ejectedmedia.fuzzygo.model.FuzzyCard
import com.ejectedmedia.fuzzygo.model.FuzzyLane
import com.ejectedmedia.fuzzygo.model.FuzzyPriority
import com.ejectedmedia.fuzzygo.ui.components.TaskLane

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectBoardScreen(
    lanes: List<FuzzyLane>,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Fuzzy-Go Mobile", 
                        fontWeight = FontWeight.Bold,
                        color = Color.White 
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE) // Fuzzy Purple
                ),
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Search, "Search", tint = Color.White)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Notifications, "Alerts", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        // The Board Area
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFE0E0E0)) // Darker grey background for the board "canvas"
                .padding(paddingValues)
        ) {
            LazyRow(
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(lanes) { lane ->
                    TaskLane(
                        title = lane.title,
                        cards = lane.cards,
                        onAddCardClick = { 
                            // TODO: Open "Add Card" Dialog for this lane
                        }
                    )
                }
            }
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true, widthDp = 800, heightDp = 500) // Wide preview to see multiple columns
@Composable
fun PreviewProjectBoard() {
    // 1. Setup Dummy Data
    val todoCards = listOf(
        FuzzyCard("1", "Setup Firestore", "Init the Go module and keys", FuzzyPriority.HIGH, listOf("Backend")),
        FuzzyCard("2", "Design App Icon", "Need a fuzzy creature logo", FuzzyPriority.LOW, listOf("Design"))
    )
    
    val inProgressCards = listOf(
        FuzzyCard("3", "Build Lane UI", "Compose LazyColumn implementation", FuzzyPriority.FUZZY, listOf("UI", "Dev"))
    )
    
    val doneCards = listOf(
        FuzzyCard("4", "Project Setup", "Repo created and .gitignore added", FuzzyPriority.LOW, listOf("Admin"))
    )

    val dummyLanes = listOf(
        FuzzyLane("l1", "To Do", todoCards),
        FuzzyLane("l2", "In Progress", inProgressCards),
        FuzzyLane("l3", "Done", doneCards),
        FuzzyLane("l4", "Icebox", emptyList())
    )

    // 2. Render
    ProjectBoardScreen(lanes = dummyLanes)
}
