package jp.osdn.gokigen.aira01d.ui.component.screen.playback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.ICameraFileInfo
import jp.osdn.gokigen.aira01d.ui.component.screen.preference.ReturnToMainScreenRow
import jp.osdn.gokigen.aira01d.ui.model.ContentListViewModel
import jp.osdn.gokigen.aira01d.R

@Composable
fun ContentListScreenPortrait(
    navController: NavHostController,
    viewModel: ContentListViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val runMode = viewModel.runMode.observeAsState()
    val fileList = viewModel.fileList

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = modifier.safeDrawingPadding().padding(1.dp)
            )
            {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 4.dp), // 右端に少し余白を作る
                    horizontalArrangement = Arrangement.SpaceBetween, // 左右の両端に分ける
                    verticalAlignment = Alignment.CenterVertically // 上下中央揃え
                ) {
                    // 左端： 戻るボタンの行
                    ReturnToMainScreenRow(
                        onBackClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            navController.popBackStack()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // 件数の表示
                    Text(
                        text = "${stringResource(R.string.content_count)}${fileList.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    // --- コンテンツリロードボタン
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            viewModel.getAllContentList()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "reload contents",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    ) { innerPadding ->
        if (fileList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.content_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(fileList) { file ->
                    FileItemCard(file = file)
                }
            }
        }
    }
}

@Composable
fun FileItemCard(file: ICameraFileInfo.ImageFileInfo) {
    Card(
        modifier = Modifier.aspectRatio(1f),
        // カードの背景にSurface（地色）を設定
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(contentAlignment = Alignment.BottomStart) {
            // ----- 代替アイコン
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp), // 少しアイコンを小さくして収まりを良く
                // アイコンをコンテナに対して目立たない中間色に変更
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            // 写真の上にファイル名などを重ねる（Scrim: 遮光効果）
            Text(
                text = file.fileName,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                // 写真が明るくても文字が読めるよう、ここは固定の白（またはonSecondaryContainer）
                color = Color.White, // MaterialTheme.colorScheme.onSecondaryContainer
                modifier = Modifier
                    .fillMaxWidth()
                    // M3のscrim（黒系）を80%から徐々に透明にするグラデーションにする
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.scrim.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
