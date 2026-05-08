package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel

@Composable
fun RawModeButton(
    viewModel: LiveviewViewModel,
    controlModel: CameraStatusViewModel,
    modifier: Modifier = Modifier
)
{
    val haptic = LocalHapticFeedback.current

    // ----- ステータスを監視する
    val isLvActivated = viewModel.isLvActivated.observeAsState()
    val rawMode = controlModel.rawMode.observeAsState()

    // ----- ステータスに合わせてアイコンをと色を決める
    val iconId = when (rawMode.value) {
        "ON" -> R.drawable.outline_raw_on_24
        "OFF" -> R.drawable.outline_raw_off_24
        else -> R.drawable.outline_raw_off_24
    }

    val iconColor = if (isLvActivated.value == true) {
        when (rawMode.value)
        {
            "ON" ->MaterialTheme.colorScheme.tertiary
            "OFF" -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    } else {
        MaterialTheme.colorScheme.primary
    }

    // ----- ボタンの表示
    IconButton(
        onClick = {
            // ----- コマンド実行
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val command = if (rawMode.value == "ON") { "OFF" } else { "ON" }
            controlModel.setProperty(ICameraStatus.CameraProperty.RawMode, command)
        },
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = "RAW mode",
            tint = iconColor,
            modifier = Modifier.size(42.dp)
        )
    }
}
