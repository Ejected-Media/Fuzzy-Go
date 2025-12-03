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
