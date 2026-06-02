package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel

/*
@Composable
fun RecordImageButton(navController: NavHostController, liveviewViewModel: LiveviewViewModel, viewModel: CameraStatusViewModel, modifier: Modifier = Modifier)
{
    val haptic = LocalHapticFeedback.current

    val bitmap by liveviewViewModel.lastCapturedImage.collectAsStateWithLifecycle()

    // ----- ステータスに合わせてアイコンをと色を決める
    val imageBitmap = bitmap?.asImageBitmap()
    val iconId = R.drawable.outline_image_24
    val iconColor = MaterialTheme.colorScheme.primary

    // ----- ボタンの表示
    IconButton(
        onClick = {
            // ----- 現在未サポート
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        },
        enabled = false,
        modifier = modifier.size(96.dp)
    ) {
        if (imageBitmap != null) {
            // --- 撮影画像を表示する ---
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp) // 影をつける
            ) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Captured image",
                    modifier = Modifier.size(72.dp),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            // --- 画像がないときは Iconを使用 ---
            Icon(
                painter = painterResource(iconId),
                contentDescription = "Record image",
                tint = iconColor,
                modifier = Modifier.size(60.dp)
            )
        }
    }
}
*/

@Composable
fun RecordImageButton(
    navController: NavHostController,
    liveviewViewModel: LiveviewViewModel,
    viewModel: CameraStatusViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val bitmap by liveviewViewModel.lastCapturedImage.collectAsStateWithLifecycle()

    val imageBitmap = bitmap?.asImageBitmap()
    val iconId = R.drawable.outline_image_24
    val iconColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .size(96.dp)
            .clip(CircleShape) // 独自のリップルエフェクトの形を定義
            .combinedClickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress) // 例
                    // 通常クリックの処理  (撮影一覧画面へ遷移する)
                },
                onLongClick = {
                    // ----- 長押し :
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    liveviewViewModel.getLadtRecordImage()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Captured image",
                    modifier = Modifier.size(72.dp),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            Icon(
                painter = painterResource(iconId),
                contentDescription = "Record image",
                tint = iconColor,
                modifier = Modifier.size(60.dp)
            )
        }
    }
}
