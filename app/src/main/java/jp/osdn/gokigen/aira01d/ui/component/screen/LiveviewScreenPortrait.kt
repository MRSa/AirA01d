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
import jp.osdn.gokigen.aira01d.ui.component.widget.AFLockUnlockButton
import jp.osdn.gokigen.aira01d.ui.component.widget.ApertureButton
import jp.osdn.gokigen.aira01d.ui.component.widget.DriveModeButton
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
fun LiveviewScreenPortrait(navController: NavHostController, liveviewModel: LiveviewViewModel, cameraStatusViewModel: CameraStatusViewModel)
{
    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        LiveviewWidget(
            liveviewModel,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // これで「余ったスペースを全部使う」指示になる
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
        )
        {
            TakeModeButton(cameraStatusViewModel)
            ShutterSpeedButton(cameraStatusViewModel)
            ApertureButton(cameraStatusViewModel)
            IsoSensitivityButton(cameraStatusViewModel)
            PictureEffectButton(cameraStatusViewModel)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f),
            horizontalArrangement = Arrangement.Center
        )
        {
            ShutterButton(liveviewModel, cameraStatusViewModel)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp), // 少し余裕を持たせる
            horizontalArrangement = Arrangement.SpaceEvenly // ボタンを均等配置
        ) {
            DriveModeButton(cameraStatusViewModel)
            FocusModeButton(cameraStatusViewModel)
            WhiteBalanceButton(cameraStatusViewModel)
            ExposureCompensationButton(cameraStatusViewModel)
            ExposureWarningText(cameraStatusViewModel)
        }

        // 操作ボタン：必要な分だけ高さを取る
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp), // 少し余裕を持たせる
            horizontalArrangement = Arrangement.SpaceEvenly // ボタンを均等配置
        ) {
            ConnectButton(cameraStatusViewModel)
            WifiConfigButton(liveviewModel)
            AFLockUnlockButton(liveviewModel)
            MirrorImageButton(liveviewModel)
            ShowGridButton(liveviewModel)
            PowerOffButton(cameraStatusViewModel)
        }
    }
}
