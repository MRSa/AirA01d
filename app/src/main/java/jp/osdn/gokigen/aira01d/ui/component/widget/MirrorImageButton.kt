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
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel

@Composable
fun MirrorImageButton(viewModel: LiveviewViewModel, modifier: Modifier = Modifier)
{
    val haptic = LocalHapticFeedback.current

    // ----- ステータスを監視する
    val isMirrorMode = viewModel.isMirrorMode.observeAsState()
    val isLvActivated = viewModel.isLvActivated.observeAsState()

    // ----- ステータスに合わせてアイコンをと色を決める
    val iconId = if (isMirrorMode.value == true) { R.drawable.outline_flip_camera_ios_24 } else { R.drawable.outline_photo_camera_24 }
    val iconColor = if (isMirrorMode.value == true) { MaterialTheme.colorScheme.onSurfaceVariant } else { MaterialTheme.colorScheme.primary }

    // ----- ボタンの表示
    IconButton(
        onClick = {
            if (isMirrorMode.value == false)
            {
                // ----- 鏡像表示モードに切り替える
                viewModel.setMirrorMode(true)
                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
            }
            else
            {
                // ----- 通常表示モードに切り替える
                viewModel.setMirrorMode(false)
                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
            }
        },
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = "mirror image",
            tint = iconColor
        )
    }
}
