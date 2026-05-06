package jp.osdn.gokigen.aira01d.ui.component.widget.drawer

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GridDrawer(imageSize: Size, modifier: Modifier = Modifier)
{
    Canvas(modifier = modifier)
    {
        // ContentScale.Fit で描画された際の実サイズを計算
        val canvasSize = size
        val scale = minOf(
            canvasSize.width / imageSize.width,
            canvasSize.height / imageSize.height
        )

        val drawWidth = imageSize.width * scale
        val drawHeight = imageSize.height * scale

        // グリッドの描画開始位置（中央寄せを考慮）
        val offsetX = (canvasSize.width - drawWidth) / 2
        val offsetY = (canvasSize.height - drawHeight) / 2

        val strokeWidth = 1.dp.toPx()
        val gridColor = Color.White.copy(alpha = 0.5f)

        // 垂直線 (2本)
        for (i in 1..2) {
            val x = offsetX + (drawWidth / 3) * i
            drawLine(
                color = gridColor,
                start = Offset(x, offsetY),
                end = Offset(x, offsetY + drawHeight),
                strokeWidth = strokeWidth
            )
        }

        // 水平線 (2本)
        for (i in 1..2) {
            val y = offsetY + (drawHeight / 3) * i
            drawLine(
                color = gridColor,
                start = Offset(offsetX, y),
                end = Offset(offsetX + drawWidth, y),
                strokeWidth = strokeWidth
            )
        }
    }

}