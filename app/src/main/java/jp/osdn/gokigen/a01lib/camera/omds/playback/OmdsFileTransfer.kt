package jp.osdn.gokigen.a01lib.camera.omds.playback

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.IPlaybackControl.IDownloadContentCallback
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient.IReceivedMessageCallback
import java.io.ByteArrayOutputStream


class OmdsFileTransfer(
    userAgent: String = "OlympusCameraKit",
    private val executeUrl: String = "http://192.168.0.10",
    private val timeoutMs: Int = TIMEOUT_MS
) {
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    // --- 外部から getter/setter としてアクセスできる Kotlin スタイルのプロパティ
    var useOpcProtocol: Boolean = true

    init {
        headerMap["User-Agent"] = userAgent
        headerMap["X-Protocol"] = userAgent
    }

    fun downloadContent(directory: String, callback: IDownloadContentCallback)
    {
        try
        {
            Thread {
                http.httpGetBytes(
                    url = executeUrl + directory,
                    headerMap = headerMap,
                    contentType = null,
                    timeoutMs = timeoutMs,
                    callback = object : IReceivedMessageCallback {
                        val byteAccumulator = ByteArrayOutputStream()

                        override fun onCompleted() {
                            // ---- データ受信応答を返す
                            callback.onCompleted(byteAccumulator.toByteArray())
                            byteAccumulator.close()
                        }

                        override fun onErrorOccurred(e: Exception?) {
                            // --- エラー応答を返す
                            callback.onErrorOccurred(e)
                            byteAccumulator.close()
                        }

                        override fun onReceive(
                            readBytes: Int,   // 現在までに送信されてきたバイト数
                            length: Int,      // データの総バイト数
                            size: Int,        // 今回送ったデータのサイズ
                            data: ByteArray?  // データボディ
                        ) {
                            // --- readBytes (累計) ではなく、今回届いた size が 0 より大きいかで判定する
                            try
                            {
                                if (data != null && size > 0) {
                                    try {
                                        byteAccumulator.write(data, 0, size)
                                    } catch (e: java.lang.Exception) {
                                        Log.e(TAG, "Write error", e)
                                    }
                                }
                                callback.onReceive(readBytes, length, size)
                            }
                            catch (e: Exception)
                            {
                                e.printStackTrace()
                            }

                        }
                    }
                )
            }.start()
        }
        catch (e: Exception)
        {
            Log.e(TAG, "ERR> GET FILE($directory): ${e.localizedMessage}", e)
        }
    }

    companion object {
        private val TAG = OmdsFileTransfer::class.java.simpleName
        private const val TIMEOUT_MS = 5500
    }
}
