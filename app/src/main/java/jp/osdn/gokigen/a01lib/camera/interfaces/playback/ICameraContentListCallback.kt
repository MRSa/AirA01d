package jp.osdn.gokigen.a01lib.camera.interfaces.playback

interface ICameraContentListCallback {
    fun onCompleted(contentList: List<ICameraFileInfo?>?)
    fun onErrorOccurred(e: Exception?)
}
