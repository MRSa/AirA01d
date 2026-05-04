package jp.osdn.gokigen.a01lib.camera.interfaces

interface ICameraRunModeCallback
{
    fun onCompleted(isRecording: Boolean)
    fun onErrorOccurred(isRecording: Boolean)
}
