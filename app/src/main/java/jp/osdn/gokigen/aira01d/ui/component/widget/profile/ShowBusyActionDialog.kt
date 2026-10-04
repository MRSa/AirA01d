package jp.osdn.gokigen.aira01d.ui.component.widget.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import jp.osdn.gokigen.aira01d.R

@Composable
fun ShowBusyActionDialog(
    isAbortable: Boolean,
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        // ----- ダイアログ外のタップや戻るキーで勝手に閉じないように制御する
        onDismissRequest = {
            if (isAbortable) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = isAbortable,
            dismissOnClickOutside = isAbortable
        ),
        icon = {
            Icon(
                painter = painterResource(R.drawable.baseline_info_outline_24),
                contentDescription = "Busy Action"
            )
        },
        title = { Text(text = title) },
        // ----- メッセージとインジケータを配置
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(text = message)

                Spacer(modifier = Modifier.height(16.dp))

                // 円形プログレスバー表示
                CircularProgressIndicator()
            }
        },
        confirmButton = {
            if (isAbortable) {
                OutlinedButton(onClick = onDismiss) {
                    Text(stringResource(R.string.button_abort))
                }
            }
        }
    )
}
