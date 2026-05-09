package jp.osdn.gokigen.aira01d.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// --- Light Theme Colors
val AzureBlue40 = Color(0xFF0061A4)
val AzureBlueGrey40 = Color(0xFF535F70)
val SkyBlue40 = Color(0xFF28638A)

// 警告色（Warning）の定義
val WarningLight = Color(0xFF855400)
val OnWarningLight = Color(0xFFFFFFFF)
val WarningContainerLight = Color(0xFFFFDDB3)
val OnWarningContainerLight = Color(0xFF2A1700)

// --- Dark Theme Colors
val AzureBlue80 = Color(0xFFD1E4FF)
val AzureBlueGrey80 = Color(0xFFBBC7DB)
val SkyBlue80 = Color(0xFFA5C8F6)

// 警告色（Warning）の定義
val WarningDark = Color(0xFFFFB951)
val OnWarningDark = Color(0xFF452B00)
val WarningContainerDark = Color(0xFF633F00)
val OnWarningContainerDark = Color(0xFFFFDDB3)

// カスタムカラーを保持するデータクラス
@Immutable
data class CustomColors(
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color
)

// CompositionLocal の定義
val LocalCustomColors = staticCompositionLocalOf {
    CustomColors(
        warning = Color.Unspecified,
        onWarning = Color.Unspecified,
        warningContainer = Color.Unspecified,
        onWarningContainer = Color.Unspecified
    )
}
