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
fun DriveModeButton(
    viewModel: LiveviewViewModel,
    controlModel: CameraStatusViewModel,
    modifier: Modifier = Modifier
) {
    // ----- ステータスを監視する
    val driveMode = controlModel.driveMode.observeAsState()
    val isLvActivated = viewModel.isLvActivated.observeAsState()

    // ----- ステータスに合わせてアイコンをと色を決める (仮)
    val iconId = when (driveMode.value) {
        "DRIVE_NORMAL" -> R.drawable.outline_crop_square_24
        "DRIVE_CONTINUE" -> R.drawable.outline_auto_awesome_motion_24
        "normal" -> R.drawable.outline_crop_square_24
        "silent-normal" -> R.drawable.outline_crop_square_24
        else -> R.drawable.outline_question_mark_24
    }
    val iconColor = MaterialTheme.colorScheme.primary

    PropertyIconButton(
        controlModel = controlModel,
        targetProperty = ICameraStatus.CameraProperty.DriveMode,
        currentValue = driveMode.value ?: "???",
        description = "Drive Mode",
        iconId = iconId,
        iconColor = iconColor,
        isEditable = (isLvActivated.value == true),
        modifier = modifier
    )
}
