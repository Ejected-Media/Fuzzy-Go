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
