// In FuzzyRepository.kt

private val auth = FirebaseAuth.getInstance()

fun createProject(name: String, description: String) {
    val userId = auth.currentUser?.uid ?: return // Guard clause

    val projectData = hashMapOf(
        // ... existing fields ...
        "owner_id" to userId // NOW IT IS REAL!
    )
    // ...
}
