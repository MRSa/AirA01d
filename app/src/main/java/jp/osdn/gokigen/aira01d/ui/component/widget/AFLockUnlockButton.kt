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
fun AFLockUnlockButton(viewModel: LiveviewViewModel, modifier: Modifier = Modifier)
{
    val haptic = LocalHapticFeedback.current

    // ----- ステータスを監視する
    val focusFrameStatus = viewModel.focusFrameStatus.observeAsState()

    // ----- ステータスに合わせてアイコンをと色を決める
    val (iconId, iconColor) = when (focusFrameStatus.value) {
        LiveviewViewModel.FocusFrameStatus.Focused ->
            // ----- フォーカスロック中
            R.drawable.outline_reset_focus_24 to MaterialTheme.colorScheme.onSurfaceVariant
        else ->
            // ----- フォーカスなし
            R.drawable.outline_center_focus_weak_24 to MaterialTheme.colorScheme.primary
    }

    // ----- ボタンの表示
    IconButton(
        onClick = {
            if (focusFrameStatus.value == LiveviewViewModel.FocusFrameStatus.Focused)
            {
                // ----- フォーカスアンロックを実行
                viewModel.unlockFocus()
            }
            else
            {
                // ----- 画面の中心にフォーカスを合わせる
                viewModel.onTouchPosition(0.5f, 0.5f)
            }
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        },
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = "focusing status",
            tint = iconColor
        )
    }
}
