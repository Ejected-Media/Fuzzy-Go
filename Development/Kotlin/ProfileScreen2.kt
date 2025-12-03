// Add this parameter to the composable
fun ProfileScreen(
    currentTheme: AppThemeOption,
    onThemeChange: (AppThemeOption) -> Unit,
    // ... existing params
) {
    // ... Inside the Column ...
    
    Text("Appearance", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(8.dp))
    
    // Theme Toggle Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Fairy Floss Theme 🍬")
        Switch(
            checked = currentTheme == AppThemeOption.FAIRY_FLOSS,
            onCheckedChange = { isChecked ->
                val newTheme = if (isChecked) AppThemeOption.FAIRY_FLOSS else AppThemeOption.SYSTEM
                onThemeChange(newTheme)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = FairyFloss.Pink,
                checkedTrackColor = FairyFloss.Purple
            )
        )
    }
}
