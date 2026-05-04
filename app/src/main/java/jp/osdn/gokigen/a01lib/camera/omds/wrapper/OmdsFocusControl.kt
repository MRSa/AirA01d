package jp.osdn.gokigen.a01lib.camera.omds.wrapper

import android.graphics.PointF
import android.util.Log
import android.view.MotionEvent
import jp.osdn.gokigen.a01lib.camera.interfaces.IFocusingControl
import jp.osdn.gokigen.a01lib.camera.interfaces.screen.IAutoFocusFrameDisplay
import jp.osdn.gokigen.a01lib.camera.interfaces.screen.IIndicatorControl
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsAutoFocusControl
import java.lang.Exception

class OmdsFocusControl(private val frameDisplay: IAutoFocusFrameDisplay, indicator: IIndicatorControl) : IFocusingControl
{
    private val afControl = OmdsAutoFocusControl(frameDisplay, indicator)

    override fun driveAutoFocus(motionEvent: MotionEvent?): Boolean
    {
        Log.v(TAG, "driveAutoFocus()")
        if (motionEvent?.action != MotionEvent.ACTION_DOWN)
        {
            return false
        }
        try
        {
            val point: PointF = frameDisplay.getPointWithEvent(motionEvent) ?: PointF(0.5f, 0.5f)
            if (frameDisplay.isContainsPoint(point))
            {
                afControl.lockAutoFocus(point)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return false
    }

    override fun unlockAutoFocus()
    {
        Log.v(TAG, "unlockAutoFocus()")
        try
        {
            afControl.unlockAutoFocus()
            frameDisplay.hideFocusFrame()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun halfPressShutter(isPressed: Boolean)
    {
        Log.v(TAG, "halfPressShutter() $isPressed")
        try
        {
            afControl.halfPressShutter(isPressed)
            if (!isPressed)
            {
                // フォーカスを外す
                frameDisplay.hideFocusFrame()
                afControl.unlockAutoFocus()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = OmdsFocusControl::class.java.simpleName
    }
}
