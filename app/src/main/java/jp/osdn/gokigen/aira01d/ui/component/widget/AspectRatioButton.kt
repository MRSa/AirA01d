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
fun AspectRatioButton(
    viewModel: LiveviewViewModel,
    controlModel: CameraStatusViewModel,
    modifier: Modifier = Modifier
) {
    // ----- ステータスを監視する
    val aspectRatio = controlModel.aspectRatio.observeAsState()
    val isLvActivated = viewModel.isLvActivated.observeAsState()

    // ----- ステータスに合わせてアイコンをと色を決める
    val iconId = when (aspectRatio.value) {
        "04_03" -> R.drawable.outline_aspect_ratio_24
        "03_02" -> R.drawable.outline_crop_3_2_24
        "16_09" -> R.drawable.outline_crop_16_9_24
        "03_04" -> R.drawable.outline_crop_portrait_24
        "06_06" -> R.drawable.outline_crop_square_24
        else -> R.drawable.outline_aspect_ratio_24
    }
    val iconColor = MaterialTheme.colorScheme.primary

    PropertyIconButton(
        controlModel = controlModel,
        targetProperty = ICameraStatus.CameraProperty.AspectRatio,
        currentValue = aspectRatio.value ?: "???",
        description = "Aspect Ratio",
        iconId = iconId,
        iconColor = iconColor,
        isEditable = (isLvActivated.value == true),
        keyNameHeader = "aspect_",
        modifier = modifier
    )
}
