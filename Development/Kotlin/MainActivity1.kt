// ... inside setContent ...
val navController = rememberNavController()
val auth = FirebaseAuth.getInstance()
val startDestination = if (auth.currentUser != null) "project_list" else "login"

NavHost(navController = navController, startDestination = startDestination) {

    // 1. Login Screen
    composable("login") {
        LoginScreen(
            onLoginSuccess = {
                // Pop login off the stack so back button doesn't return to login
                navController.navigate("project_list") {
                    popUpTo("login") { inclusive = true }
                }
            }
        )
    }

    // 2. Project List
    composable("project_list") {
        ProjectListScreen(
            onProjectClick = { projectId -> navController.navigate("board/$projectId") }
        )
    }

    // ... Board Screen ...
}
