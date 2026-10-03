package jp.osdn.gokigen.aira01d.ui.component.widget.profile

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import jp.osdn.gokigen.aira01d.R

@Composable
fun ShowActionResultDialog(
    isSuccess: Boolean,
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = if (isSuccess) { painterResource(R.drawable.baseline_info_outline_24) } else { painterResource(R.drawable.outline_warning_24) },
                contentDescription = "Action Result",
                //tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_close))
            }
        }
    )
}
