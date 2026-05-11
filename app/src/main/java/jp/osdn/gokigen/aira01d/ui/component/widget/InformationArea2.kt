package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.aira01d.ui.component.widget.dialog.ZoomSelectionDialog
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel

@Composable
fun InformationArea2(
    controlModel: CameraStatusViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showDialog by remember { mutableStateOf(false) }

    // Stateの集約
    val cameraInformation by controlModel.cameraInformation.observeAsState("")
    val informationLevel by controlModel.cameraInformationLevel.observeAsState(10)
    val focalLengthNow by controlModel.focalLengthNow.observeAsState(0)
    val focalLengthWide by controlModel.focalLengthWide.observeAsState(0)
    val focalLengthTele by controlModel.focalLengthTele.observeAsState(0)
    val checkingHardware by controlModel.checkingCameraHardware.observeAsState(false)
    val electricZoom by controlModel.electricZoom.observeAsState("")
    val focalLengthList by controlModel.focalLengthList.observeAsState(emptyList())

    // 表示メッセージと色などの計算部分
    val colorScheme = colorScheme
    val displayInfo by remember {
        derivedStateOf {
            val isZoom = focalLengthWide != focalLengthTele
            val focalText = if (isZoom) "$focalLengthNow mm ($focalLengthWide mm - $focalLengthTele mm)" else "$focalLengthNow mm"
            val color = when {
                informationLevel > 5 -> { colorScheme.primary }
                informationLevel > 3 -> colorScheme.tertiary
                else -> colorScheme.error
            }
            val weight = if (informationLevel > 2) FontWeight.Normal else FontWeight.Bold
            Triple("$cameraInformation $focalText", color, weight)
        }
    }

    // ----- メインボタン
    TextButton(
        onClick = {
            // --- ボタンが押されたときの処理... 電動ズームの時だけダイアログを出す
            if (focalLengthWide < focalLengthTele) {
                controlModel.clearElectricZoomInfo()
                controlModel.checkElectricZoom()
                showDialog = true
            }
        },
        modifier = modifier.height(48.dp).widthIn(min = 48.dp, max = 106.dp)
    ) {
        Text(
            text = displayInfo.first,
            style = TextStyle(fontWeight = displayInfo.third, color = displayInfo.second)
        )
    }

    // ----- ダイアログ制御ロジック
    if (showDialog && !checkingHardware) {
        when (electricZoom) {
            "OK" -> {
                LaunchedEffect(Unit) {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                }
                ZoomSelectionDialog(
                    currentFocal = focalLengthNow,
                    focalList = focalLengthList,
                    onSelect = {
                        controlModel.driveZoomLens(it)
                        showDialog = false
                    },
                    onDismiss = { showDialog = false }
                )
            }
            "NG" -> showDialog = false
        }
    }
}
