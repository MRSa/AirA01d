package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel

@Composable
fun MirrorImage(viewModel: LiveviewViewModel, modifier: Modifier = Modifier)
{
    // ----- ステータスを監視する
    val isMirrorMode = viewModel.isMirrorMode.observeAsState()

    // ----- ステータスに合わせてアイコンをと色を決める
    val (iconId, iconColor) = when (isMirrorMode.value) {
        true ->
            // ----- 鏡像表示モード
            R.drawable.outline_flip_camera_ios_24 to MaterialTheme.colorScheme.onTertiary
        else ->
            // ----- 通常表示モード
            R.drawable.outline_photo_camera_24 to MaterialTheme.colorScheme.primary
    }

    // ----- ボタンの表示
    IconButton(
        onClick = {
            if (isMirrorMode.value == false)
            {
                // ----- 鏡像表示モードに切り替える
                viewModel.setMirrorMode(true)
            }
            else
            {
                // ----- 通常表示モードに切り替える
                viewModel.setMirrorMode(false)
            }
        },
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = "mirror image",
            tint = iconColor
        )
    }
}
