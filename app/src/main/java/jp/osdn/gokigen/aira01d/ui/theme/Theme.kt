package jp.osdn.gokigen.aira01d.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AzureBlue80,
    secondary = AzureBlueGrey80,
    tertiary = SkyBlue80
)

private val LightColorScheme = lightColorScheme(
    primary = AzureBlue40,
    secondary = AzureBlueGrey40,
    tertiary = SkyBlue40
)

@Composable
fun AirA01dTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // ColorScheme の決定
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // カスタム警告色の決定
    val customColors = if (darkTheme) {
        CustomColors(
            warning = WarningDark,
            onWarning = OnWarningDark,
            warningContainer = WarningContainerDark,
            onWarningContainer = OnWarningContainerDark
        )
    } else {
        CustomColors(
            warning = WarningLight,
            onWarning = OnWarningLight,
            warningContainer = WarningContainerLight,
            onWarningContainer = OnWarningContainerLight
        )
    }

    // CompositionLocalProvider でカスタムカラーを注入
    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// 使いやすくするためのユーティリティ
object AirA01dTheme {
    val customColors: CustomColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCustomColors.current
}
