package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.aira01d.ui.component.widget.property.PropertyTextButton
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel

@Composable
fun PictureEffectButton(
    viewModel: LiveviewViewModel,
    controlModel: CameraStatusViewModel,
    modifier: Modifier = Modifier
) {
    // ----- ステータスを監視する
    val takeMode = controlModel.takeMode.observeAsState()
    val isLvActivated = viewModel.isLvActivated.observeAsState()
    val pictureEffect = controlModel.pictureEffect.observeAsState()
    val artFilter = controlModel.artFilter.observeAsState()

    val isArtFilter = (isLvActivated.value == true)&&(when (takeMode.value) {
        "ART" -> { true }
        else -> { false }
    })
    val targetProperty = if (isArtFilter) { ICameraStatus.CameraProperty.ArtFilter } else { ICameraStatus.CameraProperty.PictureEffect }
    val currentValue = if (isArtFilter) { artFilter.value ?: "" } else { pictureEffect.value ?: "" }

    PropertyTextButton(
        controlModel = controlModel,
        targetProperty = targetProperty,
        currentValue = currentValue,
        isEditable = (isLvActivated.value == true),
        modifier = modifier
    )
}
