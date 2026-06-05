package jp.osdn.gokigen.a01lib.camera.omds.playback

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.utils.communication.HttpBinaryResponse
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient

class OmdsGetResizeImage(
    userAgent: String = "OlympusCameraKit",
    executeUrl: String = "http://192.168.0.10",
    private val timeoutMs: Int = TIMEOUT_MS
) {
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    // --- URLの末尾がスラッシュで終わるように補正（安全対策）
    private val baseUrl = if (executeUrl.endsWith("/")) executeUrl else "$executeUrl/"

    // --- 外部から getter/setter としてアクセスできる Kotlin スタイルのプロパティ
    var useOpcProtocol: Boolean = true

    init {
        headerMap["User-Agent"] = userAgent
        headerMap["X-Protocol"] = userAgent
    }

    /**
     * 指定した画像をリサイズして取得します
     */
    fun getResizeImage(directory: String, size: Int): HttpBinaryResponse?
    {
        try
        {
            // コマンドの組み立て
            val command = if (useOpcProtocol) GET_RESIZEIMAGE_COMMAND_OPC else GET_RESIZEIMAGE_COMMAND
            val cleanCommand = command.removePrefix("/")
            val commandUrl = "${baseUrl}${cleanCommand}?DIR=$directory&size=$size"
           return (http.httpCommandBinary(url = commandUrl, method = "GET", headerMap = headerMap, postData = null, contentType = null, timeoutMs = timeoutMs))
        }
        catch (e: Exception)
        {
            Log.e(TAG, "ERR> GET FILE INFO: ${e.localizedMessage}", e)
        }
        return null
    }

    companion object {
        private val TAG = OmdsGetResizeImage::class.java.simpleName
        private const val TIMEOUT_MS = 5500

        private const val GET_RESIZEIMAGE_COMMAND = "get_resizeimg.cgi"
        private const val GET_RESIZEIMAGE_COMMAND_OPC = "get_resizeimg.cgi"
    }
}
