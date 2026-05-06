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
import jp.osdn.gokigen.a01lib.camera.interfaces.ICaptureControl
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel

@Composable
fun ShutterButton(viewModel: LiveviewViewModel, controlModel: CameraStatusViewModel, modifier: Modifier = Modifier)
{
    val haptic = LocalHapticFeedback.current

    // ----- ステータスを監視する
    val isLvActivated = viewModel.isLvActivated.observeAsState()

    // ----- ステータスに合わせてアイコンをと色を決める
    val iconId = R.drawable.baseline_camera_24
    val iconColor = if (isLvActivated.value == true) { MaterialTheme.colorScheme.primary } else { MaterialTheme.colorScheme.onSurfaceVariant }

    // ----- ボタンの表示
    IconButton(
        onClick = {
            controlModel.doCapture(ICaptureControl.CaptureAction.ON)  // そのうち、撮影状態によって、操作を変える予定。
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        },
        modifier = modifier.size(96.dp)  // シャッターボタンは通常のボタンの倍のサイズ
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = "camera shutter",
            tint = iconColor
        )
    }
}
