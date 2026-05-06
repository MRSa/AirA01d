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
fun ShowGridButton(viewModel: LiveviewViewModel, modifier: Modifier = Modifier)
{
    val haptic = LocalHapticFeedback.current

    // ----- ステータスを監視する
    val isGridOn = viewModel.isGridOn.observeAsState()
    val isLvActivated = viewModel.isLvActivated.observeAsState()

    // ----- ステータスに合わせてアイコンをと色を決める
    val iconId = if (isGridOn.value == true) { R.drawable.outline_grid_off_24 } else { R.drawable.outline_grid_on_24 }
    val iconColor = if (isLvActivated.value == true) { MaterialTheme.colorScheme.primary } else { MaterialTheme.colorScheme.onSurfaceVariant }

    // ----- ボタンの表示
    IconButton(
        onClick = {
            if (isGridOn.value == false)
            {
                // ----- グリッド表示モードに切り替える
                viewModel.setGridOn(true)
                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
            }
            else
            {
                // ----- グリッド非表示モードに切り替える
                viewModel.setGridOn(false)
                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
            }
        },
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = "grid on/off",
            tint = iconColor
        )
    }
}
