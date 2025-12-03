// In ProjectBoardScreen.kt

items(lanes) { lane ->
    TaskLane(
        title = lane.title,
        laneId = lane.id, // Pass ID
        cards = lane.cards,
        onAddCardClick = { viewModel.createTestCard(lane.id) },
        onCardDrop = { cardId, newLaneId -> 
            viewModel.onCardDropped(cardId, newLaneId) // Connect to ViewModel
        }
    )
}
