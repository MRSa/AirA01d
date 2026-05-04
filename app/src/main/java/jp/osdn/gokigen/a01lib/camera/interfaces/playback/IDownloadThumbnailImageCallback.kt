package jp.osdn.gokigen.a01lib.camera.interfaces.playback

import android.graphics.Bitmap

interface IDownloadThumbnailImageCallback
{
    fun onCompleted(bitmap: Bitmap?, metadata: HashMap<String?, Any?>?)
    fun onErrorOccurred(e: Exception?)
}
