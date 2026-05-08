package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.osdn.gokigen.a01lib.camera.interfaces.ICaptureControl
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel
import jp.osdn.gokigen.aira01d.ui.model.SelfTimerViewModel

@Composable
fun ShutterButton(
    viewModel: LiveviewViewModel,
    controlModel: CameraStatusViewModel,
    selfTimerModel: SelfTimerViewModel,
    modifier: Modifier = Modifier
)
{
    val haptic = LocalHapticFeedback.current

    // ----- ステータスを監視する
    val isLvActivated = viewModel.isLvActivated.observeAsState()

    val isTimerOn by selfTimerModel.isTimerOn.collectAsStateWithLifecycle()
    val isTimerActivated by selfTimerModel.isTimerActivated.collectAsStateWithLifecycle()

    // ----- ステータスに合わせてアイコンをと色を決める
    val iconId = if (isTimerOn == SelfTimerViewModel.SelfTimerProperty.TimerOff) { R.drawable.baseline_camera_24  } else { R.drawable.outline_timer_24 }
    val iconColor = if (isTimerOn == SelfTimerViewModel.SelfTimerProperty.TimerOff) { MaterialTheme.colorScheme.primary } else { MaterialTheme.colorScheme.onSurfaceVariant }

    // ----- ボタンの表示
    IconButton(
        onClick = {
            if (isTimerOn == SelfTimerViewModel.SelfTimerProperty.TimerOff)
            {
                controlModel.doCapture(ICaptureControl.CaptureAction.ON)
            }
            else
            {
                if (isTimerActivated)
                {
                    // ----- セルフタイマー稼働中の時は、カウントダウンを止める
                    selfTimerModel.abortSelfTimer()
                }
                else
                {
                    // ----- セルフタイマーを稼働させる
                    selfTimerModel.startSelfTimer()
                }
            }
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        modifier = modifier.size(96.dp)  // シャッターボタンは通常のボタンの倍のサイズ
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = "camera shutter",
            tint = iconColor,
            modifier = Modifier.size(90.dp)
        )
    }
}
