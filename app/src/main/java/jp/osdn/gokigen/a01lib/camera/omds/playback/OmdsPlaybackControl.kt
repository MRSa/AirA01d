package jp.osdn.gokigen.a01lib.camera.omds.playback

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.ICameraFileInfo
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.IPlaybackControl
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.IPlaybackControl.IContentTransferCallback
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.IPlaybackControl.IDownloadContentCallback
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.IStillImageFileInfo

class OmdsPlaybackControl(
    userAgent: String = "OlympusCameraKit",
    executeUrl : String = "http://192.168.0.10",
    timeoutMs: Int = TIMEOUT_MS
) : IPlaybackControl
{
    private val imageListGetter = OmdsGetImageFileList(userAgent = userAgent, executeUrl = executeUrl, timeoutMs = timeoutMs)
    private val fileInfoGetter = OmdsGetFileInfo(userAgent = userAgent, executeUrl = executeUrl, timeoutMs = timeoutMs)
    private val getThumbnail = OmdsGetThumbnail(userAgent = userAgent, executeUrl = executeUrl, timeoutMs = timeoutMs)
    private val getScreennail = OmdsGetScreennail(userAgent = userAgent, executeUrl = executeUrl, timeoutMs = timeoutMs)
    private val resizeImage = OmdsGetResizeImage(userAgent = userAgent, executeUrl = executeUrl, timeoutMs = timeoutMs)
    private val fileTransfer = OmdsFileTransfer(userAgent = userAgent, executeUrl = executeUrl, timeoutMs = timeoutMs)

    fun setUseOpcProtocol(isOpcProtocol: Boolean)
    {
        imageListGetter.useOpcProtocol = isOpcProtocol
        fileInfoGetter.useOpcProtocol = isOpcProtocol
        getThumbnail.useOpcProtocol = isOpcProtocol
        getScreennail.useOpcProtocol = isOpcProtocol
        resizeImage.useOpcProtocol = isOpcProtocol
        fileTransfer.useOpcProtocol = isOpcProtocol
    }

    override fun getRawFileSuffix(): String
    {
        return "ORF"
    }

    override fun enterPlaybackMode(): Boolean { return true }
    override fun leavePlaybackMode(): Boolean { return true }

    override fun getImageFileList(directory: String): List<ICameraFileInfo.ImageFileInfo>
    {
        return imageListGetter.getImageFileList(directory)
    }

    override fun getMovieFileInfo(directory: String): ICameraFileInfo.MovieFileInfo
    {
        return fileInfoGetter.getMovieFileInfo(directory)
    }

    override fun getStillImageFileInfo(directory: String): IStillImageFileInfo.StillFileParameterInfo
    {
        return fileInfoGetter.getStillImageFileInfo(directory)
    }

    override fun getImageThumbnail(directory: String): Bitmap?
    {
        try
        {
            val reply = getThumbnail.getImageThumbnail(directory)
            if (reply?.body != null)
            {
                // ----- ヘッダの解釈をする場合は、ここで実行
                return BitmapFactory.decodeByteArray(reply.body, 0, (reply.body).size)
            }
            Log.w(TAG, "FAIL> getImageThumbnail($directory)")
        }
        catch (t: Throwable)
        {
            Log.w(TAG, "ERR>get Thumbnail: $directory (${t.localizedMessage})")
        }
        return null
    }

    override fun getResizeImage(directory: String, size: Int): Bitmap?
    {
        try
        {
            val reply = resizeImage.getResizeImage(directory, size)
            if (reply?.body != null)
            {
                // ----- ヘッダの解釈をする場合は、ここで実行
                return BitmapFactory.decodeByteArray(reply.body, 0, (reply.body).size)
            }
            Log.w(TAG, "FAIL> getResizeImage($directory, $size)")
        }
        catch (t: Throwable)
        {
            Log.w(TAG, "ERR>get getResizeImage($directory, $size) : ${t.localizedMessage}")
        }
        return null
    }

    override fun getImageScreennail(directory: String): Bitmap?
    {
        return getScreennail.getImageScreennail(directory)
    }

    override fun downloadContent(directory: String, callback: IDownloadContentCallback)
    {
        fileTransfer.downloadContent(directory, callback)
    }

    override fun downloadContent(directory: String, callback: IContentTransferCallback)
    {
        fileTransfer.downloadContent(directory, callback)
    }

    companion object
    {
        private val TAG = OmdsPlaybackControl::class.java.simpleName
        private const val TIMEOUT_MS = 5500
    }
}
