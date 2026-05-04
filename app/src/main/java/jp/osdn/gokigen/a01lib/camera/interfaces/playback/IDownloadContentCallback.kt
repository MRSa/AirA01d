package jp.osdn.gokigen.a01lib.camera.interfaces.playback

interface IDownloadContentCallback
{
    fun onCompleted()
    fun onErrorOccurred(e: Exception?)
    fun onProgress(data: ByteArray?, length: Int, e: IProgressEvent?)
}
