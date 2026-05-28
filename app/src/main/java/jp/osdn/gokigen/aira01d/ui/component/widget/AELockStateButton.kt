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
fun AELockStateButton(
    viewModel: LiveviewViewModel,
    controlModel: CameraStatusViewModel,
    modifier: Modifier = Modifier
)
{
    val haptic = LocalHapticFeedback.current

    // ----- ステータスを監視する
    val isLvActivated = viewModel.isLvActivated.observeAsState()
    val aeLockState = controlModel.aeLockState.observeAsState()
    val cameraProtocol = controlModel.cameraProtocol.observeAsState()

    // ----- ステータスに合わせてアイコンをと色を決める
    val iconId =
        when (aeLockState.value)
        {
            "UNLOCK" -> R.drawable.ae_unlock
            "LOCK" -> R.drawable.ae_lock
            else -> R.drawable.ae_unlock
        }
    val iconColor = if (isLvActivated.value == true) {
        when (aeLockState.value)
        {
            "UNLOCK" -> MaterialTheme.colorScheme.primary
            "LOCK" ->MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    } else {
        MaterialTheme.colorScheme.primary
    }

    // ----- ボタンの表示
    IconButton(
        onClick = {
            if (cameraProtocol.value == ICameraConnectionStatus.CameraProtocol.OPC)
            {
                // ----- AE Lock / Unlock コマンド実行 (OPCモードの時)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val command = (aeLockState.value == "UNLOCK")
                controlModel.changeAELockState(command)
            }
        },
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = "AE lock",
            tint = iconColor,
            modifier = Modifier.size(42.dp)
        )
    }
}
