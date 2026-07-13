package jp.osdn.gokigen.aira01d.ui.component.screen.playback

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import coil3.SingletonImageLoader
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.ICameraFileInfo
import jp.osdn.gokigen.aira01d.AppScope
import jp.osdn.gokigen.aira01d.ui.component.screen.preference.ReturnToMainScreenRow
import jp.osdn.gokigen.aira01d.ui.model.ContentListViewModel
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.component.widget.playback.FilterChipsRow
import jp.osdn.gokigen.aira01d.ui.component.widget.playback.omds.OmdsColumnView
import jp.osdn.gokigen.aira01d.ui.component.widget.playback.omds.OmdsScreennailPagerOverlay
import jp.osdn.gokigen.aira01d.ui.component.widget.playback.omds.OmdsVerticalGridView
import jp.osdn.gokigen.aira01d.ui.model.ContentListViewModel.GetImageSize
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

    // 複数選択用の状態管理
    var isSelectMode by rememberSaveable { mutableStateOf(false) }
    var selectedFiles by remember { mutableStateOf(setOf<ICameraFileInfo.ImageFileInfo>()) }

    // 一括ダウンロード時のダウンロードサイズ選択
    var showSizeSelector by rememberSaveable { mutableStateOf(false) }
    var sizeOptions by remember { mutableStateOf<List<GetImageSize>>(emptyList()) }
    var selectedSize by remember { mutableStateOf(GetImageSize.ORIGINAL) }

    // 選択状態を解除するヘルパー
    fun exitSelectMode() {
        isSelectMode = false
        selectedFiles = emptySet()
    }

    // --- 一括ダウンロード実行処理
    //val scope = rememberCoroutineScope()
    val downloadMessageStartHead = stringResource(R.string.start_bulk_downloading_head)
    val downloadMessageStartFoot = stringResource(R.string.start_bulk_downloading_foot)

    // 画像一括ダウンロードが指定された
    fun handleBulkDownload() {
        // 選択したファイルがなければ何もしない
        if (selectedFiles.isEmpty()) return

        showSizeSelector = true // サイズ選択ダイアログを表示（するだけ）
    }

    // 画像一括ダウンロードの開始
    fun startBulkDownload(imageSize: GetImageSize)
    {
        showSizeSelector = false // ダイアログを閉じる

        // ダウンロード開始の表示
        Toast.makeText(context, "$downloadMessageStartHead ${selectedFiles.size} $downloadMessageStartFoot", Toast.LENGTH_SHORT).show()

        // 提案いただいた引数の型（imageSize）に合わせて呼び出し
        viewModel.downloadMultipleFiles(files = selectedFiles.toList(), imageSize = imageSize , context = context)

        // 画像選択モードから抜ける
        exitSelectMode()
    }

    // --- 全選択・全解除の（トグル）処理
    val isAllSelected = filteredFileList.isNotEmpty() && selectedFiles.size == filteredFileList.size
    fun toggleSelectAll() {
        selectedFiles = if (isAllSelected) {
            // すでに全選択されているなら、すべて解除
            emptySet()
        } else {
            // そうでなければ、現在表示されているファイルをすべてセットに投入
            filteredFileList.toSet()
        }
    }

    // --- バックボタンで選択モードを抜けられるようにする
    BackHandler(enabled = isSelectMode) {
        exitSelectMode()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // ----- 画像を１枚表示している時には、topBarは表示しない
            if (selectedIndex == null) {
                // ----- 選択モードと通常モードでTopBarを切り替える
                if (isSelectMode)
                {
                    // ----- 選択モード時の TopBar
                    Row(
                        modifier = modifier.safeDrawingPadding().fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { exitSelectMode() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Cancel")
                        }
                        Text(
                            text = "${selectedFiles.size} ${stringResource(R.string.selected_count)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // --- 全選択 / 全解除 ボタン (アイコン付き）
                            TextButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    toggleSelectAll()
                                },
                                enabled = filteredFileList.isNotEmpty()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isAllSelected) {
                                        // 全解除時のアイコン
                                        Icon(
                                            imageVector = Icons.Default.Deselect,
                                            contentDescription = stringResource(R.string.deselect_all)
                                        )
                                        Text(text = stringResource(R.string.deselect_all))
                                    } else {
                                        // 全選択時のアイコン
                                        Icon(
                                            imageVector = Icons.Default.SelectAll,
                                            contentDescription = stringResource(R.string.select_all)
                                        )
                                        Text(text = stringResource(R.string.select_all))
                                    }
                                }
                            }

                            // 一括ダウンロードボタン
                            IconButton(
                                onClick = { handleBulkDownload() },
                                enabled = selectedFiles.isNotEmpty()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Bulk Download",
                                    tint = if (selectedFiles.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                } else {
                    // ----- 通常モード時のTopBar
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
                            if (runMode.value != "play") {
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
                                    displayMode =
                                        if (displayMode == ContentListViewModel.DisplayMode.Grid) {
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
                            val isFilterActive =
                                extensionFilter != ContentListViewModel.ExtensionFilter.ALL ||
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
                                    tint = if ((isFilterExpanded) || (isFilterActive)) {
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
                            selectedFiles = selectedFiles,
                            isSelectMode = isSelectMode,
                            modifier = Modifier.fillMaxSize().padding(innerPadding),
                            onItemClick = { index ->
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                val file = filteredFileList[index]
                                if (isSelectMode) {
                                    // --- 選択モード時は選択/非選択のトグル処理
                                    selectedFiles = if (selectedFiles.contains(file)) {
                                        selectedFiles - file
                                    } else {
                                        selectedFiles + file
                                    }
                                } else {
                                    selectedIndex = index // 通常時は詳細表示
                                }
                            },
                            onItemLongClick = { index ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (!isSelectMode) {
                                    // --- 選択モードに切り替え
                                    isSelectMode = true
                                    selectedFiles = setOf(filteredFileList[index])
                                }
                                else
                                {
                                    // --- 選択モード時、長押し操作でも選択/非選択が可能に
                                    val file = filteredFileList[index]
                                    selectedFiles = if (selectedFiles.contains(file)) {
                                        selectedFiles - file
                                    } else {
                                        selectedFiles + file
                                    }
                                }
                            }
                        )
                    }
                    // ----- リスト表示
                    ContentListViewModel.DisplayMode.List -> {
                        OmdsColumnView(
                            fileList = filteredFileList,
                            selectedFiles = selectedFiles,
                            isSelectMode = isSelectMode,
                            modifier = Modifier.fillMaxSize().padding(innerPadding),
                            onItemClick = { index ->
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                val file = filteredFileList[index]
                                if (isSelectMode) {
                                    selectedFiles = if (selectedFiles.contains(file)) {
                                        selectedFiles - file
                                    } else {
                                        selectedFiles + file
                                    }
                                } else {
                                    selectedIndex = index
                                }
                            },
                            onItemLongClick = { index ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (!isSelectMode) {
                                    isSelectMode = true
                                    selectedFiles = setOf(filteredFileList[index])
                                }
                                else
                                {
                                    // --- 選択モード時、長押し操作でも選択/非選択が可能に
                                    val file = filteredFileList[index]
                                    selectedFiles = if (selectedFiles.contains(file)) {
                                        selectedFiles - file
                                    } else {
                                        selectedFiles + file
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // --- Screennail画像 左右スワイプ表示部分 (オーバーレイ) ---
            selectedIndex?.let { index ->
                OmdsScreennailPagerOverlay(
                    viewModel = viewModel,
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

    // --- 画像サイズ選択ダイアログ
    if (showSizeSelector)
    {
        // プロトコル（OPC or OMDS）に応じて画像サイズの選択肢を切り替える
        sizeOptions = if (cameraProtocol.value == ICameraConnectionStatus.CameraProtocol.OPC)
        {
            // --- OPC機
            listOf(
                GetImageSize.ORIGINAL,
                GetImageSize.WIDTH_640_PX,
                GetImageSize.WIDTH_1024_PX,
                GetImageSize.WIDTH_1280_PX,
                GetImageSize.WIDTH_1600_PX,
                GetImageSize.WIDTH_1920_PX,
                GetImageSize.WIDTH_2048_PX,
                GetImageSize.WIDTH_2560_PX,
            )
        }
        else {
            // --- OMDS機
            listOf(
                GetImageSize.ORIGINAL,
                GetImageSize.WIDTH_1024_PX,
                GetImageSize.WIDTH_1600_PX,
                GetImageSize.WIDTH_1920_PX,
                GetImageSize.WIDTH_2048_PX,
            )
        }

        // デフォルトの選択肢はオリジナルにする
        selectedSize = GetImageSize.ORIGINAL

        AlertDialog(
            onDismissRequest = { showSizeSelector = false },
            title = { Text(text = stringResource(R.string.title_start_bulk_download)) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    sizeOptions.forEach { size ->
                        val isSelected = (size == selectedSize)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // ここではダイアログを閉じず、選択状態の変更のみ
                                    selectedSize = size
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text =
                                    when (size)
                                    {
                                        GetImageSize.WIDTH_640_PX -> stringResource(R.string.image_size_640)
                                        GetImageSize.WIDTH_1024_PX -> stringResource(R.string.image_size_1024)
                                        GetImageSize.WIDTH_1280_PX -> stringResource(R.string.image_size_1280)
                                        GetImageSize.WIDTH_1600_PX -> stringResource(R.string.image_size_1600)
                                        GetImageSize.WIDTH_1920_PX -> stringResource(R.string.image_size_1920)
                                        GetImageSize.WIDTH_2048_PX -> stringResource(R.string.image_size_2048)
                                        GetImageSize.WIDTH_2560_PX -> stringResource(R.string.image_size_2560)
                                        GetImageSize.ORIGINAL -> stringResource(R.string.image_size_original)
                                    },
                                style = if (size == selectedSize) {
                                    // ---- 選択しているアイテムを太字にする
                                    MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                } else {
                                    MaterialTheme.typography.bodyLarge
                                },
                                color = if (size == selectedSize) {
                                    // ---- 選択しているアイテムの色を変える
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showSizeSelector = false
                    startBulkDownload(selectedSize)
                }) {
                    Text(stringResource(R.string.button_ok_start))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSizeSelector = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        )
    }

    // --- ダウンロード中の操作ブロック用ダイアログ
    if (viewModel.isDownloading) {
        AlertDialog(
            // 外側をタップされても閉じないように空にする（重要）
            onDismissRequest = { },
            title = {
                Text(
                    text = viewModel.downloadFileName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = viewModel.downloadStatusText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 進捗バー
                    LinearProgressIndicator(
                        progress = { viewModel.downloadProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // パーセンテージ表示
                    Text(
                        text = "${(viewModel.downloadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            // ボタンを両方空にすることで、ユーザーが自発的に閉じられない「完全なロック状態」を作ります
            confirmButton = {},
            dismissButton = {},
            // Androidの物理バックキーを押されても閉じないようにガード
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        )
    }
}
