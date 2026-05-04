package jp.osdn.gokigen.a01lib.camera.interfaces.screen

import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent

interface IAutoFocusFrameDisplay
{
    // フォーカスフレームの状態
    enum class FocusFrameStatus {
        Running, Focused, Failed, Errored, None
    }

    fun getContentSizeWidth(): Float
    fun getContentSizeHeight(): Float

    fun getPointWithEvent(event: MotionEvent?): PointF?
    fun isContainsPoint(point: PointF?): Boolean

    fun showFocusFrame(rect: RectF?, status: FocusFrameStatus, duration: Float)
    fun hideFocusFrame()
}