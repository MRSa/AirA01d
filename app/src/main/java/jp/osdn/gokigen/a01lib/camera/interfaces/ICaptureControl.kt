package jp.osdn.gokigen.a01lib.camera.interfaces

interface ICaptureControl
{
    enum class CaptureAction
    {
        ON, HALF, OFF
    }
    fun doCapture(captureAction: CaptureAction)
}