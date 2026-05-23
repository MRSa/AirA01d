package jp.osdn.gokigen.aira01d.ui.component.widget.property

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.aira01d.AppSingleton
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel

@Composable
fun ShowCameraPropertyChoiceButtons(
    controlModel: CameraStatusViewModel,
    targetProperty: ICameraStatus.CameraProperty?,
    propertyName: String,
    propertyNameHeader: String = "",
    currentValue: String,
    propertyList: List<String>,
    onClose: () -> Unit,
)
{
    propertyList.forEach { mode ->
        OutlinedButton(
            onClick = {
                // ----- 選択したアイテムで Propertyを更新
                if (targetProperty != null)
                {
                    controlModel.setProperty(targetProperty, mode)
                }
                else
                {
                    controlModel.setPropertyString(propertyName, mode)
                }
                onClose()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            val rscId = AppSingleton.resourceConverter.getStringResourceId("$propertyNameHeader$mode")
            val modeString = if (rscId == 0) { mode } else { stringResource(rscId) }
            Text(
                text = modeString,
                modifier = Modifier.padding(vertical = 2.dp),
                style =  TextStyle(
                    textDecoration = if (mode == currentValue) { TextDecoration.Underline } else { TextDecoration.None },
                    fontWeight = if (mode == currentValue) { FontWeight.Bold } else { FontWeight.Normal },
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
