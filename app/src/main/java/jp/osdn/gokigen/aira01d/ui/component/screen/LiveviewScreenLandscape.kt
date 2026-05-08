package jp.osdn.gokigen.aira01d.ui.component.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import jp.osdn.gokigen.aira01d.ui.component.widget.AELockStateButton
import jp.osdn.gokigen.aira01d.ui.component.widget.AEModeButton
import jp.osdn.gokigen.aira01d.ui.component.widget.ApertureButton
import jp.osdn.gokigen.aira01d.ui.component.widget.ApplicationPreferencesButton
import jp.osdn.gokigen.aira01d.ui.component.widget.AspectRatioButton
import jp.osdn.gokigen.aira01d.ui.component.widget.CameraPropertyListButton
import jp.osdn.gokigen.aira01d.ui.component.widget.CameraTuningButton
import jp.osdn.gokigen.aira01d.ui.component.widget.DriveModeButton
import jp.osdn.gokigen.aira01d.ui.component.widget.ExposureCompensationButton
import jp.osdn.gokigen.aira01d.ui.component.widget.InformationArea1
import jp.osdn.gokigen.aira01d.ui.component.widget.FocusModeButton
import jp.osdn.gokigen.aira01d.ui.component.widget.InformationArea2
import jp.osdn.gokigen.aira01d.ui.component.widget.IsoSensitivityButton
import jp.osdn.gokigen.aira01d.ui.component.widget.LiveviewMagnifyButton
import jp.osdn.gokigen.aira01d.ui.component.widget.connect.ConnectButton
import jp.osdn.gokigen.aira01d.ui.component.widget.drawer.LiveviewWidget
import jp.osdn.gokigen.aira01d.ui.component.widget.MirrorImageButton
import jp.osdn.gokigen.aira01d.ui.component.widget.PictureEffectButton
import jp.osdn.gokigen.aira01d.ui.component.widget.RawModeButton
import jp.osdn.gokigen.aira01d.ui.component.widget.RecordImageButton
import jp.osdn.gokigen.aira01d.ui.component.widget.RemainBatteryArea
import jp.osdn.gokigen.aira01d.ui.component.widget.SelfTimerButton
import jp.osdn.gokigen.aira01d.ui.component.widget.connect.PowerOffButton
import jp.osdn.gokigen.aira01d.ui.component.widget.ShowGridButton
import jp.osdn.gokigen.aira01d.ui.component.widget.ShutterButton
import jp.osdn.gokigen.aira01d.ui.component.widget.ShutterSpeedButton
import jp.osdn.gokigen.aira01d.ui.component.widget.TakeModeButton
import jp.osdn.gokigen.aira01d.ui.component.widget.WhiteBalanceButton
import jp.osdn.gokigen.aira01d.ui.component.widget.connect.WifiConfigButton
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel
import jp.osdn.gokigen.aira01d.ui.model.SelfTimerViewModel

/*
@Composable
fun LiveviewScreenLandscape(
    navController: NavHostController,
    liveviewModel: LiveviewViewModel,
    cameraStatusViewModel: CameraStatusViewModel,
    selfTimerViewModel: SelfTimerViewModel
)
{
    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 上部：ステータス/設定ボタン群
        Row(modifier = Modifier.fillMaxWidth().height(50.dp)) {
            TakeModeButton(liveviewModel, cameraStatusViewModel)
            ShutterSpeedButton(liveviewModel, cameraStatusViewModel)
            ApertureButton(liveviewModel, cameraStatusViewModel)
            IsoSensitivityButton(liveviewModel, cameraStatusViewModel)
            PictureEffectButton(liveviewModel, cameraStatusViewModel)
            WhiteBalanceButton(liveviewModel, cameraStatusViewModel)
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
                LiveviewWidget(liveviewModel, selfTimerViewModel, Modifier.fillMaxWidth())
            }

            // 右側：シャッター・アイコン群
            Column(modifier = Modifier.width(110.dp).fillMaxHeight()) {
                AFLockUnlockButton(liveviewModel)
                SelfTimerButton(selfTimerViewModel, liveviewModel)
                MirrorImageButton(liveviewModel)
                ShowGridButton(liveviewModel)
                ShutterButton(liveviewModel, cameraStatusViewModel, selfTimerViewModel)
                FocusModeButton(cameraStatusViewModel)
                ExposureCompensationButton(liveviewModel, cameraStatusViewModel)
                InformationArea1(cameraStatusViewModel)
            }
        }
    }
}
*/
@Composable
fun LiveviewScreenLandscape(
    navController: NavHostController,
    liveviewModel: LiveviewViewModel,
    cameraStatusViewModel: CameraStatusViewModel,
    selfTimerViewModel: SelfTimerViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        // --- 左サイドパネル (スクロール可能) ---
        Column(
            modifier = Modifier
                .width(150.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // 上部ボタン群の混成レイアウト
            Row {
                PowerOffButton(cameraStatusViewModel, Modifier.weight(1f))
                TakeModeButton(liveviewModel, cameraStatusViewModel, Modifier.weight(2f))
            }
            Row {
                Column(Modifier.weight(1f))
                {
                    ConnectButton(cameraStatusViewModel)
                    WifiConfigButton(liveviewModel)
                }
                RecordImageButton(navController, cameraStatusViewModel, Modifier.weight(2f).height(80.dp))
            }
            // アイコンボタン群の配置
            Row {
                FocusModeButton(cameraStatusViewModel, Modifier.weight(1f))
                AspectRatioButton(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
                RawModeButton(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
            }
            Row {
                AELockStateButton(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
                AEModeButton(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
                DriveModeButton(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
            }
            Row {
                ShowGridButton(liveviewModel, Modifier.weight(1f))
                CameraTuningButton(cameraStatusViewModel, Modifier.weight(1f))
                CameraPropertyListButton(navController, cameraStatusViewModel, Modifier.weight(1f))
            }

            Spacer(Modifier.weight(1f))
            InformationArea1(cameraStatusViewModel, Modifier.fillMaxWidth().height(80.dp))
        }

        // --- 中央エリア (メインプレビュー + 上部バー) ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 4.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val mod = Modifier.weight(1f)
                ShutterSpeedButton(liveviewModel, cameraStatusViewModel, mod)
                ApertureButton(liveviewModel, cameraStatusViewModel, mod)
                ExposureCompensationButton(liveviewModel, cameraStatusViewModel,mod)
                IsoSensitivityButton(liveviewModel, cameraStatusViewModel, mod)
            }
            LiveviewWidget(liveviewModel, selfTimerViewModel, Modifier.fillMaxSize().padding(top = 4.dp))
        }

        // --- 右サイドパネル (スクロール可能) ---
        Column(
            modifier = Modifier
                .width(150.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PictureEffectButton(liveviewModel, cameraStatusViewModel, Modifier.fillMaxWidth())
            WhiteBalanceButton(liveviewModel, cameraStatusViewModel, Modifier.fillMaxWidth())

            Spacer(Modifier.height(8.dp))

            // シャッターボタンを強調
            ShutterButton(liveviewModel, cameraStatusViewModel, selfTimerViewModel, Modifier.fillMaxWidth().height(100.dp))

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth()) {
                MirrorImageButton(liveviewModel, Modifier.weight(1f))
                LiveviewMagnifyButton(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth()) {
                ApplicationPreferencesButton(navController, cameraStatusViewModel, Modifier.weight(1f))
                SelfTimerButton(selfTimerViewModel, liveviewModel, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth()) {
                RemainBatteryArea(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.weight(1f))
            InformationArea2(cameraStatusViewModel, Modifier.fillMaxWidth().height(80.dp))
        }
    }
}
