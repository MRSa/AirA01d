package jp.osdn.gokigen.aira01d.ui.component.widget

import android.util.Log
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
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import kotlin.math.roundToInt

@Composable
fun InformationArea2(controlModel: CameraStatusViewModel, modifier: Modifier = Modifier)
{
    val haptic = LocalHapticFeedback.current

    // ----- ダイアログの表示状態を管理
    var showDialog by remember { mutableStateOf(false) }

    // ----- ステータスを監視する
    val cameraInformation = controlModel.cameraInformation.observeAsState()
    val informationLevel = controlModel.cameraInformationLevel.observeAsState()
    val focalLengthNow = controlModel.focalLengthNow.observeAsState()
    val focalLengthWide = controlModel.focalLengthWide.observeAsState()
    val focalLengthTele = controlModel.focalLengthTele.observeAsState()

    val focalLengthNowInt = focalLengthNow.value ?: 0
    val focalLengthWideInt = focalLengthWide.value ?: 0
    val focalLengthTeleInt = focalLengthTele.value ?: 0

    val focalLength = if (focalLengthWideInt == focalLengthTeleInt) { "${focalLengthNowInt}mm" } else { "${focalLengthNowInt}mm (${focalLengthWideInt}mm - ${focalLengthTeleInt}mm)" }
    val message = "${cameraInformation.value ?: ""} $focalLength"

    // ----- 表示文字の装飾
    val informationLevelValue = informationLevel.value ?: 10
    val fontWeight = if (informationLevelValue > 2) { FontWeight.Normal } else { FontWeight.Bold }
    val textColor =
        if (informationLevelValue > 5 ) { MaterialTheme.colorScheme.primary }
        else if (informationLevelValue > 3 ) { MaterialTheme.colorScheme.tertiary }
        else { MaterialTheme.colorScheme.error }

    // ----- ボタンの表示
    TextButton(
        onClick = {
            // ---- ズームレンズの場合、ズーム操作を行う
            if ((focalLengthWideInt != focalLengthTeleInt)&&(focalLengthWideInt < focalLengthTeleInt))
            {
                //  ズームレンズ装着時、また、 Wide < Tele の値になっているとき...
                showDialog = true
            }
        },
        modifier = modifier
            .height(48.dp)
            .widthIn(min = 48.dp, max = 106.dp)
    ) {
        Text(
            text = message,
            style = TextStyle(
                textDecoration = TextDecoration.None,
                fontWeight = fontWeight,
                color = textColor
            )
        )
    }

    if (showDialog)
    {
        // --- ズーム選択ダイアログを表示
        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)

        // --- （ステップズームの選択肢を作る）
        val focalLengthListStr = mutableListOf<String>()
        val focalLengthListInt = mutableListOf<Int>()
        val targetSize = 10  // ズームステップは 10段階にする
        val step = (focalLengthTeleInt.toDouble() - focalLengthWideInt) / (targetSize - 1)
        for (index in 0 until targetSize) {
            // 各ステップの値を計算し、四捨五入してIntに変換
            val value = (focalLengthWideInt + index * step).roundToInt()
            focalLengthListInt.add(value)
        }
        if (focalLengthNowInt !in focalLengthListInt) {
            // 選択肢に現在の焦点距離がなかったときは、末尾に追加する
            focalLengthListInt.add(focalLengthNowInt)
        }
        focalLengthListInt.forEach { focalLengthListStr.add("${it}mm") }

        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = { Text(text = "${stringResource(R.string.dialog_title_selection)} : ${focalLengthNowInt}mm") },
            text = {
                // スクロール可能なリストを表示
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    focalLengthListStr.forEachIndexed{ index, mode ->
                        OutlinedButton(
                            onClick = {
                                // ズームを駆動させる
                                controlModel.driveZoomLens(focalLengthListInt[index])
                                showDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = mode,
                                modifier = Modifier.padding(vertical = 2.dp),
                                style =  TextStyle(
                                    textDecoration = if (mode == "${focalLengthNowInt}mm") { TextDecoration.Underline } else { TextDecoration.None },
                                    fontWeight = if (mode == "${focalLengthNowInt}mm") { FontWeight.Bold } else { FontWeight.Normal },
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
