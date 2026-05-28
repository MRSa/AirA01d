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
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel


@Composable
fun LiveviewMagnifyButton(
    viewModel: LiveviewViewModel,
    controlModel: CameraStatusViewModel,
    modifier: Modifier = Modifier
)
{
    val haptic = LocalHapticFeedback.current

    // ----- ステータスを監視する
    val isLvActivated = viewModel.isLvActivated.observeAsState()
    val lvMagnifySize = controlModel.liveViewMagnifySize.observeAsState()
    val cameraProtocol = controlModel.cameraProtocol.observeAsState()

    // ----- ステータスに合わせてアイコンをと色を決める
    val iconId = when (lvMagnifySize.value)
    {
        5 -> R.drawable.times_5
        7 -> R.drawable.times_7
        10 -> R.drawable.times_10
        14 -> R.drawable.times_14
        else -> R.drawable.outline_loupe_24
    }
    val iconColor = when (lvMagnifySize.value)
    {
        5 -> MaterialTheme.colorScheme.tertiary
        7 -> MaterialTheme.colorScheme.tertiary
        10 -> MaterialTheme.colorScheme.tertiary
        14 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    // ----- ボタンの表示
    IconButton(
        onClick = {
            if ((isLvActivated.value == true)&&(cameraProtocol.value == ICameraConnectionStatus.CameraProtocol.OPC))
            {
                // ----- ライブビュー表示 & OPCプロトコルの時、ライブビューの拡大を実行
                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                controlModel.changeLiveviewScale()
            }
        },
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = "liveview zoom",
            tint = iconColor
        )
    }
}
