package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.aira01d.ui.component.widget.property.PropertyTextButton
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel

@Composable
fun TakeModeButton(
    viewModel: LiveviewViewModel,
    controlModel: CameraStatusViewModel,
    modifier: Modifier = Modifier
)
{
    // ----- ステータスを監視する
    val takeMode = controlModel.takeMode.observeAsState()
    val isLvActivated = viewModel.isLvActivated.observeAsState()

    PropertyTextButton(
        controlModel = controlModel,
        targetProperty = ICameraStatus.CameraProperty.TakeMode,
        currentValue = takeMode.value ?: "???",
        isEditable = (isLvActivated.value == true),
        modifier = modifier
    )
}
