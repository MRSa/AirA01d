package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel

@Composable
fun LiveviewWidget(viewModel: LiveviewViewModel, modifier: Modifier = Modifier)
{
    // ViewModel の状態を監視
    val bitmap by viewModel.liveViewBitmap.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // ViewModelから画像を取得する
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Live View",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}
