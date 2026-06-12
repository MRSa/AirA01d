package jp.osdn.gokigen.aira01d.ui.component.screen.playback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import jp.osdn.gokigen.aira01d.ui.component.widget.playback.FilterChipsRow
import jp.osdn.gokigen.aira01d.ui.component.widget.playback.omds.OmdsColumnView
import jp.osdn.gokigen.aira01d.ui.component.widget.playback.omds.OmdsScreennailPagerOverlay
import jp.osdn.gokigen.aira01d.ui.component.widget.playback.omds.OmdsVerticalGridView
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
    val rawFileList = viewModel.fileList

    // --- フィルタ条件の開閉状態
    var isFilterExpanded by rememberSaveable { mutableStateOf(false) }

    // ----------------------------------------------------
    // フィルタ条件の状態管理
    // ----------------------------------------------------
    var sortOrder by rememberSaveable { mutableStateOf(ContentListViewModel.SortOrder.NEWEST) }
    var startDate by rememberSaveable { mutableStateOf<Long?>(null) }
    var endDate by rememberSaveable { mutableStateOf<Long?>(null) }
    var extensionFilter by rememberSaveable { mutableStateOf(ContentListViewModel.ExtensionFilter.ALL) }

    val filteredFileList by remember(rawFileList, sortOrder, startDate, endDate, extensionFilter) {
        derivedStateOf {
            var result = rawFileList

            // --- 拡張子フィルタ
            if (extensionFilter != ContentListViewModel.ExtensionFilter.ALL) {
                result = result.filter { extensionFilter.matches(it.fileName) }
            }

            // --- 日付フィルター
            val sDate = startDate
            if (sDate != null) {
                // UTC 0時 を 日本時間（ローカル時間）の 0時に合わせる補正
                // ※ 簡易的には UTCタイムスタンプに時差分（9時間）を引く、またはTimeZoneを考慮して比較します
                val startLocalDate = sDate - java.util.TimeZone.getDefault().getOffset(sDate)
                result = result.filter { it.dateTime.time >= startLocalDate }
            }
            val eDate = endDate
            if (eDate != null) {
                // 終了日も同様にローカル時間に補正し、さらに「その日の終わり（+24時間）」までを含める
                val endLocalDate = eDate - java.util.TimeZone.getDefault().getOffset(eDate)
                val endOfSelectDay = endLocalDate + 24 * 60 * 60 * 1000L
                result = result.filter { it.dateTime.time < endOfSelectDay }
            }

            // --- ソート順
            result = when (sortOrder) {
                ContentListViewModel.SortOrder.NEWEST -> result.sortedByDescending { it.dateTime }
                ContentListViewModel.SortOrder.OLDEST -> result.sortedBy { it.dateTime }
            }
            result
        }
    }

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

                        // --- カメラの動作モードが期待したモードではない場合は、画面表示する
                        if (runMode.value != "play")
                        {
                            Text(
                                text = " ${runMode.value} ",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                            )
                        }

                        // --- 件数の表示
                        Text(
                            text = "${stringResource(R.string.content_count)}${filteredFileList.size}/${rawFileList.size}",
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

                        // --- フィルター条件が設定済かどうか
                        val isFilterActive = extensionFilter != ContentListViewModel.ExtensionFilter.ALL ||
                                startDate != null ||
                                endDate != null

                        // --- フィルター開閉ボタン
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                isFilterExpanded = !isFilterExpanded // 開閉を反転
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                // 開閉状態に応じてアイコンを切り替える
                                imageVector = if (isFilterExpanded) Icons.Default.FilterListOff else Icons.Default.FilterList,
                                contentDescription = "Toggle filter visibility",
                                tint = if ((isFilterExpanded)||(isFilterActive)) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
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

                    AnimatedVisibility(
                        visible = isFilterExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        FilterChipsRow(
                            currentSort = sortOrder,
                            onSortChange = { sortOrder = it },
                            currentExt = extensionFilter,
                            onExtChange = { extensionFilter = it },
                            startDate = startDate,
                            onStartDateChange = { startDate = it },
                            endDate = endDate,
                            onEndDateChange = { endDate = it }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (filteredFileList.isEmpty()) {
                val emptyMessage = if (rawFileList.isNotEmpty() && filteredFileList.isEmpty()) {
                    stringResource(R.string.content_not_matched)
                } else {
                    when (contentStatus.value) {
                        ContentListViewModel.ContentLoadingStatus.Uninitialized -> stringResource(R.string.content_uninitialized)
                        ContentListViewModel.ContentLoadingStatus.ChangingMode -> stringResource(R.string.mode_changing)
                        ContentListViewModel.ContentLoadingStatus.Fetching -> stringResource(R.string.content_fetching)
                        else -> stringResource(R.string.content_not_found)
                    }
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
                            fileList = filteredFileList,
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
                            fileList = filteredFileList,
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
                    fileList = filteredFileList,
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
