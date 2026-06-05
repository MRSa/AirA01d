package jp.osdn.gokigen.a01lib.camera.omds.playback

import android.graphics.Bitmap
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.ICameraFileInfo
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.IPlaybackControl
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.IStillImageFileInfo
import jp.osdn.gokigen.a01lib.camera.utils.communication.HttpBinaryResponse

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

    fun setUseOpcProtocol(isOpcProtocol: Boolean)
    {
        imageListGetter.useOpcProtocol = isOpcProtocol
        fileInfoGetter.useOpcProtocol = isOpcProtocol
        getThumbnail.useOpcProtocol = isOpcProtocol
        getScreennail.useOpcProtocol = isOpcProtocol
        resizeImage.useOpcProtocol = isOpcProtocol
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

    override fun getImageThumbnail(directory: String): HttpBinaryResponse?
    {
        return getThumbnail.getImageThumbnail(directory)
    }

    override fun getResizeImage(directory: String, size: Int): HttpBinaryResponse?
    {
        return resizeImage.getResizeImage(directory, size)
    }

    override fun getImageScreennail(directory: String): Bitmap?
    {
        return getScreennail.getImageScreennail(directory)
    }

    companion object
    {
        private val TAG = OmdsPlaybackControl::class.java.simpleName
        private const val TIMEOUT_MS = 5500
    }
}
