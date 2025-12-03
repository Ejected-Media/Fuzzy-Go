// In MainActivity.kt

NavHost(...) {
    
    // 1. Update Project List Route to handle the click
    composable("project_list") {
        ProjectListScreen(
            onProjectClick = { projectId -> navController.navigate("board/$projectId") },
            onProfileClick = { navController.navigate("profile") } // Go to profile
        )
    }

    // 2. Add Profile Route
    composable("profile") {
        ProfileScreen(
            onBackClick = { navController.popBackStack() },
            onSignOutSuccess = {
                // Clear the backstack so they can't press "Back" to get into the app again
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
    
    // ... other routes (login, board) ...
}
