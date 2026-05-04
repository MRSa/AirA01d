package jp.osdn.gokigen.aira01d.ui.component.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import jp.osdn.gokigen.aira01d.ui.component.widget.ConnectButton
import jp.osdn.gokigen.aira01d.ui.component.widget.LiveviewWidget
import jp.osdn.gokigen.aira01d.ui.component.widget.MirrorImage
import jp.osdn.gokigen.aira01d.ui.component.widget.PowerOffButton
import jp.osdn.gokigen.aira01d.ui.component.widget.WifiConfigButton
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel

@Composable
fun LiveviewScreenLandscape(navController: NavHostController, liveviewModel: LiveviewViewModel, cameraStatusViewModel: CameraStatusViewModel)
{
    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 上部：ステータス/設定ボタン群
        Row(modifier = Modifier.fillMaxWidth().height(50.dp)) {
            PowerOffButton(cameraStatusViewModel)
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // 左側：ドライブモード・電池など
            Column(modifier = Modifier.width(110.dp).fillMaxHeight()) {
                ConnectButton(cameraStatusViewModel)
            }

            // 中央：メインライブビュー
            Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                LiveviewWidget(liveviewModel, Modifier.fillMaxWidth())
            }

            // 右側：シャッター・アイコン群
            Column(modifier = Modifier.width(110.dp).fillMaxHeight()) {
                ConnectButton(cameraStatusViewModel)
                WifiConfigButton(liveviewModel)
                MirrorImage(liveviewModel)
            }
        }
    }
}
