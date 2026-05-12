package jp.osdn.gokigen.a01lib.camera.interfaces

interface ICaptureControl
{
    enum class CaptureAction
    {
        ON, OFF, TOGGLE
    }
    fun doCapture(captureAction: CaptureAction)
    fun doMovie(captureAction: CaptureAction)
}