package jp.osdn.gokigen.aira01d.ui.component.widget.connect

import android.app.Activity
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel

@Composable
fun PowerOffButton(viewModel: CameraStatusViewModel, modifier: Modifier = Modifier)
{
    val haptic = LocalHapticFeedback.current

    // Contextを取得し、Activityにキャストする
    val context = LocalContext.current
    val activity = context as? Activity

    // ----- ダイアログの表示状態を管理する変数
    var showDialog by remember { mutableStateOf(false) }

    // ----- ボタン表示 （押されたらダイアログを表示して確認する）
    IconButton(
        onClick = {
            showDialog = true
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                  },
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.outline_power_settings_new_24),
            contentDescription = "Power",
            tint = MaterialTheme.colorScheme.primary
        )
    }

    if (showDialog) {
        PowerOffConfirmationDialog(
            onConfirm = {
                showDialog = false

                // --- Power Offの実処理
                viewModel.confirmPowerOff(onFinish = { activity?.finishAndRemoveTask() })
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun PowerOffConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(R.drawable.outline_power_settings_new_24),
                contentDescription = "Power",
                //tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text(text = stringResource(R.string.dialog_title_confirm_power_off)) },
        text = { Text(text = stringResource(R.string.dialog_message_power_off)) },
        confirmButton = {
            OutlinedButton(onClick = onConfirm) {
                Text(stringResource(R.string.button_power_off_confirm))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}
