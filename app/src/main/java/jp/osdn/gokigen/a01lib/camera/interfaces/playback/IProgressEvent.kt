package jp.osdn.gokigen.a01lib.camera.interfaces.playback

interface IProgressEvent
{
    val progress: Float
    val isCancellable: Boolean
    fun requestCancellation()
    interface CancelCallback
    {
        fun requestCancellation(result: Boolean)
    }
}
