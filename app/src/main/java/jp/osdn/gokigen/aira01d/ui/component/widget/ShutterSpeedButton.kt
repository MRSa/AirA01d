package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.aira01d.ui.component.widget.property.PropertyTextButton
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel


@Composable
fun ShutterSpeedButton(
    viewModel: LiveviewViewModel,
    controlModel: CameraStatusViewModel,
    modifier: Modifier = Modifier
) {
    // ----- ステータスを監視する
    val isLvActivated = viewModel.isLvActivated.observeAsState()
    val takeMode = controlModel.takeMode.observeAsState()
    val tv = controlModel.tv.observeAsState()

    val isEditable = (isLvActivated.value == true)&&(when (takeMode.value) {
        "M" -> { true }
        "S" -> { true }
        else -> { false }
    })

    PropertyTextButton(
        controlModel = controlModel,
        targetProperty = ICameraStatus.CameraProperty.ShutterSpeed,
        currentValue = tv.value ?: "",
        isEditable = isEditable,
        modifier = modifier
    )
}
