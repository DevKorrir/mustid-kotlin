package dev.korryr.digitalid.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00D666),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF00D666).copy(alpha = 0.2f),
    onPrimaryContainer = Color.Black,
    secondary = Color(0xFF007BFF),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF007BFF).copy(alpha = 0.2f),
    onSecondaryContainer = Color.Black,
    tertiary = Color(0xFFFD7E14),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFFD7E14).copy(alpha = 0.2f),
    onTertiaryContainer = Color.Black,
    error = Color(0xFFFFC107),
    onError = Color.Black,
    errorContainer = Color(0xFFFFC107).copy(alpha = 0.2f),
    onErrorContainer = Color.Black,
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF343a40),
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF343a40),
    surfaceVariant = Color(0xFF868E96),
    onSurfaceVariant = Color.White,
    outline = Color(0xFF868E96),
    inverseOnSurface = Color(0xFFF8F9FA),
    inverseSurface = Color(0xFF343a40),
    inversePrimary = Color(0xFF28a745),
    surfaceTint = Color(0xFF20c997),
    outlineVariant = Color(0xFF868E96),
    scrim = Color(0x80000000)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00D666),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF00D666).copy(alpha = 0.2f),
    onPrimaryContainer = Color.Black,
    secondary = Color(0xFF007BFF),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF007BFF).copy(alpha = 0.2f),
    onSecondaryContainer = Color.Black,
    tertiary = Color(0xFFFD7E14),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFFD7E14).copy(alpha = 0.2f),
    onTertiaryContainer = Color.Black,
    error = Color(0xFFFFC107),
    onError = Color.Black,
    errorContainer = Color(0xFFFFC107).copy(alpha = 0.2f),
    onErrorContainer = Color.Black,
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF343a40),
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF343a40),
    surfaceVariant = Color(0xFF868E96),
    onSurfaceVariant = Color.White,
    outline = Color(0xFF868E96),
    inverseOnSurface = Color(0xFFF8F9FA),
    inverseSurface = Color(0xFF343a40),
    inversePrimary = Color(0xFF28a745),
    surfaceTint = Color(0xFF20c997),
    outlineVariant = Color(0xFF868E96),
    scrim = Color(0x80000000)
)

@Composable
fun DigitalIDTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}