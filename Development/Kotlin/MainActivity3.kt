// In MainActivity.kt
var currentTheme by remember { mutableStateOf(AppThemeOption.SYSTEM) }

FuzzyGoTheme(themeOption = currentTheme) {
    NavHost(...) {
        composable("profile") {
            ProfileScreen(
                currentTheme = currentTheme,
                onThemeChange = { newTheme -> currentTheme = newTheme }, // Update state
                // ...
            )
        }
    }
}
