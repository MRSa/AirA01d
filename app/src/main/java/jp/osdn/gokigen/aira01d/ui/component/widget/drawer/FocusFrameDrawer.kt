package jp.osdn.gokigen.aira01d.ui.component.widget.drawer

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel

/*
@Composable
fun FocusFrameDrawer(canvasSize: Size, focusFrameStatus: IAutoFocusFrameDisplay.FocusFrameStatus?, focusFrameRectangle: RectF?, modifier: Modifier = Modifier)
{
    if ((focusFrameStatus == null)||(focusFrameRectangle == null))
    {
        // ----- フォーカスの状態が取れない場合は何もしない
        return
    }

    // 描画する色
    val focusColor = when (focusFrameStatus) {
        Running -> MaterialTheme.colorScheme.secondary
        Focused -> Color.Green
        Failed -> MaterialTheme.colorScheme.error
        Errored -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    // 描画する線の太さ
    val strokeWidth = with(LocalDensity.current) { 3.dp.toPx() }
/*
    Canvas(modifier = modifier)
    {
        // ----- 正規化された座標を現在のCanvasサイズにスケール
        val left = focusFrameRectangle.left * size.width
        val top = focusFrameRectangle.top * size.height
        val right = focusFrameRectangle.right * size.width
        val bottom = focusFrameRectangle.bottom * size.height

        // ----- 矩形のサイズを計算
        val rectSize = Size(
            width = (right - left),
            height = (bottom - top)
        )

        // ----- 描画
        drawRect(
            color = focusColor,
            topLeft = Offset(left, top),
            size = rectSize,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
*/
    Canvas(modifier = modifier.fillMaxSize()) {
        // 座標の変換
        val left = focusFrameRectangle.left * size.width
        val top = focusFrameRectangle.top * size.height
        val right = focusFrameRectangle.right * size.width
        val bottom = focusFrameRectangle.bottom * size.height

        // 線の設定
        val strokeWidthPx = 6.dp.toPx() // 太めの設定
        val cornerLength = 20.dp.toPx() // L字の各辺の長さ

        val bracketPath = Path().apply {
            // --- 左上角 ---
            moveTo(left, top + cornerLength)
            lineTo(left, top)
            lineTo(left + cornerLength, top)

            // --- 右上角 ---
            moveTo(right - cornerLength, top)
            lineTo(right, top)
            lineTo(right, top + cornerLength)

            // --- 右下角 ---
            moveTo(right, bottom - cornerLength)
            lineTo(right, bottom)
            lineTo(right - cornerLength, bottom)

            // --- 左下角 ---
            moveTo(left + cornerLength, bottom)
            lineTo(left, bottom)
            lineTo(left, bottom - cornerLength)
        }

        drawPath(
            path = bracketPath,
            color = focusColor,
            style = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
*/

/**
 * カメラのフォーカス枠を描画するComposable
 * 四隅のブラケット、センタードット、視認性向上のための外枠（影）を描画します。
 */
@Composable
fun FocusFrameDrawer(
    imageSize: Size,
    focusFrameStatus: LiveviewViewModel.FocusFrameStatus?,
    focusFrameRectangle: RectF?,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 4.dp,
    bracketLength: Dp = 16.dp,
    centerDotRadius: Dp = 2.dp
) {
    // ----- 必要な情報がなければ描画しない
    if ((focusFrameStatus == null) || (focusFrameRectangle == null)) {
        return
    }

    // ----- 描画する色の決定
    val focusColor = when (focusFrameStatus) {
        LiveviewViewModel.FocusFrameStatus.Running -> MaterialTheme.colorScheme.secondary
        LiveviewViewModel.FocusFrameStatus.Focused -> Color.Green
        LiveviewViewModel.FocusFrameStatus.Failed -> MaterialTheme.colorScheme.error
        LiveviewViewModel.FocusFrameStatus.Errored -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    // ----- 視認性向上のための外枠（影）の色
    val shadowColor = Color.Black.copy(alpha = 0.5f)

    // ----- DpをPxに変換
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidth.toPx() }
    val bracketLengthPx = with(density) { bracketLength.toPx() }
    val centerDotRadiusPx = with(density) { centerDotRadius.toPx() }

    // 外枠（影）の太さ。メインの線より少し太くする
    val shadowStrokeWidthPx = strokeWidthPx + with(density) { 1.5.dp.toPx() }

    // 5. 描画領域全体の Modifier
    val finalModifier = modifier.fillMaxSize()

    Canvas(modifier = finalModifier) {

        // ContentScale.Fit で描画された際の実サイズを計算
        val canvasSize = size
        val scale = minOf(
            canvasSize.width / imageSize.width,
            canvasSize.height / imageSize.height
        )
        val drawWidth = imageSize.width * scale
        val drawHeight = imageSize.height * scale

        // 画像が中央に配置されるためのオフセット(余白)を計算
        val offsetX = (canvasSize.width - drawWidth) / 2f
        val offsetY = (canvasSize.height - drawHeight) / 2f

        // ----- 正規化座標 (0.0f ~ 1.0f) を ピクセル座標へ変換し、オフセットを加える
        val left = (focusFrameRectangle.left * drawWidth) + offsetX
        val top = (focusFrameRectangle.top * drawHeight) + offsetY
        val right = (focusFrameRectangle.right * drawWidth) + offsetX
        val bottom = (focusFrameRectangle.bottom * drawHeight) + offsetY

        // ----- 矩形の幅と高さ
        val rectW = right - left
        val rectH = bottom - top

        // ----- 長さが矩形サイズを超えないように調整（小さな枠の場合）
        val maxLen = minOf(rectW, rectH) / 2f
        val actualBracketLen = if (bracketLengthPx > maxLen) maxLen else bracketLengthPx

        // ----- Pathの生成 (四隅のL字型)
        // 座標やサイズが変わったときだけ再生成する
        val bracketPath = Path().apply {
            // --- 左上角 ---
            moveTo(left, top + actualBracketLen)
            lineTo(left, top)
            lineTo(left + actualBracketLen, top)

            // --- 右上角 ---
            moveTo(right - actualBracketLen, top)
            lineTo(right, top)
            lineTo(right, top + actualBracketLen)

            // --- 右下角 ---
            moveTo(right, bottom - actualBracketLen)
            lineTo(right, bottom)
            lineTo(right - actualBracketLen, bottom)

            // --- 左下角 ---
            moveTo(left + actualBracketLen, bottom)
            lineTo(left, bottom)
            lineTo(left, bottom - actualBracketLen)
        }

        // ===== 描画処理 =====

        // ----- 外枠（影）の描画
        // 先に少し太い黒（半透明）を描画することで、背景が明るくても枠が見えるようにする
        drawPath(
            path = bracketPath,
            color = shadowColor,
            style = Stroke(
                width = shadowStrokeWidthPx, // 影用の太さ
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // ----- メインのフォーカス枠の描画
        drawPath(
            path = bracketPath,
            color = focusColor,
            style = Stroke(
                width = strokeWidthPx,      // メインの太さ
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // ----- センタードットの描画
        val centerX = left + (rectW / 2f)
        val centerY = top + (rectH / 2f)

        // ----- ドットにも外枠（影）をつける
        drawCircle(
            color = shadowColor,
            radius = centerDotRadiusPx + with(density) { 1.dp.toPx() },
            center = Offset(centerX, centerY)
        )
        // ----- メインのドット
        drawCircle(
            color = focusColor,
            radius = centerDotRadiusPx,
            center = Offset(centerX, centerY)
        )
    }
}
