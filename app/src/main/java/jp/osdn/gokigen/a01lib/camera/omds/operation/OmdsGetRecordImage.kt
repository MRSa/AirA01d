package jp.osdn.gokigen.a01lib.camera.omds.operation

import jp.osdn.gokigen.a01lib.camera.interfaces.IGetRecordImage
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.lang.Exception
import java.util.HashMap

class OmdsGetRecordImage(userAgent: String = "OlympusCameraKit", private val executeUrl : String = "http://192.168.0.10", private val useOpcProtocol: Boolean = true) : IGetRecordImage
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    override fun getCapturedImage(isLastJpeg: Boolean, callback: IGetRecordImage.RecordImageCallback)
    {
        // ----- 撮影した画像をカメラに要求する （画像を取得しないと先に進まない様子...）
        try
        {
            val thread = Thread {
                try
                {
                    val command: String = if (isLastJpeg)
                    {
                        if (useOpcProtocol)
                        {
                            "/exec_takemisc.cgi?com=getlastjpg"
                        }
                        else
                        {
                            "/exec_takemisc.cgi?com=getlastjpg"
                        }
                    }
                    else
                    {
                        if (useOpcProtocol)
                        {
                            "/exec_takemisc.cgi?com=getrecview"
                        }
                        else
                        {
                            "/exec_takemisc.cgi?com=getrecview"
                        }
                    }
                    val reply: String = http.httpGetWithHeader(
                        executeUrl + command,
                        headerMap,
                        null,
                        TIMEOUT_MS
                    ) ?: ""
                    // ---- 応答を返す
                    callback.receivedRecordImage(isLastJpeg, reply)
                }
                catch (e: Exception)
                {
                    e.printStackTrace()

                    // ---- 応答を返す
                    callback.receivedRecordImage(isLastJpeg, "")
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private const val TIMEOUT_MS = 3000
    }

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"
    }
}
