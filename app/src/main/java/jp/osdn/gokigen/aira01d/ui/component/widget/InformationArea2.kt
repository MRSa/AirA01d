package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel

@Composable
fun InformationArea2(viewModel: CameraStatusViewModel, modifier: Modifier = Modifier)
{
    // ----- ステータスを監視する
    val cameraInformation = viewModel.cameraInformation.observeAsState()
    val informationLevel = viewModel.cameraInformationLevel.observeAsState()
    val focalLengthNow = viewModel.focalLengthNow.observeAsState()
    val focalLengthWide = viewModel.focalLengthWide.observeAsState()
    val focalLengthTele = viewModel.focalLengthTele.observeAsState()

    val focalLengthNowInt = focalLengthNow.value ?: 0
    val focalLengthWideInt = focalLengthWide.value ?: 0
    val focalLengthTeleInt = focalLengthTele.value ?: 0

    val focalLength = if (focalLengthWideInt == focalLengthTeleInt) { "${focalLengthNowInt}mm" } else { "${focalLengthNowInt}mm (${focalLengthWideInt}mm - ${focalLengthTeleInt}mm)" }
    val message = "${cameraInformation.value ?: ""} $focalLength"

    // ----- 表示文字の装飾
    val informationLevelValue = informationLevel.value ?: 10
    val fontWeight = if (informationLevelValue > 2) { FontWeight.Normal } else { FontWeight.Bold }
    val textColor =
        if (informationLevelValue > 5 ) { MaterialTheme.colorScheme.primary }
        else if (informationLevelValue > 3 ) { MaterialTheme.colorScheme.tertiary }
        else { MaterialTheme.colorScheme.error }

    // ----- ボタンの表示
    TextButton(
        onClick = {
            // ---- ズームレンズの場合、ズーム操作を行う
        },
        modifier = modifier
            .height(48.dp)
            .widthIn(min = 48.dp, max = 106.dp)
    ) {
        Text(
            text = message,
            style = TextStyle(
                textDecoration = TextDecoration.None,
                fontWeight = fontWeight,
                color = textColor
            )
        )
    }
}
