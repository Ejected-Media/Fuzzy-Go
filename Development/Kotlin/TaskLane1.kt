// In TaskLane.kt

@Composable
fun TaskLane(
    title: String,
    laneId: String, // Add this parameter!
    cards: List<FuzzyCard>,
    onCardDrop: (String, String) -> Unit, // Add this callback!
    // ... existing params
) {
    DropTargetLaneWrapper(
        laneId = laneId,
        onCardDropped = onCardDrop
    ) { isHovering ->
        
        // Visual feedback: Darker grey if hovering
        val bgColor = if (isHovering) Color(0xFFE0E0E0) else Color(0xFFF3F4F6)

        Surface(
            modifier = modifier
                .width(300.dp)
                .fillMaxHeight()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = bgColor // Use the dynamic color
        ) {
            Column(...) {
                // ... Existing Header and LazyColumn ...
                // IMPORTANT: Ensure LazyColumn handles the drop target correctly 
                // by taking up the full space so you can drop anywhere in the lane.
            }
        }
    }
}
