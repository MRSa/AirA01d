package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.theme.AirA01dTheme

@Composable
fun InformationArea1(viewModel: CameraStatusViewModel, modifier: Modifier = Modifier)
{
    // ----- ステータスを監視する
    val exposureWarningResId = viewModel.exposureWarningResId.observeAsState()
    val exposureWarningLevel = viewModel.exposureWarningLevel.observeAsState()
    val isMediaBusy = viewModel.isMediaBusy.observeAsState()
    val isCaptureActivated = viewModel.isCaptureActivated.observeAsState()

    // ----- 表示文字の装飾
    val resId = exposureWarningResId.value ?: R.string.blank
    var informationText = stringResource(resId)
    var warningLevel = exposureWarningLevel.value ?: 10
    if (isMediaBusy.value == true)
    {
        informationText = "$informationText  ${stringResource(R.string.media_busy)}"
        warningLevel = 4
    }
    if (isCaptureActivated.value == true)
    {
        informationText = "$informationText   ${stringResource(R.string.capturing)}"
        warningLevel = 1
    }

    val fontWeight = if (warningLevel > 4) { FontWeight.Normal } else { FontWeight.Bold }
    val textColor =  // 0,1,2 がエラー、 3,4,5 が警告、それ以上が普通
        if (warningLevel > 6 ) { MaterialTheme.colorScheme.primary }
        else if (warningLevel > 2 ) { AirA01dTheme.customColors.warning }
        else { MaterialTheme.colorScheme.error }

    // ----- ボタンの表示
    TextButton(
        onClick = { },
        modifier = modifier
            .height(48.dp)
            .widthIn(min = 48.dp, max = 106.dp)
    ) {
        Text(
            text = informationText,
            style = TextStyle(
                textDecoration = TextDecoration.None,
                fontWeight = fontWeight,
                color = textColor
            )
        )
    }
}
