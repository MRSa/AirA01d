package jp.osdn.gokigen.a01lib.camera.interfaces.liveview

interface IImageDataReceiver
{
    fun onUpdateLiveView(data: ByteArray, metadata: Map<String, Any>?, degrees : Int = 0)
}
