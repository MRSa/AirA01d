package jp.osdn.gokigen.aira01d.ui.component.widget.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel

@Composable
fun ZoomDirectionSelectMenu(
    controlModel: CameraStatusViewModel,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .width(180.dp)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // タイトル部分
            Text(
                text = "${stringResource(R.string.dialog_title_zoom_direction)} : OMDS",
                style = MaterialTheme.typography.titleSmall
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        controlModel.driveZoomLens(-1)
                    },
                    modifier = modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_zoom_out_24),
                        contentDescription = "zoom out",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.weight(2f))

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        controlModel.driveZoomLens(0)
                    },
                    modifier = modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_stop_24),
                        contentDescription = "zoom stop",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.weight(2f))
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        controlModel.driveZoomLens(1)
                    },
                    modifier = modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_zoom_in_24),
                        contentDescription = "zoom in",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // キャンセルボタン（元々の confirmButton 部分）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        }
    }
}
