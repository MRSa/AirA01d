package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.component.widget.property.PropertyIconButton
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel

@Composable
fun AEModeButton(
    viewModel: LiveviewViewModel,
    controlModel: CameraStatusViewModel,
    modifier: Modifier = Modifier
) {
    // ----- ステータスを監視する
    val meteringMode = controlModel.meteringMode.observeAsState()
    val isLvActivated = viewModel.isLvActivated.observeAsState()

    // ----- ステータスに合わせてアイコンをと色を決める (仮)
    val iconId = when (meteringMode.value) {
        "AE_ESP" -> R.drawable.outline_crop_free_24
        "AE_CENTER" -> R.drawable.outline_center_focus_strong_24
        "AE_PINPOINT" -> R.drawable.outline_filter_center_focus_24
        else -> R.drawable.outline_question_mark_24
    }
    val iconColor = MaterialTheme.colorScheme.primary

    PropertyIconButton(
        controlModel = controlModel,
        targetProperty = ICameraStatus.CameraProperty.MeteringMode,
        currentValue = meteringMode.value ?: "",
        description = "AE Mode",
        iconId = iconId,
        iconColor = iconColor,
        isEditable = (isLvActivated.value == true),
        modifier = modifier
    )
}
