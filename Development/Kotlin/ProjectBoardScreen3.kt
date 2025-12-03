// In ProjectBoardScreen.kt

@Composable
fun ProjectBoardScreen(
    viewModel: BoardViewModel = ...
) {
    val lanes by viewModel.boardState.collectAsStateWithLifecycle()
    // Observe the dialog state
    val activeLaneId by viewModel.activeLaneIdForDialog.collectAsStateWithLifecycle()

    Scaffold(...) { paddingValues ->
        Box(...) {
            // ... LazyRow implementation ...
             items(lanes) { lane ->
                TaskLane(
                    // ... other params ...
                    onAddCardClick = { 
                        viewModel.openAddCardDialog(lane.id) // OPEN DIALOG
                    }
                )
            }
            
            // --- CONDITIONAL DIALOG RENDERING ---
            if (activeLaneId != null) {
                // Find the lane title for the header (optional nicety)
                val laneTitle = lanes.find { it.id == activeLaneId }?.title ?: "Lane"
                
                AddCardDialog(
                    laneTitle = laneTitle,
                    onDismiss = { viewModel.closeAddCardDialog() },
                    onConfirm = { title, desc, priority ->
                        viewModel.createCard(title, desc, priority)
                    }
                )
            }
        }
    }
}
