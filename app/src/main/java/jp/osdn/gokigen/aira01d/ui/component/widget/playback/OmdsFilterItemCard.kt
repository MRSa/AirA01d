package jp.osdn.gokigen.aira01d.ui.component.widget.playback

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.ICameraFileInfo
import jp.osdn.gokigen.aira01d.AppSingleton
import jp.osdn.gokigen.aira01d.R

@Composable
fun OmdsFileItemCard(file: ICameraFileInfo.ImageFileInfo)
{
    val context = LocalContext.current
    val baseUrl = AppSingleton.CAMERA_BASE_URL

    var retryCount by rememberSaveable { mutableIntStateOf(0) }

    // 基本となるサムネイルURL (オープンプラットフォームカメラ仕様書 get_thumbnail.cgi 参照)
    // 例: http://192.168.0.10/get_thumbnail.cgi?DIR=/DCIM/100OLYMP/P6230001.JPG
    val baseThumbnailUrl = "$baseUrl/get_thumbnail.cgi?DIR=${file.directory}/${file.fileName}"

    // retryCount が増えるたびに、URLの末尾が変化するようにします（例: &retry=1, &retry=2 ...）
    val thumbnailUrl = if (retryCount > 0) "$baseThumbnailUrl&retry=$retryCount" else baseThumbnailUrl

    val customHeaders = NetworkHeaders.Builder()
        .set("User-Agent", "OlympusCameraKit")
        .set("X-Protocol", "OlympusCameraKit")
        .build()

    val imageRequest = ImageRequest.Builder(context)
        .data(thumbnailUrl) // 変化するURLを渡す
        .httpHeaders(customHeaders)
        .crossfade(true)
        .apply {
            // タップされてリトライ中（retryCount > 0）なら、古いエラーキャッシュを無視して強制取得
            if (retryCount > 0) {
                memoryCachePolicy(CachePolicy.WRITE_ONLY)
                diskCachePolicy(CachePolicy.WRITE_ONLY)
            }
        }
        .build() // listener は不要になったので削除してシンプルに

    Card(
        modifier = Modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(contentAlignment = Alignment.BottomStart) {

            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = "Thumbnail for ${file.fileName}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                // カウントを増やすことで、URLが変わり、Coilの再取得が即座にトリガーされる
                                retryCount++
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        DefaultPlaceholderIcon()
                        Text(
                            text = stringResource(R.string.tap_to_retry),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
                        )
                    }
                }
            )

            // 写真の上にファイル名を重ねる
            Text(
                text = file.fileName,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
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

@Composable
private fun DefaultPlaceholderIcon() {
    Icon(
        imageVector = Icons.Outlined.Image,
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
}
