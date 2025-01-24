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
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = YoYoColors.Purple80,
        secondary = YoYoColors.PurpleGrey80,
        tertiary = YoYoColors.Pink80,
        surface = Color(0xFF1C1B1F),
        background = Color(0xFF000000),
        onSurface = Color.White.copy(alpha = 0.87f)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TvTypography().copy(
            titleLarge = TvTypography().titleLarge.copy(fontSize = 32.sp),
            titleMedium = TvTypography().titleMedium.copy(fontSize = 24.sp),
            bodyLarge = TvTypography().bodyLarge.copy(fontSize = 20.sp),
            bodyMedium = TvTypography().bodyMedium.copy(fontSize = 18.sp)
        ),
        content = content
    )
}