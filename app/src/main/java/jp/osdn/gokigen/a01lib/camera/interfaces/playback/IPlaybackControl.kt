package jp.osdn.gokigen.a01lib.camera.interfaces.playback

import android.graphics.Bitmap

// -----   画像再生・取得用インタフェース
interface IPlaybackControl
{
    // ----- RAWファイルの拡張子を応答する -----
    fun getRawFileSuffix() : String?

    // ----- 画像再生モードへ移行する ----
    fun enterPlaybackMode() : Boolean

    // ----- 画像再生モードから離脱する -----
    fun leavePlaybackMode() : Boolean

    // ----- 画像ファイルの一覧を取得する -----
    fun getImageFileList(directory: String): List<ICameraFileInfo.ImageFileInfo>

    // ----- 動画ファイルの情報を取得する -----
    fun getMovieFileInfo(directory: String): ICameraFileInfo.MovieFileInfo

    // ----- 静止画ファイルの情報を取得する -----
    fun getStillImageFileInfo(directory: String): IStillImageFileInfo.StillFileParameterInfo

    // ----- 画像ファイルのサムネイルを取得する -----
    fun getImageThumbnail(directory: String): Bitmap?

    // ----- サイズを調整した画像を取得する -----
    fun getResizeImage(directory: String, size: Int): Bitmap?

    // ----- 画面表示用の画像を取得する -----
    fun getImageScreennail(directory: String): Bitmap?

    // ----- 画像ファイルを取得する -----
    fun downloadContent(directory: String, callback: IDownloadContentCallback)

    interface IDownloadContentCallback
    {
        fun onCompleted(downloadedContent: ByteArray?)
        fun onErrorOccurred(e: Exception?)
        fun onReceive(readBytes: Int, length: Int, size: Int)
    }
}
