package cz.tomasjanicek.bp.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MyGreen,
    onPrimary = MyBlack, // Text na zeleném tlačítku
    secondary = MyPink,
    tertiary = Pink80,

    background = MyBlack,
    onBackground = MyWhite,

    // Důležité pro Karty (Card) v nastavení:
    surface = Color(0xFF1E1E1E), // O něco světlejší černá pro karty
    onSurface = MyWhite,
    surfaceVariant = Color(0xFF2D2D2D), // Ještě světlejší pro prvky v kartách
    onSurfaceVariant = Color(0xFFCCCCCC),

    error = MyRed,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

// --- SVĚTLÉ SCHÉMA ---
private val LightColorScheme = lightColorScheme(
    primary = MyGreen,
    onPrimary = MyWhite, // Text na zeleném tlačítku
    secondary = MyPink,
    tertiary = Pink40,

    background = MyWhite,
    onBackground = MyBlack,

    // Důležité pro Karty (Card) v nastavení:
    surface = Color(0xFFF5F5F5), // Jemně šedá, aby se karty odlišily od bílého pozadí
    onSurface = MyBlack,
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF49454F),

    error = MyRed,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun BpTheme(
    // Tento parametr se plní z MainActivity na základě nastavení (Flow)
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Rozhodneme natvrdo pouze mezi našimi dvěma schématy.
    // Ignorujeme dynamické barvy Androidu 12+, aby aplikace držela tvůj brand.
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    // Volitelné: Nastavení barvy systémové lišty (pokud to neděláš v MainActivity přes Accompanist)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Nastavíme status bar na barvu pozadí (nebo transparent)
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()

            // Ovládání tmavých/světlých ikon v liště
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
enum class AppTheme {
    SYSTEM, // Automaticky podle telefonu
    LIGHT,  // Vynucený světlý
    DARK    // Vynucený tmavý
}