package jp.osdn.gokigen.aira01d.ui.component.widget.drawer

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel
import jp.osdn.gokigen.aira01d.ui.model.SelfTimerViewModel

@Composable
fun LiveviewWidget(
    viewModel: LiveviewViewModel,
    selfTimer: SelfTimerViewModel,
    modifier: Modifier = Modifier
) {
    // --- ViewModel の状態を監視 (StateFlow / collectAsStateWithLifecycle) ---
    val bitmap by viewModel.liveViewBitmap.collectAsStateWithLifecycle()

    val isGridOn = viewModel.isGridOn.observeAsState()
    val isShowFocusFrame = viewModel.isShowFocusFrame.observeAsState()
    val focusFrameStatus = viewModel.focusFrameStatus.observeAsState()
    val focusFrameRectangle = viewModel.focusFrameRectangle.observeAsState()
    val isFocusAssist = viewModel.isFocusAssist.observeAsState()


    // SelfTimerViewModel の監視
    val isTimerActivated by selfTimer.isTimerActivated.collectAsStateWithLifecycle()
    val remainSec by selfTimer.isTimerRemainSec.collectAsStateWithLifecycle()

    // コンポーネントのサイズを保持する変数
    var layoutSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .background(Color.Black)
            .onGloballyPositioned { coordinates ->
                layoutSize = coordinates.size
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val btm = bitmap ?: return@detectTapGestures

                    val canvasWidth = layoutSize.width.toFloat()
                    val canvasHeight = layoutSize.height.toFloat()
                    if (canvasWidth <= 0.0f || canvasHeight <= 0.0f) return@detectTapGestures

                    val imgWidth = btm.width.toFloat()
                    val imgHeight = btm.height.toFloat()

                    // ContentScale.Fit 時のスケールを計算
                    val scale = minOf(canvasWidth / imgWidth, canvasHeight / imgHeight)
                    val drawWidth = imgWidth * scale
                    val drawHeight = imgHeight * scale

                    // 画像の開始位置（オフセット）を計算
                    val offsetX = (canvasWidth - drawWidth) / 2f
                    val offsetY = (canvasHeight - drawHeight) / 2f

                    // タップ位置を画像内の相対座標に変換
                    val relativeX = (offset.x - offsetX) / drawWidth
                    val relativeY = (offset.y - offsetY) / drawHeight

                    if (relativeX in 0f..1f && relativeY in 0f..1f) {
                        viewModel.onTouchPosition(relativeX, relativeY)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let { btm ->
            val imageBitmap = btm.asImageBitmap()

            // 画像とオーバーレイ（グリッド、フォーカス、タイマー）を重ねる
            Box(contentAlignment = Alignment.Center) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Live View Bitmap",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(1.dp)
                        .let { baseModifier ->
                            // --- フォーカスアシストモード かつ Android 13 (API 33) 以上の時だけ輪郭強調を適用
                            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)&&(isFocusAssist.value == true)) {
                                baseModifier.contourHighlight(
                                    enabled = true, // ViewModel等から状態を取って切り替えてもOK
                                    highlightColor = androidx.compose.ui.graphics.Color.White, // ハイライトの色
                                    threshold = 0.12f // 感度（小さくするほど微細な輪郭も拾う）
                                )
                            } else {
                                baseModifier
                            }
                        },
                    contentScale = ContentScale.Fit
                )

                // ----- フォーカス表示 -----
                if (isShowFocusFrame.value == true && focusFrameRectangle.value != null) {
                    FocusFrameDrawer(
                        imageSize = Size(imageBitmap.width.toFloat(), imageBitmap.height.toFloat()),
                        focusFrameStatus = focusFrameStatus.value,
                        focusFrameRectangle = focusFrameRectangle.value,
                        modifier = Modifier
                            .matchParentSize()
                            .padding(1.dp)
                    )
                }

                // ----- Grid 表示 -----
                if (isGridOn.value == true) {
                    GridDrawer(
                        imageSize = Size(imageBitmap.width.toFloat(), imageBitmap.height.toFloat()),
                        modifier = Modifier
                            .matchParentSize()
                            .padding(1.dp)
                    )
                }

                // ----- セルフタイマー カウントダウン表示 -----
                if (isTimerActivated && remainSec > 0) {
                    Box(
                        modifier = Modifier.matchParentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = remainSec,
                            transitionSpec = {
                                // 数字が切り替わる際のアニメーション (フェードイン・アウト)
                                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(
                                    animationSpec = tween(300)
                                )
                            },
                            label = "TimerAnimation"
                        ) { targetCount ->
                            Text(
                                text = targetCount.toString(),
                                style = TextStyle(
                                    fontSize = 120.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        offset = Offset(4f, 4f),
                                        blurRadius = 8f
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun Modifier.contourHighlight(
    enabled: Boolean = true,
    highlightColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Red,
    threshold: Float = 0.05f
): Modifier = if (!enabled) this else this.graphicsLayer {
    // 描画領域のサイズが確定していない場合はスキップ
    if (size.width <= 0f || size.height <= 0f) return@graphicsLayer

    // ----- AGSL（Android Graphics Shading Language）によるシェーダーコード
    val shaderCode = """
        uniform shader inputShader;
        layout(color) uniform vec4 uColor;
        uniform float uThreshold;

        half4 main(vec2 fragCoord) {
            half4 center = inputShader.eval(fragCoord);
            
            // 画面上の1ピクセル隣のRGBを取得
            half4 left   = inputShader.eval(fragCoord + vec2(-1.0, 0.0));
            half4 right  = inputShader.eval(fragCoord + vec2(1.0, 0.0));
            half4 top    = inputShader.eval(fragCoord + vec2(0.0, -1.0));
            half4 bottom = inputShader.eval(fragCoord + vec2(0.0, 1.0));

            // ラプラシアンフィルタによるエッジ抽出
            half4 edge = abs(left + right + top + bottom - (center * 4.0));
            float intensity = (edge.r + edge.g + edge.b) / 3.0;

            // 輝度の変化量がしきい値を超えたら指定色でハイライト
            if (intensity > uThreshold) {
                return half4(uColor.r, uColor.g, uColor.b, 1.0);
            }
            return center;
        }
    """.trimIndent()

    val shader = RuntimeShader(shaderCode)

    // パラメータをシェーダーに転送
    shader.setColorUniform("uColor", highlightColor.value.toLong())
    shader.setFloatUniform("uThreshold", threshold)

    // RenderEffectを生成してComposeのgraphicsLayerに適用
    renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "inputShader").asComposeRenderEffect()
}
