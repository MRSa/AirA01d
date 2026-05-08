package jp.osdn.gokigen.aira01d.ui.component.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset.Companion.Zero
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
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
fun LiveviewScreenLandscape(
    navController: NavHostController,
    liveviewModel: LiveviewViewModel,
    cameraStatusViewModel: CameraStatusViewModel,
    selfTimerViewModel: SelfTimerViewModel
) {
    // 上部バーの表示状態を管理（初期状態は表示する）
    var isTopBarVisible by remember { mutableStateOf(true) }

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
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
        ) {
            Row {
                ConnectButton(cameraStatusViewModel, Modifier.weight(1f))
                TakeModeButton(liveviewModel, cameraStatusViewModel, Modifier.weight(2f))
            }

            Row {
                Column(Modifier.weight(1f))
                {
                    WifiConfigButton(liveviewModel)
                    CameraPropertyListButton(navController, cameraStatusViewModel)
                }
                RecordImageButton(navController, cameraStatusViewModel, Modifier.weight(2f).height(80.dp))
            }

            Row {
                ApplicationPreferencesButton(navController, cameraStatusViewModel, Modifier.weight(1f))
                AELockStateButton(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
                AEModeButton(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
            }
            Row {
                ShowGridButton(liveviewModel, Modifier.weight(1f))
                DriveModeButton(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
                FocusModeButton(cameraStatusViewModel, Modifier.weight(1f))
            }
            Row {
                PowerOffButton(cameraStatusViewModel, Modifier.weight(1f))
                AspectRatioButton(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
                RawModeButton(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
            }
            Spacer(Modifier.weight(1f))
            InformationArea1(cameraStatusViewModel, Modifier.fillMaxWidth().height(80.dp))
        }

        // --- 中央エリア (ライブビュー画面 + 重なる上部バー) ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            // ----- スワイプアクションでパネル表示・非表示を切り替える
                            //         (dragAmount が負なら上方向（消す）、正なら下方向（出す）)

                            if (dragAmount < -10f) {   // 感度調整のための閾値
                                isTopBarVisible = false
                            } else if (dragAmount > 10f) {
                                isTopBarVisible = true
                            }
                            // イベントを消費して、ドラッグ中に他の要素が反応しないようにする
                            if (change.positionChange() != Zero) change.consume()
                        }
                    )
                }
        ) {
            // 背面：ライブビュー画面 (fillMaxSize でBoxの全領域を使用)
            LiveviewWidget(
                liveviewModel,
                selfTimerViewModel,
                Modifier.fillMaxSize()
            )

            // 前面：半透明の上部バー (AnimatedVisibility でアニメーションし、表示・非表示を切り替える)
            TopOverlayBar(
                isTopBarVisible,
                liveviewModel,
                cameraStatusViewModel,
                Modifier.align(Alignment.TopCenter)
            )
        }

        // --- 右サイドパネル (スクロール可能) ---
        Column(
            modifier = Modifier
                .width(150.dp)
                .fillMaxHeight()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.End))
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PictureEffectButton(liveviewModel, cameraStatusViewModel, Modifier.fillMaxWidth())

            Spacer(Modifier.height(1.dp))

            Row(Modifier.fillMaxWidth()) {
                MirrorImageButton(liveviewModel, Modifier.weight(1f))
                LiveviewMagnifyButton(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
            }
            Spacer(Modifier.height(1.dp))

            // シャッターボタンは大きい
            ShutterButton(liveviewModel, cameraStatusViewModel, selfTimerViewModel, Modifier.fillMaxWidth().height(100.dp))

            Spacer(Modifier.height(1.dp))

            Row(Modifier.fillMaxWidth()) {
                AFLockUnlockButton(liveviewModel,Modifier.weight(1f))
                CameraTuningButton(cameraStatusViewModel, Modifier.weight(1f))
            }

            Spacer(Modifier.height(1.dp))

            Row(Modifier.fillMaxWidth()) {
                SelfTimerButton(selfTimerViewModel, liveviewModel, Modifier.weight(1f))
                RemainBatteryArea(liveviewModel, cameraStatusViewModel, Modifier.weight(1f))
            }
            InformationArea2(cameraStatusViewModel, Modifier.fillMaxWidth().height(80.dp))
        }
    }
}

@Composable
fun TopOverlayBar(visible: Boolean, liveviewModel: LiveviewViewModel, cameraStatusViewModel: CameraStatusViewModel, modifier: Modifier = Modifier)
{
    // --- 半透明の上部バー部分 (AnimatedVisibility でアニメーションし、表示・非表示を切り替える)
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        // --- MaterialThemeについて、一時的に「ダークモード」設定で上書きする
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color.White,      // ボタンが primary を使っているなら白に
                onPrimary = Color.Black,    // ボタン内の文字が onPrimary なら黒に
                surface = Color.Transparent // 背景などは透明にする
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.4f)) // 半透明の背景
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val mod = Modifier.weight(1f)
                ShutterSpeedButton(liveviewModel, cameraStatusViewModel, mod)
                ApertureButton(liveviewModel, cameraStatusViewModel, mod)
                ExposureCompensationButton(liveviewModel, cameraStatusViewModel, mod)
                IsoSensitivityButton(liveviewModel, cameraStatusViewModel, mod)
                WhiteBalanceButton(liveviewModel, cameraStatusViewModel, mod)
            }
        }
    }
}
