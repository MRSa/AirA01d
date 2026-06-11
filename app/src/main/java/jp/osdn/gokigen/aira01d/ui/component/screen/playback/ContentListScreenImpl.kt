package jp.osdn.gokigen.aira01d.ui.component.screen.playback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.SingletonImageLoader
import jp.osdn.gokigen.aira01d.AppScope
import jp.osdn.gokigen.aira01d.ui.component.screen.preference.ReturnToMainScreenRow
import jp.osdn.gokigen.aira01d.ui.model.ContentListViewModel
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.component.widget.playback.OmdsColumnView
import jp.osdn.gokigen.aira01d.ui.component.widget.playback.OmdsScreennailPagerOverlay
import jp.osdn.gokigen.aira01d.ui.component.widget.playback.OmdsVerticalGridView
import kotlinx.coroutines.launch

@Composable
fun ContentListScreenImpl(
    navController: NavHostController,
    viewModel: ContentListViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val runMode = viewModel.runMode.observeAsState()
    val contentStatus =viewModel.contentStatus.observeAsState()
    val cameraProtocol = viewModel.cameraProtocol.observeAsState()
    val fileList = viewModel.fileList

    DisposableEffect(Unit) {
        onDispose {
            // 画面のスコープではなく、アプリ起動中ずっと生きている AppScope.ioScope を使用する
            AppScope.ioScope.launch {
                val imageLoader = SingletonImageLoader.get(context)

                // キャッシュをクリア（IOスレッドで実行されます）
                imageLoader.diskCache?.clear()    // ディスクキャッシュ
                // imageLoader.memoryCache?.clear()  // メモリキャッシュ
            }
        }
    }

    // どの画像が選択されているかのインデックス
    var selectedIndex by rememberSaveable { mutableStateOf<Int?>(null) }

    // 現在の表示モード（グリッド表示 or リスト表示）
    var displayMode by rememberSaveable { mutableStateOf(ContentListViewModel.DisplayMode.Grid) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // ----- 画像を１枚表示している時には、topBarは表示しない
            if (selectedIndex == null) {
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

                        // --- 件数の表示
                        Text(
                            text = "${stringResource(R.string.content_count)}${fileList.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 4.dp)
                        )

                        // --- グリッド/リスト切り替えボタン
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                // 表示モードを反転させる
                                displayMode = if (displayMode == ContentListViewModel.DisplayMode.Grid) {
                                    ContentListViewModel.DisplayMode.List
                                } else {
                                    ContentListViewModel.DisplayMode.Grid
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                // 現在のモードと「反対」のアイコンを表示して、押したらどうなるかを明示
                                imageVector = if (displayMode == ContentListViewModel.DisplayMode.Grid) {
                                    Icons.AutoMirrored.Filled.List // グリッド時は「リストに変える」アイコン
                                } else {
                                    Icons.Default.GridView       // リスト時は「グリッドに変える」アイコン
                                },
                                contentDescription = "Toggle display mode",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

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
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (fileList.isEmpty()) {
                val emptyMessage = when (contentStatus.value) {
                    ContentListViewModel.ContentLoadingStatus.Uninitialized -> stringResource(R.string.content_uninitialized)
                    ContentListViewModel.ContentLoadingStatus.ChangingMode -> stringResource(R.string.mode_changing)
                    ContentListViewModel.ContentLoadingStatus.Fetching -> stringResource(R.string.content_fetching)
                    else -> stringResource(R.string.content_not_found)
                }
                // -----
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                when (displayMode) {
                    // ----- グリッド表示
                    ContentListViewModel.DisplayMode.Grid -> {
                        OmdsVerticalGridView(
                            fileList = fileList,
                            modifier = Modifier.fillMaxSize().padding(innerPadding),
                            onItemClick = { index ->
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                selectedIndex = index // タップされたインデックスを保存
                            }
                        )
                    }
                    // ----- リスト表示
                    ContentListViewModel.DisplayMode.List -> {
                        OmdsColumnView(
                            fileList = fileList,
                            modifier = Modifier.fillMaxSize().padding(innerPadding),
                            onItemClick = { index ->
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                selectedIndex = index // タップされたインデックスを保存
                            }
                        )
                    }
                }
            }

            // --- Screennail画像 左右スワイプ表示部分 (オーバーレイ) ---
            selectedIndex?.let { index ->
                OmdsScreennailPagerOverlay(
                    fileList = fileList,
                    initialIndex = index,
                    cameraProtocol = cameraProtocol.value,
                    onClose = {
                        // 閉じたら 選択中画像のインデックスを null に戻す
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        selectedIndex = null
                    }
                )
            }
        }
    }
}
