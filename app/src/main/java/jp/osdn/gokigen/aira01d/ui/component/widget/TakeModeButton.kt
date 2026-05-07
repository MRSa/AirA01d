package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel

@Composable
fun TakeModeButton(
    viewModel: LiveviewViewModel,
    controlModel: CameraStatusViewModel,
    modifier: Modifier = Modifier
)
{
    val targetProperty = ICameraStatus.CameraProperty.TakeMode
    val haptic = LocalHapticFeedback.current

    // ----- ステータスを監視する
    val takeMode = controlModel.takeMode.observeAsState()
    val isLvActivated = viewModel.isLvActivated.observeAsState()

    val textDecoration = if (isLvActivated.value == true) { TextDecoration.Underline } else { TextDecoration.None }

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
            // ---- 変更用の選択肢を取得する
            controlModel.loadPropertyList(targetProperty)
        },
        modifier = modifier
            .height(48.dp)
            .widthIn(min = 48.dp, max = 106.dp)
    ) {
        Text(
            text = takeMode.value ?: "???",
            style = TextStyle(
                textDecoration = textDecoration,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }

    // ----- モード変更ダイアログの表示
    if (showDialog)
    {
        // ----- 振動フィードバック
        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)

        AlertDialog(
            onDismissRequest = {
                showDialog = false
                controlModel.onSelectPropertyDialogDismissed()
            },
            title = { Text(text = "${stringResource(R.string.dialog_title_selection)} : ${takeMode.value}") },
            text = {
                // スクロール可能なリストを表示
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    controlModel.propertyList.forEach { mode ->
                        OutlinedButton(
                            onClick = {
                                // 選択したアイテムで Propertyを更新
                                controlModel.setProperty(targetProperty, mode)
                                showDialog = false
                                controlModel.onSelectPropertyDialogDismissed()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = mode,
                                modifier = Modifier.padding(vertical = 2.dp),
                                style =  TextStyle(
                                    textDecoration = if (mode == takeMode.value) { TextDecoration.Underline } else { TextDecoration.None },
                                    fontWeight = if (mode == takeMode.value) { FontWeight.Bold } else { FontWeight.Normal },
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        showDialog = false
                        controlModel.onSelectPropertyDialogDismissed()
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
}
