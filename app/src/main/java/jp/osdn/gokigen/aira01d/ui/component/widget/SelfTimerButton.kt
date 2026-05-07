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
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel
import jp.osdn.gokigen.aira01d.ui.model.SelfTimerViewModel

@Composable
fun SelfTimerButton(viewModel: SelfTimerViewModel, lvViewModel: LiveviewViewModel, modifier: Modifier = Modifier)
{
    val haptic = LocalHapticFeedback.current

    val isTimerOn by viewModel.isTimerOn.collectAsStateWithLifecycle()
    val isTimerActivated by viewModel.isTimerActivated.collectAsStateWithLifecycle()

    val isLvActivated = lvViewModel.isLvActivated.observeAsState()

    // ----- ステータスに合わせてアイコンをと色を決める
    val iconId = when (isTimerOn) {
        SelfTimerViewModel.SelfTimerProperty.Timer3s -> R.drawable.outline_timer_3_alt_1_24
        SelfTimerViewModel.SelfTimerProperty.Timer5s -> R.drawable.outline_timer_5_24
        SelfTimerViewModel.SelfTimerProperty.Timer10s -> R.drawable.outline_timer_10_alt_1_24
        else -> R.drawable.outline_timer_off_24
    }
    val iconColor = if (isTimerOn == SelfTimerViewModel.SelfTimerProperty.TimerOff) { MaterialTheme.colorScheme.primary } else { MaterialTheme.colorScheme.onSurfaceVariant }

    // ----- ボタンの表示
    IconButton(
        onClick = {
            val changeStatus = when (isTimerOn) {
                SelfTimerViewModel.SelfTimerProperty.TimerOff -> SelfTimerViewModel.SelfTimerProperty.Timer3s
                SelfTimerViewModel.SelfTimerProperty.Timer3s -> SelfTimerViewModel.SelfTimerProperty.Timer5s
                SelfTimerViewModel.SelfTimerProperty.Timer5s -> SelfTimerViewModel.SelfTimerProperty.Timer10s
                SelfTimerViewModel.SelfTimerProperty.Timer10s -> SelfTimerViewModel.SelfTimerProperty.TimerOff
            }
            if (isTimerActivated)
            {
                // ----- セルフタイマー稼働中は、タイマーのカウントダウンを止める処理を行う
                viewModel.abortSelfTimer()
            }
            else
            {
                // ----- セルフタイマーが動いていないときは、タイマー状態を変える
                viewModel.changeStatus(changeStatus)
            }
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = "self timer",
            tint = iconColor
        )
    }
}
