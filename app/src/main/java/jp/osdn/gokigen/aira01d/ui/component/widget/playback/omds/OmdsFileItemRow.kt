package jp.osdn.gokigen.aira01d.ui.component.widget.playback.omds

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.ICameraFileInfo
import jp.osdn.gokigen.aira01d.AppSingleton
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun OmdsFileItemRow(
    file: ICameraFileInfo.ImageFileInfo,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val baseUrl = AppSingleton.CAMERA_BASE_URL

    // --- サムネイルの取得・リトライ処理 (Cardと同様のロジック)
    var retryCount by rememberSaveable { mutableIntStateOf(0) }
    val baseThumbnailUrl = "$baseUrl/get_thumbnail.cgi?DIR=${file.directory}/${file.fileName}"
    val thumbnailUrl = if (retryCount > 0) "$baseThumbnailUrl&retry=$retryCount" else baseThumbnailUrl

    val customHeaders = NetworkHeaders.Builder()
        .set("User-Agent", "OlympusCameraKit")
        .set("X-Protocol", "OlympusCameraKit")
        .build()

    val imageRequest = ImageRequest.Builder(context)
        .data(thumbnailUrl)
        .httpHeaders(customHeaders)
        .crossfade(true)
        .apply {
            if (retryCount > 0) {
                memoryCachePolicy(CachePolicy.WRITE_ONLY)
                diskCachePolicy(CachePolicy.WRITE_ONLY)
            }
        }
        .build()

    // --- ファイルサイズのフォーマット
    val formattedSize = remember(file.fileSize) {
        Formatter.formatShortFileSize(context, file.fileSize)
    }

    // --- 日時のフォーマット
    val formattedDate = remember(file.dateTime) {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        sdf.format(file.dateTime)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // --- サムネイル画像エリア ---
            Box(
                modifier = Modifier
                    .size(64.dp) // リストアイテムに適したサイズ
                    .clip(RoundedCornerShape(8.dp)) // 少し角を丸めてモダンに
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = imageRequest,
                    contentDescription = "Thumbnail for ${file.fileName}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp), // 小さめのインジケーター
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { retryCount++ }, // タップでリトライ
                            contentAlignment = Alignment.Center
                        ) {
                            // Row用に少しパディングを抑えたプレースホルダー
                            Icon(
                                imageVector = Icons.Outlined.Image,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // --- メタ情報エリア ---
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // ファイル名 (太字で強調)
                Text(
                    text = file.fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // メタ情報 (時刻とサイズ) の並び
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 時刻
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // ファイルサイズ
                    Text(
                        text = formattedSize,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
