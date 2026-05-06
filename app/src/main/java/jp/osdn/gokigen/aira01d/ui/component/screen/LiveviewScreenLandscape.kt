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
import jp.osdn.gokigen.aira01d.ui.component.widget.AFLockUnlockButton
import jp.osdn.gokigen.aira01d.ui.component.widget.ApertureButton
import jp.osdn.gokigen.aira01d.ui.component.widget.ExposureCompensationButton
import jp.osdn.gokigen.aira01d.ui.component.widget.ExposureWarningText
import jp.osdn.gokigen.aira01d.ui.component.widget.FocusModeButton
import jp.osdn.gokigen.aira01d.ui.component.widget.IsoSensitivityButton
import jp.osdn.gokigen.aira01d.ui.component.widget.connect.ConnectButton
import jp.osdn.gokigen.aira01d.ui.component.widget.drawer.LiveviewWidget
import jp.osdn.gokigen.aira01d.ui.component.widget.MirrorImageButton
import jp.osdn.gokigen.aira01d.ui.component.widget.PictureEffectButton
import jp.osdn.gokigen.aira01d.ui.component.widget.connect.PowerOffButton
import jp.osdn.gokigen.aira01d.ui.component.widget.ShowGridButton
import jp.osdn.gokigen.aira01d.ui.component.widget.ShutterButton
import jp.osdn.gokigen.aira01d.ui.component.widget.ShutterSpeedButton
import jp.osdn.gokigen.aira01d.ui.component.widget.TakeModeButton
import jp.osdn.gokigen.aira01d.ui.component.widget.WhiteBalanceButton
import jp.osdn.gokigen.aira01d.ui.component.widget.connect.WifiConfigButton
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel

@Composable
fun LiveviewScreenLandscape(navController: NavHostController, liveviewModel: LiveviewViewModel, cameraStatusViewModel: CameraStatusViewModel)
{
    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 上部：ステータス/設定ボタン群
        Row(modifier = Modifier.fillMaxWidth().height(50.dp)) {
            TakeModeButton(cameraStatusViewModel)
            ShutterSpeedButton(cameraStatusViewModel)
            ApertureButton(cameraStatusViewModel)
            IsoSensitivityButton(cameraStatusViewModel)
            PictureEffectButton(cameraStatusViewModel)
            WhiteBalanceButton(cameraStatusViewModel)
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // 左側：ドライブモード・電池など
            Column(modifier = Modifier.width(110.dp).fillMaxHeight()) {
                PowerOffButton(cameraStatusViewModel)
                ConnectButton(cameraStatusViewModel)
                WifiConfigButton(liveviewModel)
            }

            // 中央：メインライブビュー
            Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                LiveviewWidget(liveviewModel, Modifier.fillMaxWidth())
            }

            // 右側：シャッター・アイコン群
            Column(modifier = Modifier.width(110.dp).fillMaxHeight()) {
                AFLockUnlockButton(liveviewModel)
                MirrorImageButton(liveviewModel)
                ShowGridButton(liveviewModel)
                ShutterButton(liveviewModel, cameraStatusViewModel)
                FocusModeButton(cameraStatusViewModel)
                ExposureCompensationButton(cameraStatusViewModel)
                ExposureWarningText(cameraStatusViewModel)
            }
        }
    }
}
