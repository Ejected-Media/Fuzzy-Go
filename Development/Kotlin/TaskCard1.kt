// In TaskCard.kt

@Composable
fun TaskCard(
    card: FuzzyCard,
    modifier: Modifier = Modifier
) {
    DraggableCardWrapper(cardId = card.id) {
        // ... The existing Card UI implementation goes here ...
        Card(modifier = modifier...) { ... }
    }
}
