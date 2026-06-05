package jp.osdn.gokigen.a01lib.camera.interfaces.playback

import android.graphics.Bitmap
import jp.osdn.gokigen.a01lib.camera.utils.communication.HttpBinaryResponse

// -----   画像再生・取得用インタフェース
interface IPlaybackControl
{


    fun enterPlaybackMode() : Boolean
    fun leavePlaybackMode() : Boolean

    fun getImageFileList(directory: String): List<ICameraFileInfo.ImageFileInfo>

    fun getMovieFileInfo(directory: String): ICameraFileInfo.MovieFileInfo
    fun getStillImageFileInfo(directory: String): IStillImageFileInfo.StillFileParameterInfo

    fun getImageThumbnail(directory: String): HttpBinaryResponse?
    fun getResizeImage(directory: String, size: Int): HttpBinaryResponse?
    fun getImageScreennail(directory: String): Bitmap?

    /*
    fun getRawFileSuffix() : String?
    fun downloadContentList(callback: ICameraContentListCallback)
    fun updateCameraFileInfo(info: ICameraFileInfo?)

    fun downloadContentScreennail(
        path: String?,
        name: String,
        callback: IDownloadThumbnailImageCallback
    )

    fun downloadContentThumbnail(
        path: String?,
        name: String,
        callback: IDownloadThumbnailImageCallback
    )

    fun downloadContent(
        path: String?,
        name: String,
        isSmallSize: Boolean,
        callback: IDownloadContentCallback
    )
*/

}
