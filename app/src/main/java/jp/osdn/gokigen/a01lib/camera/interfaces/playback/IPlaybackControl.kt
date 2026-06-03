package jp.osdn.gokigen.a01lib.camera.interfaces.playback

import java.util.Date

// -----   画像再生・取得用インタフェース
interface IPlaybackControl
{
    data class ImageFileInfo(
        val directory: String,
        val fileName: String,
        val fileSize: Long,
        val isReadonly: Boolean,
        val isHidden: Boolean,
        val isSystem: Boolean,
        val isVolume: Boolean,
        val isDirectory: Boolean,
        val isArchive: Boolean,
        val dateTime: Date,
    )

    fun enterPlaybackMode() : Boolean
    fun leavePlaybackMode() : Boolean

    fun getImageFileList(directory: String): List<ImageFileInfo>

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
