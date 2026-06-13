package jp.osdn.gokigen.aira01d.ui.component.widget.playback.omds

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.ICameraFileInfo
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.IPlaybackControl
import jp.osdn.gokigen.a01lib.camera.omds.playback.OmdsFileTransfer
import jp.osdn.gokigen.a01lib.camera.utils.storage.MediaStoreStreamSaveHelper
import jp.osdn.gokigen.aira01d.AppSingleton
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.ContentListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OmdsScreennailPagerOverlay(
    viewModel: ContentListViewModel,
    fileList: List<ICameraFileInfo.ImageFileInfo>,
    initialIndex: Int,
    cameraProtocol: ICameraConnectionStatus.CameraProtocol?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val baseUrl = AppSingleton.CAMERA_BASE_URL
    val fileTransfer = remember(baseUrl) { OmdsFileTransfer(executeUrl = baseUrl) }

    // Pagerの状態を初期インデックスで初期化
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { fileList.size }
    )

    val scope = rememberCoroutineScope()

    // --- 日付・時刻のフォーマッタのインスタンス
    val dateFormatter = remember {
        SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    }

    // --- ダウンロードダイアログの状態管理
    var showDownloadDialog by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0.0f) }
    var downloadStatusText by remember { mutableStateOf("") }
    var downloadFileName by remember { mutableStateOf("") }

    // --- サイズ選択ダイアログの状態管理
    var showSizeDialog by remember { mutableStateOf(false) }
    var sizeOptions by remember { mutableStateOf<List<ContentListViewModel.GetImageSize>>(emptyList()) }
    var selectedSize by remember { mutableStateOf(ContentListViewModel.GetImageSize.ORIGINAL) }

    // --- カメラプロトコル情報
    val protocol = viewModel.cameraProtocol.observeAsState()

    // --- ダウンロード処理で使用するメッセージ
    val downloadMessage = stringResource(R.string.now_downloading)
    val storeOKMessage = stringResource(R.string.stored_image_ok)
    val storeNGMessage = stringResource(R.string.stored_image_ng)
    val storeErrorMessage = stringResource(R.string.stored_image_error)

    // --- ダウンロード処理を関数として抽出
    val executeDownload: (ICameraFileInfo.ImageFileInfo, ContentListViewModel.GetImageSize) -> Unit = { file, selectedSize ->
        val storeFileName = createTimestampedFileName(file.fileName)

        showDownloadDialog = true
        downloadProgress = 0.0f
        downloadStatusText = downloadMessage
        downloadFileName = file.fileName

        // --- 選択された画像サイズに応じて、リクエストパスやパラメータを調整する
        val downloadPath = when (selectedSize) {
            ContentListViewModel.GetImageSize.WIDTH_640_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=0640"
            ContentListViewModel.GetImageSize.WIDTH_1024_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=1024"
            ContentListViewModel.GetImageSize.WIDTH_1280_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=1280"
            ContentListViewModel.GetImageSize.WIDTH_1600_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=1600"
            ContentListViewModel.GetImageSize.WIDTH_1920_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=1920"
            ContentListViewModel.GetImageSize.WIDTH_2048_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=2048"
            ContentListViewModel.GetImageSize.WIDTH_2560_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=2560"
            ContentListViewModel.GetImageSize.ORIGINAL -> "${file.directory}/${file.fileName}"
        }

        val streamSaver = MediaStoreStreamSaveHelper(context, storeFileName)
        scope.launch(Dispatchers.IO) {
            val isReady = streamSaver.open()
            if (!isReady) {
                withContext(Dispatchers.Main) {
                    showDownloadDialog = false
                    downloadFileName = ""
                    Toast.makeText(context, storeNGMessage, Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            fileTransfer.downloadContent(
                directory = downloadPath,
                callback = object : IPlaybackControl.IContentTransferCallback {
                    override fun onReceive(readBytes: Int, length: Int, size: Int, data: ByteArray?) {
                        if (data != null && data.isNotEmpty()) {
                            streamSaver.write(data)
                        }
                        if (length > 0) {
                            val pct = readBytes.toFloat() / length.toFloat()
                            downloadProgress = pct
                        }
                    }

                    override fun onCompleted() {
                        streamSaver.close(success = true)
                        scope.launch(Dispatchers.Main) {
                            showDownloadDialog = false
                            downloadFileName = ""
                            Toast.makeText(
                                context,
                                "$storeOKMessage:${file.fileName}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onErrorOccurred(e: Exception?) {
                        streamSaver.close(success = false)
                        scope.launch(Dispatchers.Main) {
                            showDownloadDialog = false
                            downloadFileName = ""
                            Toast.makeText(
                                context,
                                "$storeErrorMessage: ${e?.localizedMessage}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        }
    }

    // 全画面を黒背景のコンテナにする（BackHandlerで戻るボタンにも対応）
    BackHandler(onBack = onClose)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black) // 写真が映えるように黒背景
    ) {
        // ----- 左右スワイプ可能なコンポーネント
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = 16.dp, // ページ間の余白
            userScrollEnabled = !showDownloadDialog && !showSizeDialog // ダイアログ表示中はページ変更を禁止する
        ) { page ->
            val file = fileList[page]
            var retryCount by remember { mutableIntStateOf(0) }

            // スクリーンネイルURL (オープンプラットフォームカメラ仕様書 get_screennail.cgi 参照)
            // 例: http://192.168.0.10/get_screennail.cgi?DIR=/DCIM/100OLYMP/P6230001.JPG
            val screennailBaseUrl = if ((cameraProtocol?: ICameraConnectionStatus.CameraProtocol.OPC) == ICameraConnectionStatus.CameraProtocol.OPC)
            {
                "$baseUrl/get_screennail.cgi?DIR=${file.directory}/${file.fileName}"
            }
            else
            {
                if (file.fileName.endsWith(".MOV"))
                {
                    // --- .MOV のときは screennail.cgi じゃないととれなかった...
                    "$baseUrl/get_screennail.cgi?DIR=${file.directory}/${file.fileName}"
                }
                else {
                    // --- get_screennail.cgi が動かなかったので、、、get_resizeimg.cgi を使う
                    "$baseUrl/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=1024"
                }
            }
            // ----- 画像再取得時にはURLに細工を入れてみる
            val screennailUrl = if (retryCount > 0) { "$screennailBaseUrl&retry=$retryCount" } else { screennailBaseUrl }

            val customHeaders = NetworkHeaders.Builder()
                .set("User-Agent", "OlympusCameraKit")
                .set("X-Protocol", "OlympusCameraKit")
                .build()

            val imageRequest = ImageRequest.Builder(context)
                .data(screennailUrl)
                .httpHeaders(customHeaders)
                .crossfade(true)
                .build()

            // 1枚の画像表示
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = imageRequest,
                    contentDescription = "Screennail for ${file.fileName}",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    },
                    error = {
                        // ----- 画像取得エラーの表示
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                // タップされたらカウントアップしてリクエストを再トリガー
                                retryCount++
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            },
                            contentAlignment = Alignment.Center) {
                            Text("${stringResource(R.string.get_image_failure)}\n(${stringResource(R.string.tap_to_retry)})", color = Color.White)
                        }
                    }
                )
            }
        }

        // --- 上部のヘッダーコンテナ（操作アイコン ＋ ファイル情報）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                ) // 上から下へ消えていくグラデーションで視認性を確保
                .safeDrawingPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            //  --- 操作アイコン行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- 画像のダウンロードボタン
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        val currentFile = fileList.getOrNull(pagerState.currentPage)
                        if (currentFile != null && !showDownloadDialog && !showSizeDialog) {
                            val lowerName = currentFile.fileName.lowercase()

                            // 拡張子に応じてダイアログの選択肢を切り替える
                            sizeOptions = if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
                                if (protocol.value == ICameraConnectionStatus.CameraProtocol.OPC)
                                {
                                    // --- OPC機
                                    listOf(
                                        ContentListViewModel.GetImageSize.ORIGINAL,
                                        ContentListViewModel.GetImageSize.WIDTH_640_PX,
                                        ContentListViewModel.GetImageSize.WIDTH_1024_PX,
                                        ContentListViewModel.GetImageSize.WIDTH_1280_PX,
                                        ContentListViewModel.GetImageSize.WIDTH_1600_PX,
                                        ContentListViewModel.GetImageSize.WIDTH_1920_PX,
                                        ContentListViewModel.GetImageSize.WIDTH_2048_PX,
                                        ContentListViewModel.GetImageSize.WIDTH_2560_PX,
                                    )
                                }
                                else {
                                    // --- OMDS機
                                    listOf(
                                        ContentListViewModel.GetImageSize.ORIGINAL,
                                        ContentListViewModel.GetImageSize.WIDTH_1024_PX,
                                        ContentListViewModel.GetImageSize.WIDTH_1600_PX,
                                        ContentListViewModel.GetImageSize.WIDTH_1920_PX,
                                        ContentListViewModel.GetImageSize.WIDTH_2048_PX,
                                    )
                                }
                            } else {
                                listOf(ContentListViewModel.GetImageSize.ORIGINAL)
                            }

                            // デフォルトの選択肢はオリジナルにする
                            selectedSize = ContentListViewModel.GetImageSize.ORIGINAL

                            // サイズ選択ダイアログを表示
                            showSizeDialog = true
                        }
                    },
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color.White)
                }

                // --- 画面を閉じるボタン
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White)
                }
            }

            // --- メタデータ行（アイコンのすぐ下に追加）
            val currentFile = fileList.getOrNull(pagerState.currentPage)
            if (currentFile != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // --- ファイル名
                    Text(
                        text = currentFile.fileName,
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // --- 撮影日時
                    val formattedDate = remember(currentFile.dateTime) {
                        dateFormatter.format(currentFile.dateTime)
                    }

                    Text(
                        text = formattedDate,
                        color = Color.LightGray,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                }
            }
        }

        // ---- 下部などのページインジケーター（何枚中何枚目かを表示）
        Text(
            text = "${pagerState.currentPage + 1} / ${fileList.size}",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
                .padding(bottom = 24.dp)
                .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }

    // --- ダウンロード開始ダイアログ（取得サイズ選択ダイアログ）
    if (showSizeDialog)
    {
        val currentFile = fileList.getOrNull(pagerState.currentPage)
        AlertDialog(
            onDismissRequest = { showSizeDialog = false },
            title = { Text(text = "${stringResource(R.string.title_now_downloading)} : ${currentFile?.fileName}") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    sizeOptions.forEach { size ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // ここではダイアログを閉じず、選択状態の変更のみにする
                                    selectedSize = size
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // デフォルトとして指定されている「オリジナル」を太字にするなど視認性を上げる
                            Text(
                                text =
                                    when (size)
                                    {
                                        ContentListViewModel.GetImageSize.WIDTH_640_PX -> stringResource(R.string.image_size_640)
                                        ContentListViewModel.GetImageSize.WIDTH_1024_PX -> stringResource(R.string.image_size_1024)
                                        ContentListViewModel.GetImageSize.WIDTH_1280_PX -> stringResource(R.string.image_size_1280)
                                        ContentListViewModel.GetImageSize.WIDTH_1600_PX -> stringResource(R.string.image_size_1600)
                                        ContentListViewModel.GetImageSize.WIDTH_1920_PX -> stringResource(R.string.image_size_1920)
                                        ContentListViewModel.GetImageSize.WIDTH_2048_PX -> stringResource(R.string.image_size_2048)
                                        ContentListViewModel.GetImageSize.WIDTH_2560_PX -> stringResource(R.string.image_size_2560)
                                        ContentListViewModel.GetImageSize.ORIGINAL -> stringResource(R.string.image_size_original)
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
                    showSizeDialog = false
                    if (currentFile != null) {
                        executeDownload(currentFile, selectedSize)
                    }
                }) {
                    Text(stringResource(R.string.button_ok_start))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSizeDialog = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        )
    }

    if (showDownloadDialog) {
        AlertDialog(
            onDismissRequest = {  }, // ダイアログは閉じない
            title = { Text(text = downloadFileName) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = downloadStatusText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // --- 確定進捗バー (0.0 〜 1.0 を渡す)
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- パーセンテージのテキスト表示
                    Text(
                        text = "${(downloadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            // ボタンは配置せず、完了時に自動で閉じる
            confirmButton = {},
            dismissButton = {}
        )
    }
}

// --- ファイル名に現在のタイムスタンプを付与する関数  例: "R101010.JPG" -> "R101010_20261213123400.JPG"
private fun createTimestampedFileName(originalFileName: String): String {
    val dotIndex = originalFileName.lastIndexOf('.')
    val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())

    return if (dotIndex != -1) {
        // ---拡張子がある場合 (base = R101010, ext = .JPG)
        val baseName = originalFileName.substring(0, dotIndex)
        val extension = originalFileName.substring(dotIndex)
        "${baseName}_$timestamp$extension"
    } else {
        // --- 拡張子がない場合
        "${originalFileName}_$timestamp"
    }
}
