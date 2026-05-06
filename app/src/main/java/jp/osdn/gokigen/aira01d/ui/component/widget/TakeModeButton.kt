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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun TakeModeButton(viewModel: CameraStatusViewModel, modifier: Modifier = Modifier)
{
    val haptic = LocalHapticFeedback.current

    // ----- ステータスを監視する
    val takeMode = viewModel.takeMode.observeAsState()

    // ----- ダイアログの表示状態と、表示する選択肢リストを管理
    var showDialog by remember { mutableStateOf(false) }

    // --- ポイント：通信完了を監視してダイアログを開く --- 一覧が更新され、かつ空でない場合にダイアログを表示
    LaunchedEffect(viewModel.propertyList, viewModel.activeProperty) {
        if ((viewModel.activeProperty == ICameraStatus.CameraProperty.TakeMode)&&(viewModel.propertyList.isNotEmpty())) {
            showDialog = true
        }
    }

    // ----- ボタンの表示
    TextButton(
        onClick = {
            // ----- 振動フィードバック
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)

            // ---- 変更用の選択肢を取得する
            viewModel.loadPropertyList(ICameraStatus.CameraProperty.TakeMode)
        },
        modifier = modifier
            .height(48.dp)
            .widthIn(min = 48.dp, max = 106.dp)
    ) {
        Text(
            text = takeMode.value ?: "???",
            style = TextStyle(
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }

    // ----- モード変更ダイアログの表示
    if (showDialog)
    {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                viewModel.onSelectPropertyDialogDismissed()
            },
            title = { Text(text = "${stringResource(R.string.dialog_title_selection)} : ${takeMode.value}") },
            text = {
                // スクロール可能なリストを表示
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    viewModel.propertyList.forEach { mode ->
                        OutlinedButton(
                            onClick = {
                                // 選択したモードをViewModelに反映（メソッド名は仮定）
                                viewModel.setProperty(ICameraStatus.CameraProperty.TakeMode, mode)
                                showDialog = false
                                viewModel.onSelectPropertyDialogDismissed()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = mode,
                                modifier = Modifier.padding(vertical = 8.dp),
                                style =  TextStyle(
                                    textDecoration = TextDecoration.None,
                                    fontWeight = FontWeight.Normal,
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
                        viewModel.onSelectPropertyDialogDismissed()
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
