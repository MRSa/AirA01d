package jp.osdn.gokigen.a01lib.camera.omds.operation

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.IGetRecordImage
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient.IReceivedMessageCallback
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.HashMap

class OmdsGetRecordImage(userAgent: String = "OlympusCameraKit", private val executeUrl : String = "http://192.168.0.10") : IGetRecordImage
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()
    private var useOpcProtocol: Boolean = true

    fun setUseOpcProtocol(isOpcProtocol: Boolean)
    {
        useOpcProtocol = isOpcProtocol
    }

    override fun getCapturedImage(
        isLastJpeg: Boolean,
        callback: IGetRecordImage.RecordImageCallback
    ) {
        // ----- 撮影した画像をカメラに要求する （イベント受信時には、画像を取得しないと先に進まない様子...）
        try {
            val thread = Thread {
                try {
                    val command: String = if (isLastJpeg) {
                        if (useOpcProtocol) {
                            "/exec_takemisc.cgi?com=getlastjpg"
                        } else {
                            "/exec_takemisc.cgi?com=getlastjpg"
                        }
                    } else {
                        if (useOpcProtocol) {
                            "/exec_takemisc.cgi?com=getrecview"
                        } else {
                            "/exec_takemisc.cgi?com=getrecview"
                        }
                    }
                    http.httpGetBytes(
                        url = executeUrl + command,
                        headerMap = headerMap,
                        contentType = null,
                        timeoutMs = TIMEOUT_MS,
                        callback = object : IReceivedMessageCallback {
                            val byteAccumulator = ByteArrayOutputStream()

                            override fun onCompleted() {
                                // ---- データ受信応答を返す
                                callback.receivedRecordImage(isLastJpeg, byteAccumulator.toByteArray())
                                byteAccumulator.close()
                            }

                            override fun onErrorOccurred(e: kotlin.Exception?) {
                                // --- エラー応答を返す
                                callback.receivedRecordImage(isLastJpeg, null)
                                byteAccumulator.close()
                            }

                            override fun onReceive(
                                readBytes: Int,   // 現在までに送信されているバイト数
                                length: Int,      // データの総バイト数
                                size: Int,        // 今回送ったデータのサイズ
                                data: ByteArray?  // データボディ
                            ) {
                                // readBytes (累計) ではなく、今回届いた size が 0 より大きいかで判定する
                                if (data != null && size > 0) {
                                    try {
                                        byteAccumulator.write(data, 0, size)
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Write error", e)
                                    }
                                }
                            }
                        }
                    )
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                    // ---- 応答を返す
                    callback.receivedRecordImage(isLastJpeg, null)
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"
    }
    companion object
    {
        private val TAG = OmdsGetRecordImage::class.java.simpleName
        private const val TIMEOUT_MS = 10000
    }
}
