package jp.osdn.gokigen.aira01d.ui.component.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
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
import jp.osdn.gokigen.aira01d.ui.component.widget.AFLockUnlockButton
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

@Composable
fun LiveviewScreenPortrait(
    navController: NavHostController,
    liveviewModel: LiveviewViewModel,
    cameraStatusViewModel: CameraStatusViewModel,
    selfTimerViewModel: SelfTimerViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(4.dp)
    ) {
        // --- 上部固定エリア(アイコンを6つ横に並べる)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val modifier = Modifier.weight(1f)
            ConnectButton(cameraStatusViewModel, modifier)
            WifiConfigButton(liveviewModel, modifier)
            CameraPropertyListButton(navController, cameraStatusViewModel, modifier)
            ApplicationPreferencesButton(navController, cameraStatusViewModel, modifier)
            ShowGridButton(liveviewModel, modifier)
            PowerOffButton(cameraStatusViewModel, modifier)
        }

        Spacer(Modifier.height(4.dp))

        // --- メッセージ表示エリア＋アイコン２つ
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            InformationArea1(cameraStatusViewModel, Modifier.weight(1f).height(50.dp))
            SelfTimerButton(selfTimerViewModel, liveviewModel)
            RemainBatteryArea(liveviewModel, cameraStatusViewModel)
        }

        // --- ライブビューの表示画面 ---
        LiveviewWidget(
            liveviewModel,
            selfTimerViewModel,
            Modifier
                .fillMaxWidth()
                .weight(1f) // 空きスペースをすべて使う
                .padding(vertical = 4.dp)
        )

        // --- 下部操作パネル (ここはスクロール可能にする) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()) // スクロール対応
        ) {
            // 撮影モード、シャッタースピード、絞り値の表示
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TakeModeButton(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
                ShutterSpeedButton(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
                ApertureButton(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
            }

            Spacer(Modifier.height(4.dp))

            // --- 撮影画像、シャッター、そして露出補正、鏡像表示、セルフタイマー
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                RecordImageButton(navController, liveviewModel, cameraStatusViewModel, Modifier.weight(1f).height(80.dp))
                ShutterButton(liveviewModel, cameraStatusViewModel, selfTimerViewModel, Modifier.weight(1f).height(80.dp).padding(horizontal = 4.dp))
                Column(Modifier.weight(1f)) {
                    ExposureCompensationButton(liveviewModel, cameraStatusViewModel, Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth()) {
                        MirrorImageButton(liveviewModel, Modifier.weight(1f))
                        LiveviewMagnifyButton(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ISO, WB, Picture Effect
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IsoSensitivityButton(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
                WhiteBalanceButton(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
                PictureEffectButton(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
            }

            Spacer(Modifier.height(4.dp))

            // AEロック状態、AEモード、ドライブモード、フォーカスモード、AFロックモード、カメラチューニング
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val mod = Modifier.weight(1f)
                AELockStateButton(liveviewModel, cameraStatusViewModel, mod)
                AEModeButton(liveviewModel, cameraStatusViewModel, mod)
                DriveModeButton(liveviewModel, cameraStatusViewModel, mod)
                FocusModeButton(cameraStatusViewModel,mod)
                AFLockUnlockButton(liveviewModel,mod)
                CameraTuningButton(cameraStatusViewModel, mod)
            }

            Spacer(Modifier.height(4.dp))

            // アスペクト比、Rawモード、情報表示エリア
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AspectRatioButton(liveviewModel, cameraStatusViewModel)
                Spacer(Modifier.width(4.dp))
                RawModeButton(liveviewModel, cameraStatusViewModel)
                InformationArea2(cameraStatusViewModel, Modifier.weight(1f).height(50.dp).padding(start = 4.dp))
            }
        }
    }
}
