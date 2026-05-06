package jp.osdn.gokigen.aira01d.ui.component.widget.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel

@Composable
fun LiveviewWidget(viewModel: LiveviewViewModel, modifier: Modifier = Modifier)
{
    // ViewModel の状態を監視
    val bitmap by viewModel.liveViewBitmap.collectAsStateWithLifecycle()
    val isGridOn = viewModel.isGridOn.observeAsState()
    val isShowFocusFrame = viewModel.isShowFocusFrame.observeAsState()
    val focusFrameStatus = viewModel.focusFrameStatus.observeAsState()
    val focusFrameRectangle = viewModel.focusFrameRectangle.observeAsState()

    // コンポーネントのサイズを保持する変数
    var layoutSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .background(Color.Black)
            .onGloballyPositioned { coordinates ->
                layoutSize = coordinates.size
            }
            .pointerInput(Unit) {
/*
                detectTapGestures { offset ->

                    // タッチされたときの位置を取得
                    val width = layoutSize.width.toFloat()
                    val height = layoutSize.height.toFloat()
                    if (width > 0.0f && height > 0.0f)
                    {
                        // 0.0f ～ 1.0f に正規化し、タッチ位置をviewModelに通知する
                        val posX = (offset.x / width).coerceIn(0f, 1f)
                        val posY = (offset.y / height).coerceIn(0f, 1f)
                        viewModel.onTouchPosition(posX, posY)
                    }
                }
*/
                detectTapGestures { offset ->
                    //  --- bitmapが変わったら再計算が必要なのでkeyに指定
                    val btm = bitmap ?: return@detectTapGestures

                    val canvasWidth = layoutSize.width.toFloat()
                    val canvasHeight = layoutSize.height.toFloat()
                    if (canvasWidth <= 0.0f || canvasHeight <= 0.0f) return@detectTapGestures

                    val imgWidth = btm.width.toFloat()
                    val imgHeight = btm.height.toFloat()

                    // ----- ContentScale.Fit 時のスケールを計算
                    val scale = minOf(canvasWidth / imgWidth, canvasHeight / imgHeight)

                    // ----- 実際に表示されている画像のサイズを計算
                    val drawWidth = imgWidth * scale
                    val drawHeight = imgHeight * scale

                    // ----- 画像の開始位置（オフセット）を計算
                    val offsetX = (canvasWidth - drawWidth) / 2f
                    val offsetY = (canvasHeight - drawHeight) / 2f

                    // ----- タップ位置を画像内の相対座標に変換
                    //  (画像の範囲外（黒帯）をタップした場合は無視する)
                    val relativeX = (offset.x - offsetX) / drawWidth
                    val relativeY = (offset.y - offsetY) / drawHeight

                    // 0.0 〜 1.0 の範囲内であれば ViewModel に通知
                    if (relativeX in 0f..1f && relativeY in 0f..1f) {
                        viewModel.onTouchPosition(relativeX, relativeY)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let { btm ->
            val imageBitmap = btm.asImageBitmap()

            // ---- 画像とCanvasを重ねるためのBoxを用意
            Box(contentAlignment = Alignment.Center) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Live View Bitmap",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(1.dp),
                    contentScale = ContentScale.Fit
                )

                // ----- フォーカス表示が必要な場合は表示を行う
                if ((isShowFocusFrame.value == true)&&(focusFrameRectangle.value != null))
                {
                    FocusFrameDrawer(
                        imageSize = Size(imageBitmap.width.toFloat(), imageBitmap.height.toFloat()),
                        focusFrameStatus.value,
                        focusFrameRectangle.value,
                        modifier = Modifier.matchParentSize().padding(1.dp)
                    )
                }

                // -----Grid Onの時にGridを表示する
                if (isGridOn.value == true)
                {
                    GridDrawer(
                        imageSize = Size(imageBitmap.width.toFloat(), imageBitmap.height.toFloat()),
                        modifier = Modifier.matchParentSize().padding(1.dp)
                    )
                }
            }
        }
    }
}
