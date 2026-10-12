package jp.osdn.gokigen.aira01d.ui.component.widget.drawer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import kotlin.math.abs

@Composable
fun CameraLevelDrawer(
    cameraStatusViewModel: CameraStatusViewModel,
    modifier: Modifier = Modifier)
{
    val pitch = cameraStatusViewModel.levelGaugePitch.observeAsState()
    val roll = cameraStatusViewModel.levelGaugeRoll.observeAsState()
    val orientation = cameraStatusViewModel.levelGaugeOrientation.observeAsState()

    val maxRange = 900f  // pitch, roll の最大値
    val pitchValue = pitch.value ?: 0
    val rollValue = roll.value ?: 0

    // カメラ向きからアイコンのリソースIDを決定する
    val iconId = when (orientation.value) {
        0 -> R.drawable.position_normal
        1 -> R.drawable.position_right
        2 -> R.drawable.position_opposit
        3 -> R.drawable.position_left
        4 -> R.drawable.position_downward
        5 -> R.drawable.position_upward
        else -> R.drawable.position_unknown
    }

    // BoxでCanvasとアイコンを重ねる
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize())
        {
            // 色の指定
            val pitchColor = decideLevelColor(pitchValue)
            val rollColor = decideLevelColor(rollValue)

            //  バーの長さ
            val barLengthPitch = (pitchValue / maxRange) * (size.width / 2)
            val barLengthRoll = (rollValue / maxRange) * (size.height / 2)

            // バーの描画起点
            val offsetX = size.width / 2
            val offsetY = size.height / 2

            val strokeWidth = 10.dp.toPx()

            // pitch の描画
            drawLine(
                color = pitchColor,
                start = Offset(size.width - strokeWidth, offsetY),
                end = Offset(size.width - strokeWidth, offsetY - barLengthPitch),
                strokeWidth = strokeWidth
            )

            // pitch の基準線描画
            drawLine(
                color = Color.White,
                start = Offset(size.width, offsetY),
                end = Offset(size.width - strokeWidth, offsetY),
                strokeWidth = 1.dp.toPx()
            )

            // roll の描画
            drawLine(
                color = rollColor,
                start = Offset(offsetX, size.height - strokeWidth),
                end = Offset(offsetX - barLengthRoll, size.height - strokeWidth),
                strokeWidth = strokeWidth
            )

            // roll の基準線描画
            drawLine(
                color = Color.White,
                start = Offset(offsetX, size.height),
                end = Offset(offsetX, size.height - strokeWidth),
                strokeWidth = 1.dp.toPx()
            )
        }

        // カメラ向き(Image drawable)の表示
        Image(
            painter = painterResource(id = iconId),
            contentDescription = "Camera Orientation",
            colorFilter = ColorFilter.tint(Color.White), // 全体を白色にする
            modifier = Modifier
                .size(32.dp) // 表示の大きさ：32dp x 32dp
                .shadow(elevation = 4.dp, shape = CircleShape) // 影をつける
                .align(Alignment.BottomStart)
                .padding(4.dp)
        )
    }
}

private fun decideLevelColor(value: Int): Color
{
    val absValue = abs(value)
    if (absValue < 100)
    {
        // ----- 緑
        return Color.Green
    }
    else if (absValue < 300)
    {
        // ----- 黄
        return Color.Yellow
    }
    else if (absValue < 450)
    {
        // ----- 赤
        return Color.Red
    }
    else if (absValue < 600)
    {
        // ----- 赤
        return Color.Red
    }
    // ----- 赤
    return Color.Red
}