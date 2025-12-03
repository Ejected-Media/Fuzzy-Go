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
