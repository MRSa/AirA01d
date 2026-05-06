package jp.osdn.gokigen.aira01d.ui.component.widget.connect

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel

@Composable
fun WifiConfigButton(viewModel: LiveviewViewModel, modifier: Modifier = Modifier)
{
    val haptic = LocalHapticFeedback.current

    // ----- ステータスを監視する
    val receiveCount = viewModel.receiveCount.observeAsState()
    val isLvActivated = viewModel.isLvActivated.observeAsState()

    val checkStatus = receiveCount.value?.rem(30) ?: 0
    val context = LocalContext.current

    // ----- アイコン...ライブビューが更新されているときには
    val iconId = if (isLvActivated.value != true) {
        R.drawable.outline_signal_wifi_statusbar_not_connected_24
    } else  if (checkStatus > 24) {
        R.drawable.outline_signal_wifi_4_bar_24
    } else if (checkStatus > 18) {
        R.drawable.outline_network_wifi_3_bar_24
    } else if (checkStatus > 12) {
        R.drawable.outline_network_wifi_2_bar_24
    } else if (checkStatus > 6) {
        R.drawable.outline_network_wifi_1_bar_24
    } else {
        R.drawable.outline_signal_wifi_0_bar_24
    }

    // 処理を関数として定義
    val onOpenWifiSettings = {
        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        context.startActivity(intent)
    }
    // ----- ボタンの表示
    IconButton(
        onClick = onOpenWifiSettings,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = "connection status",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
