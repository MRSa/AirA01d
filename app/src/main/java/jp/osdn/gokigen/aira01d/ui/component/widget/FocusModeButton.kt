package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel

@Composable
fun FocusModeButton(
    controlModel: CameraStatusViewModel,
    modifier: Modifier = Modifier
) {
    val targetProperty = ICameraStatus.CameraProperty.FocusMode
    val haptic = LocalHapticFeedback.current

    // ----- ステータスを監視する
    val focusMode = controlModel.focusMode.observeAsState()

    // ----- ダイアログの表示状態を管理
    var showDialogFocusMode by remember { mutableStateOf(false) }

    // --- 通信完了を監視してダイアログを開く --- 一覧が更新され、かつ空でない場合にダイアログを表示
    LaunchedEffect(controlModel.propertyList, controlModel.activeProperty) {
        if ((controlModel.activeProperty == targetProperty)&&(controlModel.propertyList.isNotEmpty())) {
            showDialogFocusMode = true
        }
    }

    // ----- ステータスに合わせてアイコンをと色を決める
    val iconId = when (focusMode.value) {
        "MF" -> R.drawable.str_mf
        "S-AF" -> R.drawable.str_saf
        "C-AF" -> R.drawable.str_caf
        else -> R.drawable.outline_question_mark_24
    }
    val iconColor = MaterialTheme.colorScheme.primary

    // ----- ボタンの表示
    IconButton(
        onClick = {
            controlModel.loadPropertyList(targetProperty)

            // haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        },
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = "Focus mode",
            tint = iconColor,
        )
    }

    // ----- モード変更ダイアログの表示
    if (showDialogFocusMode)
    {
        // ----- 振動フィードバック
        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)

        AlertDialog(
            onDismissRequest = {
                showDialogFocusMode = false
                controlModel.onSelectPropertyDialogDismissed()
            },
            title = { Text(text = "${stringResource(R.string.dialog_title_selection)} : ${focusMode.value}") },
            text = {
                // スクロール可能なリストを表示
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    controlModel.propertyList.forEach { property ->
                        val mode = when (property) {
                            "FOCUS_MF" -> "MF"
                            "FOCUS_SAF" -> "S-AF"
                            else -> property
                        }
                        OutlinedButton(
                            onClick = {
                                // 選択したアイテムで Propertyを更新
                                val requestProperty = when (mode) {
                                    "MF" -> "FOCUS_MF"
                                    "S-AF" -> "FOCUS_SAF"
                                    else -> mode
                                }
                                controlModel.setProperty(targetProperty, requestProperty)
                                showDialogFocusMode = false
                                controlModel.onSelectPropertyDialogDismissed()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = mode,
                                modifier = Modifier.padding(vertical = 2.dp),
                                style =  TextStyle(
                                    textDecoration = if (mode == focusMode.value) { TextDecoration.Underline } else { TextDecoration.None },
                                    fontWeight = if (mode == focusMode.value) { FontWeight.Bold } else { FontWeight.Normal },
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
                        showDialogFocusMode = false
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
