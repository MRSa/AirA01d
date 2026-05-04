package jp.osdn.gokigen.aira01d.ui.component.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
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
fun LiveviewScreenPortrait(navController: NavHostController, liveviewModel: LiveviewViewModel, cameraStatusViewModel: CameraStatusViewModel)
{
    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        LiveviewWidget(
            liveviewModel,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // これで「余ったスペースを全部使う」指示になる
        )

        // 操作ボタン：必要な分だけ高さを取る
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp), // 少し余裕を持たせる
            horizontalArrangement = Arrangement.SpaceEvenly // ボタンを均等配置
        ) {
            ConnectButton(cameraStatusViewModel)
            WifiConfigButton(liveviewModel)
            MirrorImage(liveviewModel)
            PowerOffButton(cameraStatusViewModel)
        }
    }
}
