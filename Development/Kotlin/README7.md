This is the final piece of the core architecture. A "Project Picker" allows users to manage multiple boards (e.g., "Mobile App," "Backend API," "Marketing").
We need to implement:
 * The Project Data Model: To store project metadata.
 * The Repository Logic: To fetch the list of projects.
 * The UI: A screen to view and create projects.
 * Navigation: The wiring to move from the Picker -> Board.

Step 1: The Project Data Model
Create a new file: model/FuzzyProject.kt.

```
package com.ejectedmedia.fuzzygo.model

data class FuzzyProject(
    val id: String,
    val name: String,
    val description: String,
    val themeColor: String = "#6200EE" // Default Purple
)
```

Step 2: Update Repository
Add a function to fetch all projects. Open data/FuzzyRepository.kt and add:

```
// In FuzzyRepository.kt

// Get a stream of all projects
fun getProjectsFlow(): Flow<List<FuzzyProject>> {
    return firestore.collection("projects")
        .orderBy("created_at", Query.Direction.DESCENDING)
        .snapshots()
        .map { snapshot ->
            snapshot.documents.map { doc ->
                FuzzyProject(
                    id = doc.id,
                    name = doc.getString("name") ?: "Untitled Project",
                    description = doc.getString("description") ?: "",
                    themeColor = doc.getString("theme_color") ?: "#6200EE"
                )
            }
        }
}

// Create a new project
fun createProject(name: String, description: String) {
    val projectData = hashMapOf(
        "name" to name,
        "description" to description,
        "created_at" to com.google.firebase.Timestamp.now(),
        "theme_color" to "#6200EE",
        "owner_id" to "user_123" // Hardcoded for now until Auth is added
    )
    
    // Create the project document
    val newDoc = firestore.collection("projects").document()
    newDoc.set(projectData)
    
    // CRITICAL: Initialize default lanes so the board isn't empty!
    val defaultLanes = listOf("To Do", "In Progress", "Done")
    defaultLanes.forEachIndexed { index, title ->
        newDoc.collection("lanes").add(hashMapOf(
            "title" to title,
            "order_index" to index
        ))
    }
}
```

Step 3: The Project List UI
Create ui/screens/ProjectListScreen.kt.
This will be a simple list of clickable cards. We'll also add a Floating Action Button (FAB) to create new projects.

```
package com.ejectedmedia.fuzzygo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ejectedmedia.fuzzygo.model.FuzzyProject
import com.ejectedmedia.fuzzygo.ui.viewmodel.ProjectListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen(
    onProjectClick: (String) -> Unit,
    viewModel: ProjectListViewModel = viewModel()
) {
    val projects by viewModel.projects.collectAsState(initial = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Projects", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Color(0xFF6200EE),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Create Project")
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF5F5F5)),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(projects) { project ->
                ProjectCard(project, onClick = { onProjectClick(project.id) })
            }
        }

        if (showCreateDialog) {
            CreateProjectDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { name, desc ->
                    viewModel.createProject(name, desc)
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
fun ProjectCard(project: FuzzyProject, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            // Color dot
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color(android.graphics.Color.parseColor(project.themeColor)), CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(project.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (project.description.isNotEmpty()) {
                    Text(project.description, color = Color.Gray)
                }
            }
        }
    }
}

// Simple Dialog (Similar to AddCardDialog)
@Composable
fun CreateProjectDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Project") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
            }
        },
        confirmButton = {
            Button(onClick = { if(name.isNotBlank()) onConfirm(name, desc) }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
```

Note: You'll need a tiny ProjectListViewModel for this screen too, just like the Board one.

Step 4: The Navigation (MainActivity)
This is where we connect the Picker to the Board. We need to modify BoardViewModel to accept a projectId dynamically, rather than hardcoding "my-first-project".
1. Update BoardViewModel
Add a method initialize(projectId: String) that triggers the repository load, instead of doing it in init {}.
2. Set up Navigation in MainActivity.kt

```
package com.ejectedmedia.fuzzygo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ejectedmedia.fuzzygo.ui.screens.ProjectBoardScreen
import com.ejectedmedia.fuzzygo.ui.screens.ProjectListScreen
import com.ejectedmedia.fuzzygo.ui.viewmodel.BoardViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "project_list") {
                
                // SCREEN 1: Project Picker
                composable("project_list") {
                    ProjectListScreen(
                        onProjectClick = { projectId ->
                            // Navigate to board with the ID
                            navController.navigate("board/$projectId")
                        }
                    )
                }

                // SCREEN 2: The Board
                composable(
                    route = "board/{projectId}",
                    arguments = listOf(navArgument("projectId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
                    
                    // specific View Model instance for this screen
                    val boardViewModel: BoardViewModel = viewModel()
                    
                    // Trigger the load when we enter this screen
                    LaunchedEffect(projectId) {
                        boardViewModel.loadProject(projectId)
                    }

                    ProjectBoardScreen(viewModel = boardViewModel)
                }
            }
        }
    }
}
```

The Full User Journey
 * Launch App: You see the "My Projects" list. It might be empty.
 * Create Project: Click +. Enter "Fuzzy-Go V1". Click Create.
 * Auto-Setup: The repository creates the project and automatically adds "To Do", "In Progress", "Done" lanes to Firestore.
 * View Project: The new project appears in the list. Click it.
 * Navigate: The app transitions to the Board Screen, loading the specific lanes for "Fuzzy-Go V1".

You now have a complete, multi-project, real-time Kanban app on Android! 🎉
Where would you like to take this next? We could look at:
 * User Authentication (Using Google Sign-In so you aren't "user_123").
 * The Go Backend (Actually building the API we planned earlier).
 * Deployment (Generating an APK to install on your phone).
 * 
