package `fun`.coda.app.yoyoplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import androidx.tv.material3.lightColorScheme
import androidx.tv.material3.Typography as TvTypography

private object YoYoColors {
    val Purple80 = Color(0xFFD0BCFF)
    val PurpleGrey80 = Color(0xFFCCC2DC)
    val Pink80 = Color(0xFFEFB8C8)
}

@Composable
fun YoYoPlayerTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = lightColorScheme(
        primary = Primary,
        secondary = Secondary,
        tertiary = Tertiary,
        background = Background,
        surface = Surface,
        surfaceVariant = SurfaceVariant,
        onPrimary = OnPrimary,
        onBackground = OnBackground,
        onSurface = OnSurface,
        primaryContainer = AccentLight,
        onPrimaryContainer = OnPrimary,
        secondaryContainer = AccentDark,
        onSecondaryContainer = Color.White,
        error = Color(0xFFFF6B6B)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}