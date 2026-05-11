package jp.osdn.gokigen.aira01d.ui.component.widget.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.aira01d.R

@Composable
fun ZoomSelectionDialog(
    currentFocal: Int,
    focalList: List<Int>,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${stringResource(R.string.dialog_title_selection)} : ${currentFocal}mm") },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                focalList.forEach { value ->
                    val isCurrent = value == currentFocal
                    OutlinedButton(
                        onClick = { onSelect(value) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "${value}mm",
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            textDecoration = if (isCurrent) TextDecoration.Underline else TextDecoration.None
                        )
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}