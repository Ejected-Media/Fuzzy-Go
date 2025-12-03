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
