This is a perfect addition. The Fairy Floss theme (created by Amy Wibowo / SailorHG) is legendary for its dreamy, pastel-purple aesthetic. It fits the "Fuzzy" vibe even better than our default purple.
We can definitely add this as a selectable "Theme Option" alongside the System Light/Dark modes.
Here is how to implement the Fairy Floss palette in Jetpack Compose.
Step 1: Define the Colors
First, we need to extract the specific hex codes from the Fairy Floss spec (soft purple background, bubblegum pinks, mints, and yellows) and define them in ui/theme/Color.kt.
ui/theme/Color.kt

```
package com.ejectedmedia.fuzzygo.ui.theme

import androidx.compose.ui.graphics.Color

// --- Standard Fuzzy Colors (Existing) ---
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// --- FAIRY FLOSS PALETTE 🍬 ---
// Based on https://sailorhg.github.io/fairyfloss/
object FairyFloss {
    val Background = Color(0xFF5a5475)   // The iconic soft plum background
    val Surface    = Color(0xFF3b3a32)   // Slightly darker for cards/inputs
    val Text       = Color(0xFFf8f8f0)   // Creamy white text
    
    val Pink       = Color(0xFFff857f)   // "Salmon" - Primary Action
    val Purple     = Color(0xFFc5a3ff)   // "Pale Purple" - Headers/Accents
    val Yellow     = Color(0xFFfff352)   // "Kryptonite" - Warnings/Highlights
    val Mint       = Color(0xFFc2ffdf)   // "Mint" - Success/Done
    val Cyan       = Color(0xFF4688cc)   // "Holo Blue" - Links/Info
}
```

Step 2: Create the Theme Logic
Now we need to update ui/theme/Theme.kt to support a third mode. Instead of just darkTheme: Boolean, we will pass a ThemeOption enum.
ui/theme/Theme.kt

```
package com.ejectedmedia.fuzzygo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. Define the Options
enum class AppThemeOption {
    SYSTEM,      // Follow Android Light/Dark
    FAIRY_FLOSS  // Force Fairy Floss 🍬
}

// 2. Define the Color Schemes
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// The Fairy Floss Scheme (Maps the palette to Material slots)
private val FairyFlossScheme = darkColorScheme(
    primary = FairyFloss.Pink,        // Buttons / FABs
    onPrimary = FairyFloss.Surface,   // Text on buttons
    primaryContainer = FairyFloss.Purple,
    
    background = FairyFloss.Background, // The main plum background
    onBackground = FairyFloss.Text,     // Standard text
    
    surface = FairyFloss.Surface,       // Cards / Bottom Sheets
    onSurface = FairyFloss.Text,
    
    error = FairyFloss.Yellow,          // Use Yellow for "alerts" (softer than red)
    onError = FairyFloss.Surface
)

@Composable
fun FuzzyGoTheme(
    themeOption: AppThemeOption = AppThemeOption.SYSTEM, // Default to System
    content: @Composable () -> Unit
) {
    // 3. Determine which colors to use
    val colorScheme = when (themeOption) {
        AppThemeOption.FAIRY_FLOSS -> FairyFlossScheme
        AppThemeOption.SYSTEM -> {
            val darkTheme = isSystemInDarkTheme()
            val context = LocalContext.current
            // Use Dynamic Color (Android 12+) if available and requested
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                 if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                 if (darkTheme) DarkColorScheme else LightColorScheme
            }
        }
    }

    // 4. Update Status Bar Color
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb() // Match background
            
            // If using Fairy Floss or Dark mode, make status bar icons light
            val isDark = themeOption == AppThemeOption.FAIRY_FLOSS || isSystemInDarkTheme()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

Step 3: Add the "Theme Picker" to Profile
Finally, let's add a switch in your Profile Screen so users can toggle this on!
Update ui/screens/ProfileScreen.kt

```
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
```

Final Step: Persisting the Choice
In MainActivity.kt, you will need a simple state to hold this preference (later you can save it to DataStore so it remembers the choice after closing the app).

```
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
```

Now, when you flip that switch, your entire app—backgrounds, buttons, and cards—will instantly transform into that beautiful SailorHG pastel aesthetic.
Video relevant to this topic: Build a Material Theme with the Material Theme Builder
This video is relevant because it explains how Material 3 themes and color schemes (like the dynamic colors and custom schemes we just built) work in Jetpack Compose, which will help you customize the Fairy Floss mapping further if you wish.
