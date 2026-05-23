package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel

@Composable
fun DummyButton(
    viewModel: LiveviewViewModel,
    controlModel: CameraStatusViewModel,
    modifier: Modifier = Modifier
)
{
    val haptic = LocalHapticFeedback.current

    // ----- ステータスを監視する
    val isLvActivated = viewModel.isLvActivated.observeAsState()
    val lvMagnifySize = controlModel.liveViewMagnifySize.observeAsState()

    // ----- ステータスに合わせてアイコンをと色を決める
    val iconId = R.drawable.outline_stop_24
    val iconColor = MaterialTheme.colorScheme.background

    // ----- ダミーボタンの表示
    IconButton(
        onClick = { },
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = "dummy",
            tint = iconColor
        )
    }
}
