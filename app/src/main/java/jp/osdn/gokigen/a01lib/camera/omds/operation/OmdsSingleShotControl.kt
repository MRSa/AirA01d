package jp.osdn.gokigen.a01lib.camera.omds.operation

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.lang.Exception
import java.util.HashMap

class OmdsSingleShotControl(userAgent: String = "OlympusCameraKit", private val executeUrl : String = "http://192.168.0.10")
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()
    private var useOpcProtocol: Boolean = true

    fun setUseOpcProtocol(isOpcProtocol: Boolean)
    {
        useOpcProtocol = isOpcProtocol
    }

    fun singleShot()
    {
        Log.v(TAG, "singleShot()")
        try
        {
            val sendUrl = if (useOpcProtocol) {
                executeUrl + CAPTURE_COMMAND_OPC
            } else {
                executeUrl + CAPTURE_COMMAND
            }
            val reply: String = http.httpGetWithHeader(sendUrl, headerMap, null, TIMEOUT_MS) ?: ""
            if ((!reply.contains("ok")) && (!reply.contains("OK"))) {
                Log.v(TAG, "Capture Failure... : $reply ($sendUrl)")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG: String = OmdsSingleShotControl::class.java.simpleName
        private const val TIMEOUT_MS = 3000
        private const val CAPTURE_COMMAND = "/exec_takemotion.cgi?com=starttake"
        private const val CAPTURE_COMMAND_OPC = "/exec_takemotion.cgi?com=newstarttake"
    }

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"
    }
}
