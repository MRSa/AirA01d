package jp.osdn.gokigen.aira01d.ui.component.widget.playback

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import jp.osdn.gokigen.aira01d.R

@Composable
fun MyDatePickerDialog(
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    // 選択された日付の状態を管理（初期値は現在のシステム時刻）
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    // 選択された日付のタイムスタンプ（ミリ秒）を取得
                    datePickerState.selectedDateMillis?.let {
                        onDateSelected(it)
                    }
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.button_ok_select))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    ) {
        // ダイアログの中身（カレンダー本体）
        DatePicker(state = datePickerState)
    }
}