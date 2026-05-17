package jp.osdn.gokigen.aira01d.ui.component.widget.property

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.aira01d.AppSingleton
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel

@Composable
fun PropertyTextButton(
    controlModel: CameraStatusViewModel,
    targetProperty: ICameraStatus.CameraProperty,
    currentValue: String,
    isEditable: Boolean,
    modifier: Modifier = Modifier,
    keyNameHeader: String = "",
)
{
    // ----- ステータスを監視する
    val textDecoration = if (isEditable) { TextDecoration.Underline } else { TextDecoration.None }
    val textFontWeight = FontWeight.Bold
    val textColor = MaterialTheme.colorScheme.primary

    // ----- リソースの文字列を変換する
    val currentRscId = AppSingleton.resourceConverter.getStringResourceId("$keyNameHeader$currentValue")
    val currentString = if (currentRscId == 0) { currentValue } else { stringResource(currentRscId) }

    // ----- ダイアログの表示状態を管理
    var showDialog by remember { mutableStateOf(false) }

    // --- 通信完了を監視してダイアログを開く --- 一覧が更新され、かつ空でない場合にダイアログを表示
    LaunchedEffect(controlModel.propertyList, controlModel.activeProperty) {
        if ((controlModel.activeProperty == targetProperty)&&(controlModel.propertyList.isNotEmpty())) {
            showDialog = true
        }
    }

    // ----- ボタンの表示
    TextButton(
        onClick = {
            if (isEditable) {
                // ---- (編集可能な時だけ)変更用の選択肢を取得する
                controlModel.loadPropertyList(targetProperty)
            }
        },
        modifier = modifier
            .height(48.dp)
            .widthIn(min = 48.dp, max = 106.dp)
    ) {
        Text(
            text = currentString,
            style = TextStyle(
                textDecoration = textDecoration,
                fontWeight = textFontWeight,
                color = textColor
            )
        )
    }

    if (showDialog)
    {
        PropertyTextSelectionDialog(
            controlModel = controlModel,
            targetProperty = targetProperty,
            current = currentValue,
            keyNameHeader = keyNameHeader,
            onClose = {
                showDialog = false
                controlModel.onSelectPropertyDialogDismissed()
            }
        )
    }
}
