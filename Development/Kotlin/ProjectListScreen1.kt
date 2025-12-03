// In ProjectListScreen.kt

@Composable
fun ProjectListScreen(
    onProjectClick: (String) -> Unit,
    onProfileClick: () -> Unit, // <--- Add this callback
    viewModel: ProjectListViewModel = viewModel()
) {
    // ... existing state ...

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Projects", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = { // <--- Add this section
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, "Profile")
                    }
                }
            )
        },
        // ... rest of the scaffold
    ) { ... }
}
