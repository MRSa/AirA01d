package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel


@Composable
fun RecordImageButton(navController: NavHostController, viewModel: CameraStatusViewModel, modifier: Modifier = Modifier)
{
    val haptic = LocalHapticFeedback.current

    // ----- ステータスに合わせてアイコンをと色を決める
    val iconId = R.drawable.outline_image_24
    val iconColor = MaterialTheme.colorScheme.primary

    // ----- ボタンの表示
    IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        },
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = "Record image",
            tint = iconColor,
            modifier = Modifier.size(46.dp)
        )
    }

}
