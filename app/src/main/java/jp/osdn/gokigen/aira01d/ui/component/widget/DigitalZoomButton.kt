package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.a01lib.camera.interfaces.IDigitalZoomControl
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
)
{
    val haptic = LocalHapticFeedback.current

    // ----- ダイアログの表示状態を管理
    var showDialog by remember { mutableStateOf(false) }

    // ----- ステータスを監視する
    val isLvActivated = viewModel.isLvActivated.observeAsState()
    val digitalZoomScaleCurrent = controlModel.digitalZoomScaleCurrent.observeAsState()
    val digitalZoomScaleList by controlModel.digitalZoomScaleList.observeAsState(emptyList())

    // ----- ステータスに合わせてアイコンをと色を決める
    val iconId = if ((digitalZoomScaleCurrent.value ?: 100) > 100) { R.drawable.digital_zooming } else { R.drawable.d_zoom }
    val iconColor = if ((digitalZoomScaleCurrent.value ?: 100) > 100) { AirA01dTheme.customColors.warning } else { MaterialTheme.colorScheme.primary }

    // ----- デジタルズームボタンの表示
    IconButton(
        onClick = {
            if (isLvActivated.value == true)
            {
                // ----- ライブビュー表示時、デジタルズームの拡大表示を行うかどうか
                controlModel.checkDigitalZoomScale(object: IDigitalZoomControl.DigitalZoomScaleCallback {
                    override fun zoomScale(lowerScale: Int, upperScale: Int) {
                        showDialog = true
                    }
                })
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

    if (showDialog)
    {
        LaunchedEffect(Unit) {
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        }
        DigitalZoomScaleSelectionDialog(
            currentFocal = digitalZoomScaleCurrent.value ?: 100,
            focalList = digitalZoomScaleList,
            onSelect = {
                // ----- デジタルズームの実行
                controlModel.changeDigitalZoomScale(it)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}
