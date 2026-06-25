package jp.osdn.gokigen.aira01d.ui.component.widget

import android.os.Build
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel
import jp.osdn.gokigen.aira01d.ui.theme.AirA01dTheme


@Composable
fun LiveviewMagnifyButton(
    viewModel: LiveviewViewModel,
    controlModel: CameraStatusViewModel,
    modifier: Modifier = Modifier
)
{
    val haptic = LocalHapticFeedback.current

    // ----- ステータスを監視する
    val isLvActivated = viewModel.isLvActivated.observeAsState()
    val lvMagnifySize = controlModel.liveViewMagnifySize.observeAsState()
    val cameraProtocol = controlModel.cameraProtocol.observeAsState()
    val isFocusAssist = viewModel.isFocusAssist.observeAsState()

    // ----- ステータスに合わせてアイコンをと色を決める
    val iconId = when (lvMagnifySize.value)
    {
        5 -> if (isFocusAssist.value == true) { R.drawable.times_5_em } else { R.drawable.times_5 }
        7 -> if (isFocusAssist.value == true) { R.drawable.times_7_em } else { R.drawable.times_7 }
        10 -> if (isFocusAssist.value == true) { R.drawable.times_10_em } else { R.drawable.times_10 }
        14 -> if (isFocusAssist.value == true) { R.drawable.times_14_em } else { R.drawable.times_14 }
        else -> if (isFocusAssist.value == true) { R.drawable.outline_loupe_em } else { R.drawable.outline_loupe_24 }
    }
    val iconColor = when (lvMagnifySize.value)
    {
        5 -> if (isFocusAssist.value == true) { AirA01dTheme.customColors.warning } else { MaterialTheme.colorScheme.tertiary }
        7 -> if (isFocusAssist.value == true) { AirA01dTheme.customColors.warning } else { MaterialTheme.colorScheme.tertiary }
        10 -> if (isFocusAssist.value == true) { AirA01dTheme.customColors.warning } else { MaterialTheme.colorScheme.tertiary }
        14 -> if (isFocusAssist.value == true) { AirA01dTheme.customColors.warning } else { MaterialTheme.colorScheme.tertiary }
        else -> if (isFocusAssist.value == true) { AirA01dTheme.customColors.warning } else { MaterialTheme.colorScheme.primary }
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape) // ---- IconButtonと同様のリップルエフェクト（波紋）の形状を円形に制限
            .combinedClickable(
                onClick = {
                    if ((isLvActivated.value == true)&&(cameraProtocol.value == ICameraConnectionStatus.CameraProtocol.OPC))
                    {
                        // ----- ライブビュー表示 & OPCプロトコルの時、ライブビューの拡大を実行
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        controlModel.changeLiveviewScale()
                    }
                },
                onLongClick = {
                    if ((isLvActivated.value == true)&&(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)) {
                        // 長押し時に触覚フィードバック（ブルッという振動）を与えて、ユーザーに入力を知らせる
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        // ViewModel のフォーカスアシスト状態変更（トグル処理）を呼び出す
                        viewModel.toggleFocusAssist()
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = "liveview zoom",
            tint = iconColor
        )
    }
}
