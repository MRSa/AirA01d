package jp.osdn.gokigen.aira01d.ui.component.widget.property

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel

@Composable
fun PropertyIconButton(
    controlModel: CameraStatusViewModel,
    targetProperty: ICameraStatus.CameraProperty,
    currentValue: String,
    description: String,
    iconId : Int,
    iconColor: Color,
    isEditable: Boolean,
    modifier: Modifier = Modifier
)
{
    // ----- ダイアログの表示状態を管理
    var showDialog by remember { mutableStateOf(false) }

    // --- 通信完了を監視してダイアログを開く --- 一覧が更新され、かつ空でない場合にダイアログを表示
    LaunchedEffect(controlModel.propertyList, controlModel.activeProperty) {
        if ((controlModel.activeProperty == targetProperty)&&(controlModel.propertyList.isNotEmpty())) {
            showDialog = true
        }
    }

    // ----- ボタンの表示
    IconButton(
        onClick = {
            if (isEditable) {
                // ---- 変更用の選択肢を取得する (編集可能時のみ)
                controlModel.loadPropertyList(targetProperty)
            }
        },
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = description,
            tint = iconColor,
            // modifier = Modifier.size(44.dp)
        )
    }

    if (showDialog)
    {
        PropertyTextSelectionDialog(
            controlModel = controlModel,
            targetProperty = targetProperty,
            current = currentValue,
            onClose = {
                showDialog = false
                controlModel.onSelectPropertyDialogDismissed()
            }
        )
    }
}
