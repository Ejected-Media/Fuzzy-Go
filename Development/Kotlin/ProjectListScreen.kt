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
