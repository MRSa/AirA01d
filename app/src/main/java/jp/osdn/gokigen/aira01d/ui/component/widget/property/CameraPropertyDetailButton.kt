package jp.osdn.gokigen.aira01d.ui.component.widget.property

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel

@Composable
fun CameraPropertyDetailButton(
    controlModel: CameraStatusViewModel,
    titleRscId: Int,
    targetPropertyString: String,
    modifier: Modifier = Modifier,
)
{
    // ----- ステータスを監視する
    val textDecoration = TextDecoration.None
    val textFontWeight = FontWeight.Bold
    val textColor = MaterialTheme.colorScheme.primary

    // ----- ダイアログの表示状態を管理
    var showDialog by remember { mutableStateOf(false) }

    // --- 通信完了を監視してダイアログを開く --- 一覧が更新され、かつ空でない場合にダイアログを表示
    LaunchedEffect(controlModel.propertyDescriptor, controlModel.queryPropertyName) {
        if ((controlModel.queryPropertyName == targetPropertyString)&&(controlModel.propertyDescriptor.values.isNotEmpty())) {
            showDialog = true
        }
    }

    // ----- ボタンの表示
    OutlinedButton(
        onClick = {
            controlModel.getPropertyDescriptor(targetPropertyString)
        },
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.button_detail),
            style = TextStyle(
                textDecoration = textDecoration,
                fontWeight = textFontWeight,
                color = textColor
            )
        )
    }

    if (showDialog)
    {
        CameraPropertyDescriptorSelectionDialog(
            controlModel = controlModel,
            propertyTitleId = titleRscId,
            propertyName = targetPropertyString,
            current = controlModel.propertyDescriptor.current,
            keyNameHeader = "",
            onClose = {
                showDialog = false
                controlModel.onDismissedPropertyDescriptor()
            }
        )
    }
}
