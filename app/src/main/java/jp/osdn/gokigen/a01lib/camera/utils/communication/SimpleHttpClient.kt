package jp.osdn.gokigen.a01lib.camera.utils.communication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * シンプルなHTTPクライアント
 */
class SimpleHttpClient
{
    interface IReceivedMessageCallback
    {
        fun onCompleted()
        fun onErrorOccurred(e: Exception?)
        fun onReceive(readBytes: Int, length: Int, size: Int, data: ByteArray?)
    }

    companion object {
        private val TAG = SimpleHttpClient::class.java.simpleName
        private const val DEFAULT_TIMEOUT = 10 * 1000 // [ms]
        private const val BUFFER_SIZE = 131072 * 2    // 256kB
    }

    /**
     * HttpURLConnectionのセットアップ
     */
    private fun setupConnection(
        url: String,
        method: String,
        setProperty: Map<String, String>?,
        contentType: String?,
        timeoutMs: Int,
        postData: String?
    ): HttpURLConnection {
        val timeout = if (timeoutMs < 0) DEFAULT_TIMEOUT else timeoutMs
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = timeout
            readTimeout = timeout
            instanceFollowRedirects = true // リダイレクトを許可

            // ヘッダーの設定
            setProperty?.forEach { (key, value) -> setRequestProperty(key, value) }
            contentType?.let { setRequestProperty("Content-Type", it) }
        }

        // ポストデータの書き込み
        if (postData != null) {
            conn.doOutput = true
            conn.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(postData) }
        }
        return conn
    }

    /**
     * 文字列レスポンス取得 (共通)
     */
    private fun httpCommand(
        url: String,
        method: String,
        postData: String?,
        setProperty: Map<String, String>?,
        contentType: String?,
        timeoutMs: Int
    ): String? {
        var conn: HttpURLConnection? = null
        return try {
            conn = setupConnection(url, method, setProperty, contentType, timeoutMs, postData)
            val responseCode = conn.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // readText()の前にサイズチェックが必要な場合はここで行う
                val body = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                "$responseCode $body"
            } else {
                Log.w(TAG, "HTTP $method Error: $responseCode - $url")
                "$responseCode "
            }
        } catch (e: Exception) {
            Log.w(TAG, "HTTP $method Exception: $url", e)
            null // 空文字よりnullの方がエラー判定しやすい
        } finally {
            conn?.disconnect() // 接続を確実に閉じる
        }
    }

    /**
     * バイト配列としての取得 (Callback経由)
     */
    private fun httpCommandBytes(
        url: String,
        requestMethod: String,
        postData: String?,
        setProperty: Map<String, String>?,
        contentType: String?,
        timeoutMs: Int,
        callback: IReceivedMessageCallback
    ) {
        var conn: HttpURLConnection? = null
        var isFinished = false // 二重呼び出し防止フラグ

        try
        {
            conn = setupConnection(url, requestMethod, setProperty, contentType, timeoutMs, postData)
            val responseCode = conn.responseCode

            if (responseCode != HttpURLConnection.HTTP_OK) {
                callback.onErrorOccurred(IOException("HTTP $responseCode"))
                return
            }

            conn.inputStream.use { input ->
                val contentLength = conn.getHeaderField("X-FILE_SIZE")?.toIntOrNull()
                    ?: conn.contentLength

                val buffer = ByteArray(BUFFER_SIZE)
                var totalReadBytes = 0

                while (true) {
                    val readSize = input.read(buffer)
                    if (readSize == -1) break

                    // 受信した分だけをコピーして渡す（参照による上書き防止）
                    val currentChunk = buffer.copyOf(readSize)

                    callback.onReceive(totalReadBytes, contentLength, readSize, currentChunk)
                    totalReadBytes += readSize
                }
            }
            callback.onCompleted()
            isFinished = true // 正常完了
        }
        catch (e: Exception)
        {
            Log.w(TAG, "HTTP Bytes Exception: $url", e)
            callback.onErrorOccurred(e)
            isFinished = true // エラーとして終了
        }
        finally
        {
            conn?.disconnect()
            // --- try/catch 内でonCompletedが呼ばれなかった場合のみonCompletedを呼ぶ
            if (!isFinished)
            {
                callback.onCompleted()
            }
        }
    }

    /**
     * Bitmapとしての取得
     */
    private fun httpCommandBitmap(
        url: String,
        method: String,
        postData: String?,
        setProperty: Map<String, String>?,
        contentType: String?,
        timeoutMs: Int
    ): Bitmap? {
        var conn: HttpURLConnection? = null
        return try {
            conn = setupConnection(url, method, setProperty, contentType, timeoutMs, postData)
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                conn.inputStream.use { BitmapFactory.decodeStream(it) }
            } else null
        } catch (e: Exception) {
            Log.w(TAG, "HTTP Bitmap Exception: $url", e)
            null
        } finally {
            conn?.disconnect()
        }
    }

    fun httpCommandBinary(
        url: String,
        method: String,
        postData: String?,
        headerMap: Map<String, String>?,
        contentType: String?,
        timeoutMs: Int
    ): HttpBinaryResponse?
    {
        var conn: HttpURLConnection? = null
        return try {
            conn = setupConnection(url, method, headerMap, contentType, timeoutMs, postData)
            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // --- 全てのレスポンスヘッダを取得 (Map<String, List<String>>)
                val headers = conn.headerFields ?: emptyMap()

                // --- ボディをバイト配列として丸ごと読み込み
                val body = conn.inputStream.use { it.readBytes() }

                HttpBinaryResponse(responseCode, headers, body)
            } else {
                Log.w(TAG, "HTTP GET Binary Error: $responseCode - $url")
                // エラー時もステータスコードと空のデータを返す、もしくは null
                HttpBinaryResponse(responseCode, conn.headerFields ?: emptyMap(), ByteArray(0))
            }
        }
        catch (e: Exception)
        {
            Log.w(TAG, "HTTP GET Binary Exception: $url", e)
            null
        }
        finally
        {
            conn?.disconnect()
        }
    }

    // --- 各種ラッパーメソッド ---
    fun httpGet(url: String, timeoutMs: Int) = httpCommand(url, "GET", null, null, null, timeoutMs)

    fun httpPost(url: String, postData: String?, timeoutMs: Int) = httpCommand(url, "POST", postData, null, null, timeoutMs)

    fun httpGetWithHeader(url: String, headerMap: Map<String, String>?, contentType: String?, timeoutMs: Int) =
        httpCommand(url, "GET", null, headerMap, contentType, timeoutMs)

    fun httpPostWithHeader(url: String, postData: String?, headerMap: Map<String, String>?, contentType: String?, timeoutMs: Int) =
        httpCommand(url, "POST", postData, headerMap, contentType, timeoutMs)

    fun httpPutWithHeader(url: String, putData: String?, headerMap: Map<String, String>?, contentType: String?, timeoutMs: Int) =
        httpCommand(url, "PUT", putData, headerMap, contentType, timeoutMs)

    fun httpGetBytes(url: String, headerMap: Map<String, String>?, timeoutMs: Int, contentType: String?, callback: IReceivedMessageCallback) =
        httpCommandBytes(url, "GET", null, headerMap, contentType, timeoutMs, callback)

    fun httpGetBitmap(url: String, setProperty: Map<String, String>?, timeoutMs: Int) =
        httpCommandBitmap(url, "GET", null, setProperty, null, timeoutMs)
}
