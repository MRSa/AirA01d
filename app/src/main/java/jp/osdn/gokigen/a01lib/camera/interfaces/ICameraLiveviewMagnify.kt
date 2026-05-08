package jp.osdn.gokigen.a01lib.camera.interfaces

interface ICameraLiveviewMagnify
{
    // カメラの動作モード変更
    fun startMagnify(scale: Int)
    fun changeMagnify(scale: Int)
    fun stopMagnify()
}
