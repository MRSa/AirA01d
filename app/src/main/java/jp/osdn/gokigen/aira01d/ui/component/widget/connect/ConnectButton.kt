package jp.osdn.gokigen.aira01d.ui.component.widget.connect

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel

@Composable
fun ConnectButton(viewModel: CameraStatusViewModel, modifier: Modifier = Modifier)
{
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    // ----- ステータスを監視する
    val cameraConnectionStatus = viewModel.cameraConnectionStatus.observeAsState()
    val isConnectError = viewModel.isConnectError.observeAsState()

    // ----- ステータスに合わせてアイコンをと色を決める
    val (iconId, iconColor) = when (cameraConnectionStatus.value) {
        ICameraConnectionStatus.CameraConnectionStatus.CONNECTED ->
            R.drawable.outline_cloud_done_24 to MaterialTheme.colorScheme.primary // 接続済み：メインカラー

        ICameraConnectionStatus.CameraConnectionStatus.DISCONNECTED ->
            R.drawable.outline_cloud_off_24 to MaterialTheme.colorScheme.primary // 切断：控えめな色

        ICameraConnectionStatus.CameraConnectionStatus.ERROR ->
            R.drawable.outline_cloud_alert_24 to MaterialTheme.colorScheme.error // 異常：警告の赤

        ICameraConnectionStatus.CameraConnectionStatus.NOT_FOUND ->
            R.drawable.cloud_unknown to MaterialTheme.colorScheme.error // 異常：警告の赤

        ICameraConnectionStatus.CameraConnectionStatus.START ->
            R.drawable.outline_cloud_sync_24 to MaterialTheme.colorScheme.tertiary // 進行中2：第3色（少し目立つ色）

        ICameraConnectionStatus.CameraConnectionStatus.CONNECTING ->
            R.drawable.outline_cloud_sync_24 to MaterialTheme.colorScheme.secondary // 進行中3：第2色

        ICameraConnectionStatus.CameraConnectionStatus.CHECK_WIFI ->
            R.drawable.outline_cloud_24 to MaterialTheme.colorScheme.tertiary // 進行中1：第3色（少し目立つ色）

        else ->
            R.drawable.outline_question_mark_24 to MaterialTheme.colorScheme.outline
    }

    // ----- ボタンの表示
    IconButton(
        onClick = {
            if (cameraConnectionStatus.value == ICameraConnectionStatus.CameraConnectionStatus.CONNECTED)
            {
                viewModel.disconnectFromCamera()
            }
            else if (cameraConnectionStatus.value == ICameraConnectionStatus.CameraConnectionStatus.DISCONNECTED)
            {
                viewModel.connectToCamera()
            }
            else if ((cameraConnectionStatus.value == ICameraConnectionStatus.CameraConnectionStatus.DISCONNECTED) ||
                (cameraConnectionStatus.value == ICameraConnectionStatus.CameraConnectionStatus.ERROR) ||
                (cameraConnectionStatus.value == ICameraConnectionStatus.CameraConnectionStatus.NOT_FOUND))
            {
                viewModel.startCamera(context)
            }
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        },
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = "connection status",
            tint = iconColor
        )
    }

    val onOpenWifiSettings = {
        // ----- WI-FI設定画面を開く
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        context.startActivity(intent)
    }

    val onRetryAction = {
        // ----- 再接続を実行
        viewModel.connectToCamera()
    }

    // ----- 接続エラー状態を検出した場合は、ダイアログを表示する
    if (isConnectError.value == true)
    {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.outline_warning_24),
                    contentDescription = "warning",
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(text = stringResource(R.string.dialog_title_connect_failed))
            },
            text = {
                Text(text = stringResource(R.string.camera_not_connected))
                // エラーメッセージ（スタックトレース等）を表示
            },
            // 右側のボタン（ポジティブなアクション）
            confirmButton = {
                // 枠線ありのボタンに変更
                OutlinedButton(onClick = onRetryAction) {
                    Text(stringResource(R.string.dialog_button_retry))
                }
            },
            // 左側のボタン（ネガティブ/サブのアクション）
            dismissButton = {
                // 枠線ありのボタンに変更
                OutlinedButton(onClick = onOpenWifiSettings) {
                    Text(stringResource(R.string.dialog_button_network_settings))
                }
            }
        )
    }
}
