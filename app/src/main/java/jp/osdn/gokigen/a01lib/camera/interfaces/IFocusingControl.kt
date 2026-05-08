package jp.osdn.gokigen.a01lib.camera.interfaces

interface IFocusingControl
{
    fun driveAutoFocus(posX: Float, posY: Float): Boolean
    fun unlockAutoFocus()

    fun lockAutoExposure()
    fun unlockAutoExposure()
}
