// In BoardViewModel.kt

fun onCardDropped(cardId: String, newLaneId: String) {
    // 1. Optimistic Update (Optional but recommended): 
    // We could update the _boardState immediately so the user sees it move 
    // before the server responds. For now, we rely on the Firestore real-time listener 
    // we built earlier, which is usually fast enough ( < 100ms).

    viewModelScope.launch {
        // Hardcoded project ID for now
        repository.moveCardToLane("my-first-project", cardId, newLaneId)
    }
}
