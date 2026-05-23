package jp.osdn.gokigen.aira01d.ui.component.widget.property

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.aira01d.AppSingleton
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel

@Composable
fun PropertyTextSelectionDialog(
    controlModel: CameraStatusViewModel,
    targetProperty: ICameraStatus.CameraProperty,
    current: String,
    keyNameHeader: String = "",
    onClose: () -> Unit,
)
{
    // ----- プロパティ変更ダイアログの表示
    val haptic = LocalHapticFeedback.current

    // ----- 振動フィードバック
    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)

    // ----- リソースの文字列を変換する
    val currentRscId = AppSingleton.resourceConverter.getStringResourceId("$keyNameHeader$current")
    val currentString = if (currentRscId == 0) { current } else { stringResource(currentRscId) }

    AlertDialog(
        onDismissRequest = {
            onClose()
        },
        title = { Text(text = "${stringResource(R.string.dialog_title_selection)} : $currentString") },
        text = {
            // ----- スクロール可能なリストを表示
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                ShowCameraPropertyChoiceButtons(
                    controlModel = controlModel,
                    targetProperty = targetProperty,
                    propertyName = "",
                    propertyNameHeader = keyNameHeader,
                    currentValue = current,
                    propertyList = controlModel.propertyList,
                    onClose = { onClose() }
                )
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = {
                    onClose()
                }
            ) {
                Text(
                    text = stringResource(R.string.button_cancel),
                    style =  TextStyle(
                        textDecoration = TextDecoration.None,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    )
}
