package jp.osdn.gokigen.a01lib.camera.utils.communication

import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class SimpleLiveViewSlicer
{
    class Payload(val jpegData: ByteArray?, val paddingData: ByteArray?)

    // マーカーは外部から変更可能。初期値は MJPEG 標準
    private var mJpegStartMarker = byteArrayOf(0x0d.toByte(), 0x0a.toByte(), 0x0d.toByte(), 0x0a.toByte(), 0xff.toByte(), 0xd8.toByte())
    private var mHttpConn: HttpURLConnection? = null
    private var mInputStream: InputStream? = null

    fun setMJpegStartMarker(startMarker: IntArray)
    {
        mJpegStartMarker = ByteArray(startMarker.size) { startMarker[it].toByte() }
    }

    // URL接続の共通処理（DRY原則）
    private fun setupConnection(url: String, method: String): Boolean
    {
        return try {
            if (mInputStream != null || mHttpConn != null) {
                Log.v(TAG, "Slicer is already open.")
                return false
            }
            mHttpConn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = CONNECTION_TIMEOUT
                readTimeout = CONNECTION_TIMEOUT // リードタイムアウトも設定すべき
                doInput = true
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Connection setup failed", e)
            false
        }
    }

    fun open(liveViewUrl: String, postData: String?, contentType: String?)
    {
        if (!setupConnection(liveViewUrl, "POST")) return
        try
        {
            mHttpConn?.apply {
                doOutput = true
                if (contentType != null) setRequestProperty("Content-Type", contentType)

                outputStream.use { os ->
                    OutputStreamWriter(os, "UTF-8").use { it.write(postData) }
                }

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    mInputStream = BufferedInputStream(inputStream) // バッファリング追加
                }
            }
        }
        catch (e: Exception)
        {
            Log.e(TAG, "POST open failed", e)
        }
    }

    fun close()
    {
        // stream.close() は connection.disconnect() で内部的に行われることが多いが、明示的に閉じる
        runCatching { mInputStream?.close() }
        runCatching { mHttpConn?.disconnect() }
        mInputStream = null
        mHttpConn = null
    }

    fun nextPayload(): Payload?
    {
        val stream = mInputStream ?: return null
        return try {
            // Common Header (8 bytes)
            val commonHeader = readBytesStrict(stream, 8) ?: return null

            if (commonHeader[0] != 0xFF.toByte()) return null

            when (commonHeader[1]) {
                0x12.toByte() -> {
                    // Skip Information Header
                    readBytesStrict(stream, 166)
                    nextPayload() // 再帰またはループで次を探す
                }
                0x01.toByte(), 0x11.toByte() -> readPayload(stream)
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "NextPayload error", e)
            null
        }
    }

    private fun readPayload(stream: InputStream): Payload?
    {
        // Payload Header (128 bytes)
        val header = readBytesStrict(stream, 128) ?: return null

        // Start code check ($5hy)
        if (header[0] != 0x24.toByte() || header[1] != 0x35.toByte()) return null

        val jpegSize = bytesToInt(header, 4, 3)
        val paddingSize = bytesToInt(header, 7, 1)

        val jpegData = readBytesStrict(stream, jpegSize)
        val paddingData = if (paddingSize > 0) readBytesStrict(stream, paddingSize) else null

        return Payload(jpegData, paddingData)
    }

    companion object
    {
        private val TAG = SimpleLiveViewSlicer::class.java.simpleName
        private const val CONNECTION_TIMEOUT = 2000

        /**
         * 指定されたサイズを厳密に読み込む（不完全な読み込みを防ぐ）
         */
        private fun readBytesStrict(inputStream: InputStream, length: Int): ByteArray?
        {
            val buffer = ByteArray(length)
            var totalRead = 0
            while (totalRead < length)
            {
                val read = inputStream.read(buffer, totalRead, length - totalRead)
                if (read == -1) return null
                totalRead += read
            }
            return buffer
        }

        private fun bytesToInt(data: ByteArray, start: Int, count: Int): Int
        {
            var result = 0
            for (i in 0 until count)
            {
                result = (result shl 8) or (data[start + i].toInt() and 0xFF)
            }
            return result
        }
    }
}
