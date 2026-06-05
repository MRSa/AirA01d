package jp.osdn.gokigen.a01lib.camera.omds.playback

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient

class OmdsGetScreennail(
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
    fun getImageScreennail(directory: String): Bitmap?
    {
        try
        {
            // コマンドの組み立て
            val command = if (useOpcProtocol) GET_SCREENNAIL_COMMAND_OPC else GET_SCREENNAIL_COMMAND
            val cleanCommand = command.removePrefix("/")
            val commandUrl = "${baseUrl}${cleanCommand}?DIR=$directory"
            val response = http.httpCommandBinary(url = commandUrl, method = "GET", headerMap = headerMap, postData = null, contentType = null, timeoutMs = timeoutMs)
            if (response?.body != null)
            {
                return BitmapFactory.decodeByteArray(response.body, 0, response.body.size)
            }
        }
        catch (e: Exception)
        {
            Log.e(TAG, "ERR> GET FILE INFO: ${e.localizedMessage}", e)
        }
        return null
    }

    companion object {
        private val TAG = OmdsGetScreennail::class.java.simpleName
        private const val TIMEOUT_MS = 5500

        private const val GET_SCREENNAIL_COMMAND = "get_resizeimg.cgi"
        private const val GET_SCREENNAIL_COMMAND_OPC = "get_resizeimg.cgi"
    }
}
