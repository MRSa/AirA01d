package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel


@Composable
fun ExposureCompensationButton(viewModel: CameraStatusViewModel, modifier: Modifier = Modifier)
{
    val haptic = LocalHapticFeedback.current

    // ----- ステータスを監視する
    val takeMode = viewModel.takeMode.observeAsState()
    val xv = viewModel.xv.observeAsState()

    val isOk = when (takeMode.value) {
        "P" -> { true }
        "A" -> { true }
        "S" -> { true }
        else -> { false }
    }
    // ----- ボタンの表示
    TextButton(
        onClick = {
            if (isOk)
            {
                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
            }
        },
        modifier = modifier
            .height(48.dp)
            .widthIn(min = 48.dp, max = 106.dp)
    ) {
        Text(
            text = xv.value ?: "",
            style = TextStyle(
                textDecoration = if (isOk) {TextDecoration.Underline } else { TextDecoration.None },
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}
