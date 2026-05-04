package jp.osdn.gokigen.a01lib.camera.interfaces

interface ICameraRunMode
{
    // カメラの動作モード変更
    fun changeRunMode(isRecording: Boolean, callback: ICameraRunModeCallback)
    fun isRecordingMode() : Boolean
}
