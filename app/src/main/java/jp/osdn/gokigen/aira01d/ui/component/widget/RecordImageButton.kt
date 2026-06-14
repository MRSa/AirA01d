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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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

@Composable
fun RecordImageButton(
    navController: NavHostController,
    liveviewViewModel: LiveviewViewModel,
    viewModel: CameraStatusViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val bitmap by liveviewViewModel.lastCapturedImage.collectAsStateWithLifecycle()
    val isLvActivated = liveviewViewModel.isLvActivated.observeAsState()

    val imageBitmap = bitmap?.asImageBitmap()
    val iconId = R.drawable.outline_image_24
    val iconColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .size(96.dp)
            .clip(CircleShape) // 独自のリップルエフェクトの形を定義
            .combinedClickable(
                onClick = {
                    if (isLvActivated.value == true)
                    {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        // 通常クリックの処理  (撮影画像一覧画面へ遷移する)
                        navController.navigate("ContentListScreen") {
                            // ボタン連打による画面の重複スタックを防止
                            launchSingleTop = true
                        }
                    }
                },
                onLongClick = {
                    if (isLvActivated.value == true)
                    {
                        // ----- 長押し : ボタンに表示するために撮影した画像を取得する
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        liveviewViewModel.getLastRecordImage()
                    }
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
