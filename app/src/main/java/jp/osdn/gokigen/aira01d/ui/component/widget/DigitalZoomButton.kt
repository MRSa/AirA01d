package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.component.widget.dialog.DigitalZoomScaleSelectionDialog
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel
import jp.osdn.gokigen.aira01d.ui.theme.AirA01dTheme

@Composable
fun DigitalZoomButton(
    viewModel: LiveviewViewModel,
    controlModel: CameraStatusViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // すべての状態を by で監視
    val isLvActivated by viewModel.isLvActivated.observeAsState(initial = false)
    val digitalZoomScaleCurrent by controlModel.digitalZoomScaleCurrent.observeAsState(initial = 100)
    val digitalZoomScaleList by controlModel.digitalZoomScaleList.observeAsState(emptyList())

    // ダイアログの表示状態も ViewModel から受け取る
    val showDialog by controlModel.showDigitalZoomScaleDialog.observeAsState(initial = false)

    val isZoomed by remember { derivedStateOf { digitalZoomScaleCurrent > 100 } }
    val iconId = if (isZoomed) R.drawable.digital_zooming else R.drawable.d_zoom
    val iconColor = if (isZoomed) AirA01dTheme.customColors.warning else MaterialTheme.colorScheme.primary

    IconButton(
        onClick = {
            if (isLvActivated) {
                // デジタルズーム可否のチェックを確認する （デジタルズーム可能な場合には、ダイアログを表示する）
                controlModel.checkDigitalZoomScale()
            }
        },
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = "digital zoom",
            tint = iconColor
        )
    }

    if (showDialog) {
        // ----- ダイアログが表示されたトリガーでバイブレーションを1回実行
        LaunchedEffect(Unit) {
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        }

        DigitalZoomScaleSelectionDialog(
            currentFocal = digitalZoomScaleCurrent,
            focalList = digitalZoomScaleList,
            onSelect = { scale ->
                controlModel.changeDigitalZoomScale(scale)
                controlModel.dismissDigitalZoomScaleDialog() // Dialog Close処理をViewModelに通知
            },
            onDismiss = {
                controlModel.dismissDigitalZoomScaleDialog() // Dialog Close処理をViewModelに通知
            }
        )
    }
}