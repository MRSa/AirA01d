package jp.osdn.gokigen.a01lib.camera.interfaces

import android.view.MotionEvent

interface IFocusingControl
{
    fun driveAutoFocus(motionEvent: MotionEvent?): Boolean
    fun unlockAutoFocus()
    fun halfPressShutter(isPressed: Boolean)
}