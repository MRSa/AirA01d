package jp.osdn.gokigen.a01lib.camera.interfaces.playback

// -----   画像再生・取得用インタフェース
interface IPlaybackControl
{
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

    fun startedPlayback()
    fun finishedPlayback()
}
