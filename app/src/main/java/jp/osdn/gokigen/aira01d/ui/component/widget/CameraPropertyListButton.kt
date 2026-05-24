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
import androidx.navigation.NavHostController
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel

@Composable
fun CameraPropertyListButton(navController: NavHostController, viewModel: CameraStatusViewModel, modifier: Modifier = Modifier)
{
    val haptic = LocalHapticFeedback.current
    val cameraConnectionStatus = viewModel.cameraConnectionStatus.observeAsState()
    val cameraProtocol = viewModel.cameraProtocol.observeAsState()

    val isEnabled = ((cameraConnectionStatus.value == ICameraConnectionStatus.CameraConnectionStatus.CONNECTED)&&
        (cameraProtocol.value == ICameraConnectionStatus.CameraProtocol.OPC))

    // ----- ステータスに合わせてアイコンをと色を決める
    val iconId = R.drawable.outline_settings_photo_camera_24
    val iconColor = MaterialTheme.colorScheme.primary

    // ----- ボタンの表示
    IconButton(
        enabled = isEnabled,
        onClick = {
            // ----- カメラと接続中かつ、OPC接続のときのみ、画面遷移する
            if (isEnabled)
            {
                // 画面を開いたことを通知する
                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)

                // 設定画面への遷移
                navController.navigate("CameraPreferenceScreen") {
                    // ボタン連打による画面の重複スタックを防止
                    launchSingleTop = true
                }
            }
        },
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = "camera property list",
            tint = iconColor
        )
    }

}
