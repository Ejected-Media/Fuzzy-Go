// In BoardViewModel.kt

// Add this state to track the active dialog
private val _activeLaneIdForDialog = MutableStateFlow<String?>(null)
val activeLaneIdForDialog: StateFlow<String?> = _activeLaneIdForDialog

fun openAddCardDialog(laneId: String) {
    _activeLaneIdForDialog.value = laneId
}

fun closeAddCardDialog() {
    _activeLaneIdForDialog.value = null
}

fun createCard(title: String, description: String, priority: FuzzyPriority) {
    val laneId = _activeLaneIdForDialog.value ?: return
    
    viewModelScope.launch {
        repository.createCard(
            projectId = "my-first-project",
            laneId = laneId,
            title = title,
            description = description,
            priority = priority
        )
        // Close dialog after creating
        closeAddCardDialog()
    }
}
