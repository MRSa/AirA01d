package jp.osdn.gokigen.aira01d.ui.component.widget.playback

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.SubcomposeAsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.request.crossfade
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.ICameraFileInfo
import jp.osdn.gokigen.aira01d.AppSingleton
import jp.osdn.gokigen.aira01d.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun OmdsScreennailPagerOverlay(
    fileList: List<ICameraFileInfo.ImageFileInfo>,
    initialIndex: Int,
    cameraProtocol: ICameraConnectionStatus.CameraProtocol?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val baseUrl = AppSingleton.CAMERA_BASE_URL
    
    // Pagerの状態を初期インデックスで初期化
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { fileList.size }
    )

    val scope = rememberCoroutineScope()

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
            pageSpacing = 16.dp // ページ間の余白
        ) { page ->
            val file = fileList[page]

            var retryCount by remember { mutableStateOf(0) }

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
            Log.v("SCR-URL", screennailBaseUrl)
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
                    onClick = { /* 既存のダウンロード処理 */ },
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
                        val sdf = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.getDefault())
                        sdf.format(currentFile.dateTime)
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
}

/**
 * 💡 画像をダウンロードして DCIM/AirA01d に MediaStore 経由で保存する関数
 */
private suspend fun downloadAndSaveImage(context: Context, url: String, fileName: String): Boolean = withContext(Dispatchers.IO) {
    try
    {
        // --- Coilで画像を取得 ---
        val imageLoader = ImageLoader(context)
        val request = ImageRequest.Builder(context).data(url).allowHardware(false).build()
        val result = imageLoader.execute(request)
        if (result !is SuccessResult) return@withContext false
        val bitmap = (result.image as? BitmapDrawable)?.bitmap ?: return@withContext false

        val resolver = context.contentResolver

        // ----- 保存処理 -----
        // ----- OSのバージョンによって保存処理を完全に分ける -----
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 (API 29) 以降の保存方法
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/AirA01d") // Q以降のみ有効
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val imageUri = resolver.insert(collection, contentValues) ?: return@withContext false

            resolver.openOutputStream(imageUri).use { out ->
                if (out == null) return@withContext false
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(imageUri, contentValues, null, null)

        } else {
            // ------ Android 7, 8, 9 (API 24〜28) のレガシーな保存方法
            val dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            val targetFolder = File(dcimDir, "AirA01d")
            if (!targetFolder.exists()) targetFolder.mkdirs() // 物理的にフォルダ作成

            val targetFile = File(targetFolder, fileName)
            FileOutputStream(targetFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            // ----- 7,8,9はこれを開かないと他のギャラリーアプリに画像が出てこない
            @Suppress("DEPRECATION")
            MediaScannerConnection.scanFile(context, arrayOf(targetFile.toString()), null, null)
        }

        true
    }
    catch (e: Exception)
    {
        e.printStackTrace()
        false
    }
}