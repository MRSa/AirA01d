package jp.osdn.gokigen.a01lib.camera.omds.operation

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.IDigitalZoomControl
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.lang.Exception
import java.util.HashMap
import java.util.Locale

class OmdsDigitalZoomControl(
    userAgent: String = "OlympusCameraKit",
    private val executeUrl : String = "http://192.168.0.10",
    private val useOpcProtocol: Boolean = true
) : IDigitalZoomControl
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    override fun getDigitalScopeScale(callback: IDigitalZoomControl.DigitalZoomScaleCallback)
    {
        Log.v(TAG, "getDigitalScopeScale()")
        try
        {
            val sendUrl = if (useOpcProtocol) { "$executeUrl$COMMAND_DIGITAL_ZOOM_SCALE_OPC" } else { "$executeUrl$COMMAND_DIGITAL_ZOOM_SCALE" }
            val reply: String = http.httpGetWithHeader(sendUrl, headerMap, null, TIMEOUT_MS) ?: ""

            // ----- 上限%, 下限%を取得して応答する
            val lower = getNumberElement(reply, "lower") ?: 100
            val upper = getNumberElement(reply, "upper") ?: 100
            callback.zoomScale(lower, upper)
            return
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        // --- 取得失敗時には、 100, 100 (スケールできない)を応答する
        callback.zoomScale(100, 100)
    }

    override fun changeDigitalZoomScale(scale: Int)
    {
        Log.v(TAG, "changeDigitalZoomScale($scale)")
        try
        {
            val sendUrl = if (useOpcProtocol) { "$executeUrl$COMMAND_EXECUTE_DIGITAL_ZOOM_OPC&scope=$scale" } else { "$executeUrl$COMMAND_EXECUTE_DIGITAL_ZOOM&scope=$scale" }
            val reply: String = http.httpGetWithHeader(sendUrl, headerMap, null, TIMEOUT_MS) ?: ""
            if ((!reply.lowercase(Locale.US).contains("ok")))
            {
                Log.v(TAG, "DIGITAL Zoom : $reply ($sendUrl)")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun getNumberElement(checkString: String, key: String) : Int?
    {
        try
        {
            val regex = Regex("<$key>(\\d+)</$key>")
            val matchResult = regex.find(checkString)
            if (matchResult != null)
            {
                return matchResult.groupValues[1].toInt()
            }
        }
        catch (_: Exception) { }
        return null
    }

    companion object
    {
        private val TAG = OmdsDigitalZoomControl::class.java.simpleName

        private const val COMMAND_DIGITAL_ZOOM_SCALE = "/exec_takemisc.cgi?com=digizoomscope"
        private const val COMMAND_DIGITAL_ZOOM_SCALE_OPC = "/exec_takemisc.cgi?com=newdigizoomscope"

        private const val COMMAND_EXECUTE_DIGITAL_ZOOM = "/exec_takemisc.cgi?com=ctrldigizoom"
        private const val COMMAND_EXECUTE_DIGITAL_ZOOM_OPC = "/exec_takemisc.cgi?com=newctrldigizoom"

        private const val TIMEOUT_MS = 3000
    }

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"
    }
}
