package com.alternadv.vedhelper.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    primaryContainer = BrandSoft,
    onPrimaryContainer = Ink,
    secondary = Muted,
    onSecondary = Color.White,
    secondaryContainer = Panel,
    onSecondaryContainer = Ink,
    tertiary = Action,
    onTertiary = Ink,
    tertiaryContainer = ActionSoft,
    onTertiaryContainer = Ink,
    background = Canvas,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = Panel,
    onSurfaceVariant = Muted,
    surfaceDim = Color(0xFFE2E8ED),
    surfaceBright = Surface,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF9FBFC),
    surfaceContainer = Color(0xFFF1F5F8),
    surfaceContainerHigh = Color(0xFFEAF0F4),
    surfaceContainerHighest = Color(0xFFE3EAEF),
    outline = Line,
    outlineVariant = Color(0xFFE7EDF2),
    error = Alert,
    onError = Color.White,
    errorContainer = Color(0xFFFDE6DF),
    onErrorContainer = Color(0xFF7A1E08),
    inverseSurface = Ink,
    inverseOnSurface = Color.White,
    inversePrimary = BrandSoft,
    surfaceTint = Brand
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9DC4D9),
    onPrimary = Color(0xFF0C2431),
    primaryContainer = Color(0xFF24485C),
    onPrimaryContainer = OnDark,
    secondary = Color(0xFFB8C4CE),
    onSecondary = Color(0xFF101B22),
    secondaryContainer = DarkPanel,
    onSecondaryContainer = OnDark,
    tertiary = Action,
    onTertiary = Ink,
    tertiaryContainer = Color(0xFF3E4A1D),
    onTertiaryContainer = Color(0xFFE9F8C3),
    background = DarkCanvas,
    onBackground = OnDark,
    surface = DarkSurface,
    onSurface = OnDark,
    surfaceVariant = DarkPanel,
    onSurfaceVariant = Color(0xFFB8C4CE),
    surfaceDim = Color(0xFF0A1117),
    surfaceBright = Color(0xFF26343E),
    surfaceContainerLowest = Color(0xFF0A1117),
    surfaceContainerLow = Color(0xFF121C24),
    surfaceContainer = Color(0xFF17222B),
    surfaceContainerHigh = Color(0xFF1E2B35),
    surfaceContainerHighest = Color(0xFF26343E),
    outline = DarkLine,
    outlineVariant = Color(0xFF26343E),
    error = Color(0xFFFFB4A2),
    onError = Color(0xFF5F1600),
    errorContainer = Color(0xFF7A1E08),
    onErrorContainer = Color(0xFFFFDAD2),
    inverseSurface = OnDark,
    inverseOnSurface = Ink,
    inversePrimary = Brand
)

@Composable
fun VedHelperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Брендовая схема по умолчанию; dynamic color менял палитру на каждом телефоне.
    dynamicColor: Boolean = false,
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