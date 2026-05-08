package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel

@Composable
fun InformationArea1(viewModel: CameraStatusViewModel, modifier: Modifier = Modifier)
{
    // ----- ステータスを監視する
    val exposureWarning = viewModel.exposureWarning.observeAsState()
    val exposureWarningLevel = viewModel.exposureWarningLevel.observeAsState()

    // ----- 表示文字の装飾
    val exposureWarningLevelValue = exposureWarningLevel.value ?: 10
    val fontWeight = if (exposureWarningLevelValue > 2) { FontWeight.Normal } else { FontWeight.Bold }
    val textColor =
        if (exposureWarningLevelValue > 5 ) { MaterialTheme.colorScheme.primary }
        else if (exposureWarningLevelValue > 3 ) { MaterialTheme.colorScheme.tertiary }
        else { MaterialTheme.colorScheme.error }

    // ----- ボタンの表示
    TextButton(
        onClick = { },
        modifier = modifier
            .height(48.dp)
            .widthIn(min = 48.dp, max = 106.dp)
    ) {
        Text(
            text = exposureWarning.value ?: "",
            style = TextStyle(
                textDecoration = TextDecoration.None,
                fontWeight = fontWeight,
                color = textColor
            )
        )
    }
}
